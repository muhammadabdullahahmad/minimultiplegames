package com.example.minimultiplegames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private val RPSPurple = Color(0xFF673AB7)
private val WinGreen = Color(0xFF4CAF50)
private val LoseRed = Color(0xFFF44336)
private val TieYellow = Color(0xFFFF9800)

enum class Choice(val emoji: String, val label: String) {
    ROCK("🪨", "Rock"),
    PAPER("📄", "Paper"),
    SCISSORS("✂️", "Scissors")
}

enum class Result(val message: String, val color: Color) {
    WIN("You Win!", WinGreen),
    LOSE("You Lose!", LoseRed),
    TIE("It's a Tie!", TieYellow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RockPaperScissorsScreen(navController: NavController) {
    var playerChoice by remember { mutableStateOf<Choice?>(null) }
    var computerChoice by remember { mutableStateOf<Choice?>(null) }
    var result by remember { mutableStateOf<Result?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerScore by remember { mutableIntStateOf(0) }
    var computerScore by remember { mutableIntStateOf(0) }
    var ties by remember { mutableIntStateOf(0) }

    fun determineWinner(player: Choice, computer: Choice): Result {
        return when {
            player == computer -> Result.TIE
            (player == Choice.ROCK && computer == Choice.SCISSORS) ||
            (player == Choice.PAPER && computer == Choice.ROCK) ||
            (player == Choice.SCISSORS && computer == Choice.PAPER) -> Result.WIN
            else -> Result.LOSE
        }
    }

    fun play(choice: Choice) {
        if (isPlaying) return
        isPlaying = true
        playerChoice = choice
        computerChoice = null
        result = null
    }

    LaunchedEffect(playerChoice, isPlaying) {
        if (playerChoice != null && isPlaying) {
            delay(500)
            val computer = Choice.entries.random()
            computerChoice = computer
            delay(300)
            val gameResult = determineWinner(playerChoice!!, computer)
            result = gameResult
            when (gameResult) {
                Result.WIN -> playerScore++
                Result.LOSE -> computerScore++
                Result.TIE -> ties++
            }
            delay(1500)
            isPlaying = false
        }
    }

    fun resetScores() {
        playerScore = 0
        computerScore = 0
        ties = 0
        playerChoice = null
        computerChoice = null
        result = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rock Paper Scissors", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { resetScores() }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RPSPurple)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Board
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreItem("You", playerScore, WinGreen)
                    ScoreItem("Ties", ties, TieYellow)
                    ScoreItem("CPU", computerScore, LoseRed)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Battle Arena
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player side
                ChoiceDisplay(
                    choice = playerChoice,
                    label = "You",
                    isWinner = result == Result.WIN
                )

                // VS
                Text(
                    text = "VS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RPSPurple
                )

                // Computer side
                ChoiceDisplay(
                    choice = computerChoice,
                    label = "CPU",
                    isWinner = result == Result.LOSE,
                    isLoading = isPlaying && computerChoice == null
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result display
            if (result != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = result!!.color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = result!!.message,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }
            } else {
                Text(
                    text = if (isPlaying) "Waiting..." else "Make your choice!",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Choice buttons
            Text(
                text = "Choose your weapon:",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Choice.entries.forEach { choice ->
                    ChoiceButton(
                        choice = choice,
                        isSelected = playerChoice == choice,
                        enabled = !isPlaying,
                        onClick = { play(choice) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScoreItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = score.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ChoiceDisplay(
    choice: Choice?,
    label: String,
    isWinner: Boolean,
    isLoading: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isWinner) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RPSPurple
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(if (isLoading) loadingScale else scale)
                .clip(CircleShape)
                .background(
                    when {
                        isWinner -> WinGreen.copy(alpha = 0.2f)
                        choice != null -> Color(0xFFF5F5F5)
                        else -> Color(0xFFE0E0E0)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    isLoading -> "❓"
                    choice != null -> choice.emoji
                    else -> "?"
                },
                fontSize = 48.sp
            )
        }
    }
}

@Composable
private fun ChoiceButton(
    choice: Choice,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RPSPurple else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = choice.emoji,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = choice.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}
