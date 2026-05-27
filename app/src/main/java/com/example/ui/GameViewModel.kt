package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.WrongQuestion
import com.example.data.model.Question
import com.example.data.remote.ElfCoachResponse
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface GameUiState {
    object Welcome : GameUiState
    object Loading : GameUiState
    data class ActiveQuestion(
        val level: Int,
        val coachFeedback: String,
        val lastWasCorrect: Boolean?,
        val lastExplanation: String,
        val question: Question,
        val isAiMode: Boolean
    ) : GameUiState

    data class RevivalMode(
        val wrongList: List<WrongQuestion>,
        val currentIdx: Int = 0,
        val feedback: String = "",
        val isCorrect: Boolean? = null,
        val explanation: String = "",
        val completed: Boolean = false
    ) : GameUiState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database.wrongQuestionDao())

    val wrongQuestions: StateFlow<List<WrongQuestion>> = repository.allWrongQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wrongQuestionsCount: StateFlow<Int> = repository.wrongQuestionsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aiAvailable: Boolean = repository.isGeminiOnlineAvailable()

    private val _aiModeEnabled = MutableStateFlow(aiAvailable) // auto-enable if key is configured
    val aiModeEnabled = _aiModeEnabled.asStateFlow()

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Welcome)
    val uiState = _uiState.asStateFlow()

    private val _fruitsCount = MutableStateFlow(0)
    val fruitsCount = _fruitsCount.asStateFlow()

    private val prefs = application.getSharedPreferences("pinyin_elf_prefs", Context.MODE_PRIVATE)

    init {
        // Load persist elf fruits score
        _fruitsCount.value = prefs.getInt("elf_fruits_count2", 0)
    }

    fun setAiMode(enabled: Boolean) {
        if (enabled && !aiAvailable) return // can't enable if no key
        _aiModeEnabled.value = enabled
        // If mid-game, reload context
        val current = _uiState.value
        if (current is GameUiState.ActiveQuestion) {
            loadLevel(current.level, isFirstTime = true)
        }
    }

    fun loadLevel(level: Int, isFirstTime: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading

            val prevQuestion: Question?
            val prevAnswer: String?

            if (isFirstTime) {
                prevQuestion = null
                prevAnswer = null
            } else {
                // Should not happen, first time is handled
                prevQuestion = null
                prevAnswer = null
            }

            val isAi = _aiModeEnabled.value
            if (isAi) {
                try {
                    val response = repository.queryGeminiCoach(level, prevQuestion, prevAnswer)
                    setNextUiState(level, response, isAi)
                } catch (e: Exception) {
                    // Fallback
                    val fallback = repository.getLocalFallbackResponse(level, prevQuestion, prevAnswer)
                    setNextUiState(level, fallback, false)
                }
            } else {
                val response = repository.getLocalFallbackResponse(level, prevQuestion, prevAnswer)
                setNextUiState(level, response, false)
            }
        }
    }

    private fun setNextUiState(level: Int, response: ElfCoachResponse, isAi: Boolean) {
        val qJson = response.nextQuestion
        if (qJson != null) {
            _uiState.value = GameUiState.ActiveQuestion(
                level = level,
                coachFeedback = response.feedback,
                lastWasCorrect = response.isCorrect,
                lastExplanation = response.explanation,
                question = Question(
                    id = (100..999).random(),
                    level = qJson.level,
                    questionText = qJson.questionText,
                    options = qJson.options,
                    correctAnswer = qJson.correctAnswer,
                    explanation = qJson.explanation,
                    hint = qJson.hint
                ),
                isAiMode = isAi
            )
        } else {
            // Safe fallback if JSON parsing lacked question
            val fallback = repository.getLocalFallbackResponse(level, null, null)
            setNextUiState(level, fallback, false)
        }
    }

    fun submitAnswer(answer: String) {
        val current = _uiState.value
        if (current !is GameUiState.ActiveQuestion) return

        viewModelScope.launch {
            val level = current.level
            val question = current.question
            val isAi = _aiModeEnabled.value

            // Simple trim matching and comparison
            val cleanedAnswer = answer.trim()
            val cleanedCorrect = question.correctAnswer.trim()

            // Handle choice questions matching cases like "A", "A. xxx" or direct text
            val isCorrect = if (question.options.isNotEmpty()) {
                val letterOnlyAnswer = cleanedAnswer.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                val letterOnlyCorrect = cleanedCorrect.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                letterOnlyAnswer == letterOnlyCorrect || cleanedAnswer.equals(cleanedCorrect, ignoreCase = true) || cleanedAnswer.contains(cleanedCorrect, ignoreCase = true)
            } else {
                cleanedAnswer.equals(cleanedCorrect, ignoreCase = true) ||
                        cleanedAnswer.contains(cleanedCorrect, ignoreCase = true) ||
                        cleanedCorrect.contains(cleanedAnswer, ignoreCase = true)
            }

            if (isCorrect) {
                // Add points
                addFruits(5)
            } else {
                // Save to local database for Revival session
                repository.saveWrongQuestion(
                    level = question.level,
                    questionText = question.questionText,
                    options = question.options,
                    correctAnswer = question.correctAnswer,
                    explanation = question.explanation,
                    hint = question.hint
                )
            }

            _uiState.value = GameUiState.Loading

            // Query next question using the latest stats passed to coach
            if (isAi) {
                try {
                    val response = repository.queryGeminiCoach(level, question, cleanedAnswer)
                    setNextUiState(level, response, isAi)
                } catch (e: Exception) {
                    val response = repository.getLocalFallbackResponse(level, question, cleanedAnswer)
                    setNextUiState(level, response, false)
                }
            } else {
                val response = repository.getLocalFallbackResponse(level, question, cleanedAnswer)
                setNextUiState(level, response, false)
            }
        }
    }

    private fun addFruits(amount: Int) {
        val newVal = _fruitsCount.value + amount
        _fruitsCount.value = newVal
        prefs.edit().putInt("elf_fruits_count2", newVal).apply()
    }

    // "不玩了" triggers Revival Mode if there are wrong questions
    fun triggerRevival() {
        val currentWrongList = wrongQuestions.value
        if (currentWrongList.isEmpty()) {
            _uiState.value = GameUiState.Welcome
        } else {
            _uiState.value = GameUiState.RevivalMode(
                wrongList = currentWrongList,
                currentIdx = 0,
                feedback = "灵儿姐，先别走！我们把今天弄错的题目消灭掉，就算胜利毕业哦！🧚‍♂️",
                isCorrect = null,
                completed = false
            )
        }
    }

    fun submitRevivalAnswer(answer: String) {
        val state = _uiState.value
        if (state !is GameUiState.RevivalMode) return

        viewModelScope.launch {
            val wrongQuestion = state.wrongList[state.currentIdx]
            val cleanedAnswer = answer.trim()
            val cleanedCorrect = wrongQuestion.correctAnswer.trim()

            val hasOptions = wrongQuestion.optionsJson.isNotEmpty()
            val isCorrect = if (hasOptions) {
                val letterAnswer = cleanedAnswer.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                val letterCorrect = cleanedCorrect.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                letterAnswer == letterCorrect || cleanedAnswer.equals(cleanedCorrect, ignoreCase = true)
            } else {
                cleanedAnswer.equals(cleanedCorrect, ignoreCase = true) ||
                        cleanedAnswer.contains(cleanedCorrect, ignoreCase = true) ||
                        cleanedCorrect.contains(cleanedAnswer, ignoreCase = true)
            }

            if (isCorrect) {
                // Answered correctly! Earn 10 fruits for correcting mistakes!
                addFruits(10)
                // Remove from wrong book
                repository.removeWrongQuestion(wrongQuestion.id)

                val nextIdx = state.currentIdx + 1
                val isDone = nextIdx >= state.wrongList.size

                _uiState.value = state.copy(
                    isCorrect = true,
                    feedback = "太棒啦，灵儿姐！你战胜它了！👍",
                    explanation = "这道错题已经被我们踩在脚下啦！加油！",
                    currentIdx = if (isDone) state.currentIdx else nextIdx,
                    completed = isDone
                )
            } else {
                _uiState.value = state.copy(
                    isCorrect = false,
                    feedback = "哎呀，灵儿姐，它又顽皮啦，再仔细看一眼。🔍",
                    explanation = wrongQuestion.explanation
                )
            }
        }
    }

    fun completeRevival() {
        _uiState.value = GameUiState.Welcome
    }

    fun clearAllStats() {
        viewModelScope.launch {
            repository.clearAllWrongQuestions()
            _fruitsCount.value = 0
            prefs.edit().putInt("elf_fruits_count2", 0).apply()
            _uiState.value = GameUiState.Welcome
        }
    }

    fun goToWelcome() {
        _uiState.value = GameUiState.Welcome
    }
}
