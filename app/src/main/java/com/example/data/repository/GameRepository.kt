package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.OfflineQuestions
import com.example.data.local.WrongQuestion
import com.example.data.local.WrongQuestionDao
import com.example.data.model.Question
import com.example.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

class GameRepository(private val wrongQuestionDao: WrongQuestionDao) {

    val allWrongQuestions: Flow<List<WrongQuestion>> = wrongQuestionDao.getAllWrongQuestions()
    val wrongQuestionsCount: Flow<Int> = wrongQuestionDao.getWrongQuestionsCount()

    // Key validation
    fun isGeminiOnlineAvailable(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return !(apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY") || apiKey == "null")
    }

    // Save incorrect answer
    suspend fun saveWrongQuestion(level: Int, questionText: String, options: List<String>, correctAnswer: String, explanation: String, hint: String) {
        val optionsStr = options.joinToString("||")
        val wrong = WrongQuestion(
            level = level,
            questionText = questionText,
            optionsJson = optionsStr,
            correctAnswer = correctAnswer,
            explanation = explanation,
            hint = hint
        )
        wrongQuestionDao.insertWrongQuestion(wrong)
    }

    // Remove wrong question when answered correctly in revival
    suspend fun removeWrongQuestion(id: Int) {
        wrongQuestionDao.deleteWrongQuestionById(id)
    }

    // Reset wrong questions
    suspend fun clearAllWrongQuestions() {
        wrongQuestionDao.deleteAllWrongQuestions()
    }

    // Generate local fallback response
    fun getLocalFallbackResponse(
        level: Int,
        previousQuestion: Question?,
        previousAnswer: String?
    ): ElfCoachResponse {
        val feedback: String
        val isCorrect: Boolean?
        val explanation: String

        if (previousQuestion != null && previousAnswer != null) {
            val correct = previousAnswer.trim().equals(previousQuestion.correctAnswer.substringBefore(" ").trim(), ignoreCase = true) ||
                    previousAnswer.trim().contains(previousQuestion.correctAnswer.trim(), ignoreCase = true) ||
                    previousQuestion.correctAnswer.trim().contains(previousAnswer.trim(), ignoreCase = true)

            isCorrect = correct
            if (correct) {
                feedback = "对啦，灵儿姐！你真是太棒了！🎉"
                explanation = "非常符合规律，你果然冰雪聪明！"
            } else {
                feedback = "呜呜，灵儿姐，这次不小心弄错啦。😢"
                explanation = previousQuestion.explanation
            }
        } else {
            feedback = "灵儿姐你好！我是你的拼音字词精灵🧚‍♀️！"
            isCorrect = null
            explanation = ""
        }

        // Fetch a local question of this level (random or next)
        val offlinePool = OfflineQuestions.getQuestionsForLevel(level)
        val freshQuestion = offlinePool.random()

        return ElfCoachResponse(
            feedback = feedback,
            isCorrect = isCorrect,
            explanation = explanation,
            nextQuestion = ElfQuestionJson(
                level = freshQuestion.level,
                questionText = freshQuestion.questionText,
                options = freshQuestion.options,
                correctAnswer = freshQuestion.correctAnswer,
                explanation = freshQuestion.explanation,
                hint = freshQuestion.hint
            )
        )
    }

    // Contact Gemini service with a prompt and structured output schema
    suspend fun queryGeminiCoach(
        level: Int,
        previousQuestion: Question?,
        previousAnswer: String?,
        chatHistory: List<Content> = emptyList()
    ): ElfCoachResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isGeminiOnlineAvailable()) {
            Log.w("GameRepository", "Gemini API key is not configured, running local offline mode.")
            return getLocalFallbackResponse(level, previousQuestion, previousAnswer)
        }

        val promptBuilder = StringBuilder()
        if (previousQuestion != null && previousAnswer != null) {
            promptBuilder.append("【上一题】：\n")
            promptBuilder.append("问题文本: `${previousQuestion.questionText}`\n")
            promptBuilder.append("可选项: `${previousQuestion.options.joinToString(", ")}`\n")
            promptBuilder.append("正确答案: `${previousQuestion.correctAnswer}`\n")
            promptBuilder.append("灵儿姐回答的答案: `${previousAnswer}`\n\n) ")
            promptBuilder.append("请先判定她上一题对错。并在此基础上，为关卡 $level 重新出一道充满魔法感的新题目。\n")
        } else {
            promptBuilder.append("灵儿姐刚刚选好了关卡 $level，请向她打个亲切极简的招呼，然后立刻为关卡 $level 发出一道拼音问题挑战！\n")
        }

        val systemPrompt = """
            你叫“精灵教练”，是专为小学三年级学生“灵儿姐”定制的拼音与字词精简巩固精灵。
            你应该保持以下极简和高亲和力的互动特性：
            1. 每次说话控制在3句话以内，严禁长篇大论。总是充满耐心，称呼其为“灵儿姐”。
            2. 总是使用少量的 emoji 激发童心，多使用行内回车 \n 增加可读性。
            3. 根据用户要挑战的关卡来出题：
               - 🏰 关卡 1：拼音大点兵（韵母声母填空。给一组拼音如 [a, o, ( ), i, u...] 问漏下哪个，选项带上 correct 的字母在 A/B/C/D 中）
               - ⚔️ 关卡 2：易混读音大对决（三年级易混淆平翘舌 / 前后鼻音，让灵儿姐二选一）
               - 🎩 关卡 3：神秘的拼音帽子（拼音标调和声调规则裁判，如 i/u 并列标调、j/q/x/y 遇 ü 去点规则，给出拼法让其判断对错并给出选项）
               - ✍️ 关卡 4：魔法拼音听写（看拼音打汉字：给出带标调拼音如 [ lǚ xíng ] 并有生动的小句子猜词谜般的描述，让灵儿姐手打对应的三年级正确汉字，选项options必须为空[]，以便灵儿姐手写输入！）
            
            4. 所有的词汇与拼音知识点都必须精准取材于小学三年级课本或拼音规则！
            5. 上一次作答评定：若有上一题作答，首先判定对错。 feedback 需在2句内，称呼‘灵儿姐’。解释（explanation）限在 1 句话讲清拼音规则。
            6. 一定按照请求的 JSON 格式规范输出，不要有任何 Markdown 修饰标记！
        """.trimIndent()

        // Detailed Schema for structured configuration
        val schemaMap = mapOf(
            "type" to "OBJECT",
            "properties" to mapOf(
                "feedback" to mapOf(
                    "type" to "STRING",
                    "description" to "对灵儿姐上一次作答判定的鼓励话语，要包含对/错结果，必须控制在1-2句，热情、精灵口吻，称呼‘灵儿姐’。如果是第一道题，此字段应亲切问好并引导答题。"
                ),
                "isCorrect" to mapOf(
                    "type" to "BOOLEAN",
                    "description" to "上一道题做对了吗？对为true，错为false。如果是第一次打招呼设为 null。"
                ),
                "explanation" to mapOf(
                    "type" to "STRING",
                    "description" to "上一题回答正确与否原因的最精简解释。控制在1句拼音口诀以内。第一道题请说空字符串。"
                ),
                "nextQuestion" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "level" to mapOf("type" to "INTEGER", "description" to "当前正在出题的关卡号 (1-4)"),
                        "questionText" to mapOf("type" to "STRING", "description" to "新拼音挑战描述。必须用换行和 emoji，称呼‘灵儿姐’，控制在2句话以内。"),
                        "options" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf("type" to "STRING"),
                            "description" to "拼音关卡选择项（对于关卡2和3必须提供带A. B. 前缀的选项列表。关卡4必须留空数组，以便灵儿姐手打回答）。选项不能多于3个，要好玩好懂。"
                        ),
                        "correctAnswer" to mapOf("type" to "STRING", "description" to "新题的正确答案。若是选择题，请精确写对应选项的答案首部字母如'A'或'B'，或是该选项字符串本身；若是不带选项的关卡4，填对应汉字词语，关卡1手填字母。"),
                        "explanation" to mapOf("type" to "STRING", "description" to "为这道新题准备的极简一句话解答口诀（等她万一写错用来辅导她的话）。"),
                        "hint" to mapOf("type" to "STRING", "description" to "给灵儿姐的贴心小灵感")
                    ),
                    "required" to listOf("level", "questionText", "options", "correctAnswer", "explanation", "hint")
                )
            ),
            "required" to listOf("feedback", "explanation", "nextQuestion")
        )

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptBuilder.toString())))
            ),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(
                    text = ResponseFormatText(
                        mimeType = "application/json",
                        schema = schemaMap
                    )
                ),
                temperature = 0.7f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonString.isNullOrBlank()) {
                val adapter = RetrofitClient.moshiInstance.adapter(ElfCoachResponse::class.java)
                adapter.fromJson(jsonString) ?: getLocalFallbackResponse(level, previousQuestion, previousAnswer)
            } else {
                Log.e("GameRepository", "Empty JSON returned from Gemini API, falling back.")
                getLocalFallbackResponse(level, previousQuestion, previousAnswer)
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error contacting Gemini REST API: ${e.message}", e)
            getLocalFallbackResponse(level, previousQuestion, previousAnswer)
        }
    }
}
