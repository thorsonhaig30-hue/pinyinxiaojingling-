package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WrongQuestion
import com.example.data.model.Question
import com.example.ui.GameUiState
import com.example.ui.GameViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fruitsCount by viewModel.fruitsCount.collectAsStateWithLifecycle()
    val wrongCount by viewModel.wrongQuestionsCount.collectAsStateWithLifecycle()
    val aiModeEnabled by viewModel.aiModeEnabled.collectAsStateWithLifecycle()
    val aiAvailable = viewModel.aiAvailable

    // Forest Mint Background Theme
    val forestGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(forestGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Progress Bar, Shield Name, Points, and AI Toggle)
                HeaderSection(
                    fruitsCount = fruitsCount,
                    wrongCount = wrongCount,
                    aiModeEnabled = aiModeEnabled,
                    aiAvailable = aiAvailable,
                    onAiToggle = { viewModel.setAiMode(it) },
                    onGoWelcome = { viewModel.goToWelcome() },
                    onReset = { viewModel.clearAllStats() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Screen Switch Content
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    },
                    label = "MainContentTransition"
                ) { state ->
                    when (state) {
                        is GameUiState.Welcome -> {
                            WelcomeScreenContent(
                                wrongCount = wrongCount,
                                onSelectLevel = { viewModel.loadLevel(it, isFirstTime = true) },
                                onStartRevival = { viewModel.triggerRevival() }
                            )
                        }
                        is GameUiState.Loading -> {
                            LoadingStateContent()
                        }
                        is GameUiState.ActiveQuestion -> {
                            ActiveQuestionContent(
                                state = state,
                                onSubmit = { viewModel.submitAnswer(it) },
                                onTriggerRevival = { viewModel.triggerRevival() }
                            )
                        }
                        is GameUiState.RevivalMode -> {
                            RevivalModeContent(
                                state = state,
                                onSubmitAnswer = { viewModel.submitRevivalAnswer(it) },
                                onCompleted = { viewModel.completeRevival() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    fruitsCount: Int,
    wrongCount: Int,
    aiModeEnabled: Boolean,
    aiAvailable: Boolean,
    onAiToggle: (Boolean) -> Unit,
    onGoWelcome: () -> Unit,
    onReset: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo / Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onGoWelcome() }
                .testTag("app_logo_title")
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Text("🧚‍♀️", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "灵儿姐 👑",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "拼音字词精灵",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Score Badge + Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fruits Reward
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                modifier = Modifier.testTag("fruits_score_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🍎", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fruitsCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFF57F17)
                    )
                }
            }

            // AI Toggle Button
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (aiModeEnabled) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                ),
                modifier = Modifier
                    .clickable(enabled = aiAvailable) { onAiToggle(!aiModeEnabled) }
                    .testTag("ai_toggle_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (aiModeEnabled) "✨ AI在线" else "📴 本地", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Config menu icon
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("settings_btn")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "菜单",
                        tint = Color(0xFF2E7D32)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("返回大本营") },
                        onClick = {
                            showMenu = false
                            onGoWelcome()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("重置我的记录", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onReset()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreenContent(
    wrongCount: Int,
    onSelectLevel: (Int) -> Unit,
    onStartRevival: () -> Unit
) {
    var textInputState by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dialogue from initialization instructions
        ElfTalkBubble(
            text = "灵儿姐你好！我是你的拼音字词精灵。✨\n\n" +
                    "今天我们准备了 4 个极速冒险关卡：\n" +
                    "🏰 1. 基础填空\n" +
                    "⚔️ 2. 易混读音\n" +
                    "🎩 3. 拼音帽子\n" +
                    "🎒 4. 拼音听写\n\n" +
                    "你今天想先挑战第几关？请输入或点击下方数字。",
            isAiReaction = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Quick Level Gates
        Text(
            text = "🚀 选择关卡进行大冒险：",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1B5E20),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LevelCardButton(
                title = "🏰 基础填空",
                num = "1",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLevel(1) }
            )
            LevelCardButton(
                title = "⚔️ 易混读音",
                num = "2",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLevel(2) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LevelCardButton(
                title = "🎩 拼音帽子",
                num = "3",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLevel(3) }
            )
            LevelCardButton(
                title = "✍️ 拼音听写",
                num = "4",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLevel(4) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input simulation for extra interactive play
        OutlinedTextField(
            value = textInputState,
            onValueChange = { input ->
                textInputState = input
                val digit = input.trim().toIntOrNull()
                if (digit in 1..4) {
                    focusManager.clearFocus()
                    onSelectLevel(digit!!)
                    textInputState = ""
                }
            },
            label = { Text("在此回复数字即可闯关") },
            placeholder = { Text("输入 1, 2, 3 或 4") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                val digit = textInputState.trim().toIntOrNull()
                if (digit in 1..4) {
                    onSelectLevel(digit!!)
                }
                textInputState = ""
                focusManager.clearFocus()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = Color(0xFF2E7D32),
                focusedIndicatorColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("welcome_cli_input")
        )

        if (wrongCount > 0) {
            Spacer(modifier = Modifier.height(24.dp))
            // Revival Trigger button
            Button(
                onClick = onStartRevival,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("revival_entry_btn"),
                contentPadding = PaddingValues()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🎒 错题本蓄满啦！打响 错题复活赛 (${wrongCount})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelCardButton(
    title: String,
    num: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() }
            .testTag("level_btn_$num"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "冒险关 $num",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActiveQuestionContent(
    state: GameUiState.ActiveQuestion,
    onSubmit: (String) -> Unit,
    onTriggerRevival: () -> Unit
) {
    var answerState by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.question.questionText) {
        // Reset answer field on new question loading
        answerState = ""
        showHint = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Elf Feedback & dialogue balloon
        ElfTalkBubble(
            text = if (state.coachFeedback.isNotEmpty()) {
                "${state.coachFeedback}\n\n" +
                        (if (state.lastExplanation.isNotEmpty()) "💡 精灵讲解：${state.lastExplanation}\n\n" else "") +
                        state.question.questionText
            } else {
                state.question.questionText
            },
            isAiReaction = state.lastWasCorrect != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Level Title Tag
        val levelTag = when (state.level) {
            1 -> "🏰 关卡 1：拼音大点兵"
            2 -> "⚔️ 关卡 2：易混读音大对决"
            3 -> "🎩 关卡 3：神秘的拼音帽子"
            4 -> "✍️ 关卡 4：魔法拼音听写"
            else -> "✨ 拼音魔法关"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = levelTag,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1B5E20)
                    )
                    IconButton(
                        onClick = { showHint = !showHint },
                        modifier = Modifier.size(24.dp).testTag("hint_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "提示",
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (showHint && state.question.hint.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "🧚‍♀️ 精灵秘之暗示：${state.question.hint}",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options layout vs direct text input
                if (state.question.options.isNotEmpty()) {
                    // Level 1, 2, 3 multiple choices
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.question.options.forEach { option ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        keyboardController?.hide()
                                        onSubmit(option)
                                    }
                                    .testTag("option_${option.take(1).uppercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F5)),
                                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Level 4 text field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = answerState,
                            onValueChange = { answerState = it },
                            placeholder = { Text("在此手打汉字词语...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F8F5),
                                unfocusedContainerColor = Color(0xFFF1F8F5),
                                focusedIndicatorColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("text_answer_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (answerState.isNotBlank()) {
                                    onSubmit(answerState)
                                    answerState = ""
                                }
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (answerState.isNotBlank()) {
                                    onSubmit(answerState)
                                    answerState = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("text_answer_btn")
                        ) {
                            Text("作答", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trigger Revival / Quit
        Button(
            onClick = {
                onTriggerRevival()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFD8DC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_dont_play"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
        ) {
            Text(
                "🎒 灵儿姐困啦：不想玩了（进行错题大复活）",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF455A64)
            )
        }
    }
}

@Composable
fun RevivalModeContent(
    state: GameUiState.RevivalMode,
    onSubmitAnswer: (String) -> Unit,
    onCompleted: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.currentIdx) {
        textInput = ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header theme
        Text(
            text = "💀 错题复活大冒险 🏰",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFFC2185B),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Custom Talk bubble with Dark Pink style
        Card(
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomEnd = 24.dp,
                bottomStart = 4.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("🧚‍♀️", fontSize = 24.sp, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = state.feedback,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF880E4F),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    if (state.explanation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 ${state.explanation}",
                            fontSize = 11.sp,
                            color = Color(0xFFAD1457)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.completed) {
            // Revival Completed Screen
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "灵儿姐，错题全清空！",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF880E4F)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "你实在是太棒、太用心了！错题挑战圆满毕业，所有的字词都被你完全弄清喽！",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD1457)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("revival_finish_btn")
                    ) {
                        Text("完美出师，返回主页", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Render active question being revived
            val currentQuestion = state.wrongList[state.currentIdx]
            val optionsList = if (currentQuestion.optionsJson.isNotEmpty()) {
                currentQuestion.optionsJson.split("||")
            } else {
                emptyList()
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF8BBD0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "第 ${state.currentIdx + 1} / ${state.wrongList.size} 关复活考：",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentQuestion.questionText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF263238),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (optionsList.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            optionsList.forEach { opt ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            keyboardController?.hide()
                                            onSubmitAnswer(opt)
                                        }
                                        .testTag("revival_option_${opt.take(1).uppercase()}"),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                                    border = BorderStroke(1.dp, Color(0xFFF8BBD0))
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                        Text(
                                            text = opt,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF880E4F)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("在此拼写填补正确汉字...") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("revival_text_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFFF1F5),
                                    unfocusedContainerColor = Color(0xFFFFF1F5),
                                    focusedIndicatorColor = Color(0xFFD81B60)
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (textInput.isNotBlank()) {
                                        onSubmitAnswer(textInput)
                                        textInput = ""
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        onSubmitAnswer(textInput)
                                        textInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                modifier = Modifier.height(52.dp).testTag("revival_text_btn")
                            ) {
                                Text("打字", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exit early
            TextButton(
                onClick = onCompleted,
                modifier = Modifier.testTag("revival_cancel_btn")
            ) {
                Text("算了，先退赛休息一下", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LoadingStateContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2E7D32),
            modifier = Modifier.testTag("loading_progress")
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在召唤拼音精灵魔法...",
            fontSize = 13.sp,
            color = Color(0xFF1B5E20),
            fontWeight = FontWeight.Medium
        )
    }
}

// Speecy bubble styled with procedural elf avatar drawing
@Composable
fun ElfTalkBubble(
    text: String,
    isAiReaction: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Magical interactive Elf Sprite icon (procedural drawn)
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFF81C784), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ElfSpriteCanvas()
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Dialogue bubble
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 24.dp,
                bottomEnd = 24.dp,
                bottomStart = 24.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Gorgeous Procedural Magical Sprite Drawing on Custom Canvas in Compose
@Composable
fun ElfSpriteCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "ElfFloat")
    val translationY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TranslationY"
    )

    Canvas(
        modifier = Modifier
            .size(54.dp)
            .offset(y = translationY.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw glowing magical wings (Gold-Teal gradients)
        val wingBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0F2F1), Color(0xFF80CBC4), Color(0x00FFFFFF)),
            center = Offset(w / 2f, h / 2f),
            radius = w * 0.49f
        )
        // Left Wing
        drawOval(
            brush = wingBrush,
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.4f, h * 0.5f)
        )
        // Right Wing
        drawOval(
            brush = wingBrush,
            topLeft = Offset(w * 0.5f, h * 0.2f),
            size = Size(w * 0.4f, h * 0.5f)
        )

        // 2. Draw Elf Body
        val bodyBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))
        )
        drawCircle(
            brush = bodyBrush,
            radius = w * 0.24f,
            center = Offset(w / 2f, h * 0.52f)
        )

        // 3. Leaf Hat
        val path = Path().apply {
            moveTo(w * 0.26f, h * 0.40f)
            quadraticTo(w / 2f, h * 0.10f, w / 2f, h * 0.12f)
            quadraticTo(w * 0.74f, h * 0.40f, w * 0.74f, h * 0.40f)
            lineTo(w * 0.26f, h * 0.40f)
            close()
        }
        drawPath(path, color = Color(0xFF1B5E20))

        // Hat stem
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = w * 0.04f,
            center = Offset(w / 2f, h * 0.11f)
        )

        // 4. Face / Face highlights
        drawCircle(
            color = Color(0xFFFFF8E1),
            radius = w * 0.16f,
            center = Offset(w / 2f, h * 0.55f)
        )

        // Cute cartoon eyes
        drawCircle(
            color = Color(0xFF2E7D32),
            radius = w * 0.03f,
            center = Offset(w * 0.43f, h * 0.53f)
        )
        drawCircle(
            color = Color(0xFF2E7D32),
            radius = w * 0.03f,
            center = Offset(w * 0.57f, h * 0.53f)
        )

        // Eye highlights
        drawCircle(
            color = Color.White,
            radius = w * 0.01f,
            center = Offset(w * 0.42f, h * 0.52f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.01f,
            center = Offset(w * 0.56f, h * 0.52f)
        )

        // Smile
        val mouthPath = Path().apply {
            moveTo(w * 0.46f, h * 0.60f)
            quadraticTo(w / 2f, h * 0.65f, w * 0.54f, h * 0.60f)
        }
        drawPath(
            path = mouthPath,
            color = Color(0xFFD32F2F),
            style = Stroke(width = w * 0.03f)
        )
    }
}
