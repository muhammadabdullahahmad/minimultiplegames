package com.example.minimultiplegames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

private val SimonGreen = Color(0xFF4CAF50)
private val SimonRed = Color(0xFFF44336)
private val SimonYellow = Color(0xFFFFEB3B)
private val SimonBlue = Color(0xFF2196F3)
private val SimonPurple = Color(0xFF9C27B0)

enum class SimonColor(val color: Color, val brightColor: Color) {
    GREEN(Color(0xFF388E3C), Color(0xFF4CAF50)),
    RED(Color(0xFFD32F2F), Color(0xFFF44336)),
    YELLOW(Color(0xFFFBC02D), Color(0xFFFFEB3B)),
    BLUE(Color(0xFF1976D2), Color(0xFF2196F3))
}

enum class SimonGameState {
    IDLE, SHOWING_SEQUENCE, PLAYER_TURN, GAME_OVER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimonSaysScreen(navController: NavController) {
    var gameState by remember { mutableStateOf(SimonGameState.IDLE) }
    var sequence by remember { mutableStateOf(listOf<SimonColor>()) }
    var playerIndex by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }
    var activeColor by remember { mutableStateOf<SimonColor?>(null) }
    var message by remember { mutableStateOf("Press Start to Play!") }

    fun startGame() {
        sequence = listOf(SimonColor.entries.random())
        playerIndex = 0
        level = 1
        gameState = SimonGameState.SHOWING_SEQUENCE
        message = "Watch carefully..."
    }

    fun addToSequence() {
        sequence = sequence + SimonColor.entries.random()
        playerIndex = 0
        level = sequence.size
        gameState = SimonGameState.SHOWING_SEQUENCE
        message = "Watch carefully..."
    }

    fun playerInput(color: SimonColor) {
        if (gameState != SimonGameState.PLAYER_TURN) return

        activeColor = color

        if (color == sequence[playerIndex]) {
            playerIndex++
            if (playerIndex >= sequence.size) {
                if (level > highScore) highScore = level
                message = "Great! Level ${level + 1}"
                gameState = SimonGameState.IDLE
            }
        } else {
            if (level > highScore) highScore = level - 1
            gameState = SimonGameState.GAME_OVER
            message = "Game Over! You reached level $level"
        }
    }

    // Show sequence animation
    LaunchedEffect(gameState) {
        if (gameState == SimonGameState.SHOWING_SEQUENCE) {
            delay(500)
            for (color in sequence) {
                activeColor = color
                delay(500)
                activeColor = null
                delay(200)
            }
            gameState = SimonGameState.PLAYER_TURN
            message = "Your turn! (${sequence.size} colors)"
        }
    }

    // Reset active color after player input
    LaunchedEffect(activeColor) {
        if (activeColor != null && gameState == SimonGameState.PLAYER_TURN) {
            delay(200)
            activeColor = null
        }
    }

    // Auto-advance to next level
    LaunchedEffect(gameState, level) {
        if (gameState == SimonGameState.IDLE && level > 0) {
            delay(1000)
            addToSequence()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simon Says", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SimonPurple)
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
            // Score display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreBox("Level", if (level > 0) level.toString() else "-", SimonBlue)
                ScoreBox("Best", highScore.toString(), SimonGreen)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Message
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (gameState) {
                        SimonGameState.GAME_OVER -> SimonRed
                        SimonGameState.PLAYER_TURN -> SimonGreen
                        else -> SimonPurple
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simon board
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF333333))
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SimonButton(
                            simonColor = SimonColor.GREEN,
                            isActive = activeColor == SimonColor.GREEN,
                            enabled = gameState == SimonGameState.PLAYER_TURN,
                            onClick = { playerInput(SimonColor.GREEN) },
                            modifier = Modifier.weight(1f)
                        )
                        SimonButton(
                            simonColor = SimonColor.RED,
                            isActive = activeColor == SimonColor.RED,
                            enabled = gameState == SimonGameState.PLAYER_TURN,
                            onClick = { playerInput(SimonColor.RED) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SimonButton(
                            simonColor = SimonColor.YELLOW,
                            isActive = activeColor == SimonColor.YELLOW,
                            enabled = gameState == SimonGameState.PLAYER_TURN,
                            onClick = { playerInput(SimonColor.YELLOW) },
                            modifier = Modifier.weight(1f)
                        )
                        SimonButton(
                            simonColor = SimonColor.BLUE,
                            isActive = activeColor == SimonColor.BLUE,
                            enabled = gameState == SimonGameState.PLAYER_TURN,
                            onClick = { playerInput(SimonColor.BLUE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start/Restart button
            if (gameState == SimonGameState.IDLE && level == 0 || gameState == SimonGameState.GAME_OVER) {
                Button(
                    onClick = { startGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SimonPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (gameState == SimonGameState.GAME_OVER) "Play Again" else "Start Game",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScoreBox(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SimonButton(
    simonColor: SimonColor,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) simonColor.brightColor else simonColor.color)
            .clickable(enabled = enabled) { onClick() }
    )
}
