package com.example.minimultiplegames.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class PlayerColor(val color: Color, val displayName: String) {
    RED(Color(0xFFE53935), "Red"),
    GREEN(Color(0xFF43A047), "Green"),
    YELLOW(Color(0xFFFFB300), "Yellow"),
    BLUE(Color(0xFF1E88E5), "Blue")
}

data class Token(
    val id: Int,
    val player: PlayerColor,
    var position: Int = -1, // -1 means in home base
    var isFinished: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoScreen(navController: NavController) {
    var tokens by remember {
        mutableStateOf(
            PlayerColor.entries.flatMap { player ->
                (0..3).map { Token(player.ordinal * 4 + it, player) }
            }
        )
    }
    var currentPlayer by remember { mutableStateOf(PlayerColor.RED) }
    var diceValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }
    var hasRolled by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Red's turn - Roll the dice!") }
    var winner by remember { mutableStateOf<PlayerColor?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f * 3 else 0f,
        animationSpec = tween(durationMillis = if (isRolling) 600 else 0),
        label = "dice"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            repeat(8) {
                diceValue = Random.nextInt(1, 7)
                delay(60)
            }
            diceValue = Random.nextInt(1, 7)
            isRolling = false
            hasRolled = true

            // Check if player can move any token
            val playerTokens = tokens.filter { it.player == currentPlayer && !it.isFinished }
            val canMove = playerTokens.any { token ->
                if (token.position == -1) diceValue == 6
                else token.position + diceValue <= 56
            }

            if (!canMove) {
                message = "${currentPlayer.displayName} can't move. "
                delay(1000)
                // Next player
                currentPlayer = PlayerColor.entries[(currentPlayer.ordinal + 1) % 4]
                hasRolled = false
                message = "${currentPlayer.displayName}'s turn - Roll the dice!"
            } else {
                message = "${currentPlayer.displayName} rolled $diceValue - Select a token to move"
            }
        }
    }

    fun moveToken(token: Token) {
        if (!hasRolled || token.player != currentPlayer || token.isFinished || winner != null) return

        val canMove = if (token.position == -1) diceValue == 6 else true

        if (canMove) {
            tokens = tokens.map {
                if (it.id == token.id) {
                    val newPos = if (it.position == -1) 0 else it.position + diceValue
                    val finished = newPos >= 56
                    it.copy(position = if (finished) 56 else newPos, isFinished = finished)
                } else it
            }

            // Check for winner
            val playerTokens = tokens.filter { it.player == currentPlayer }
            if (playerTokens.all { it.isFinished }) {
                winner = currentPlayer
                message = "${currentPlayer.displayName} WINS!"
            } else {
                // Extra turn on 6
                if (diceValue != 6) {
                    currentPlayer = PlayerColor.entries[(currentPlayer.ordinal + 1) % 4]
                }
                hasRolled = false
                message = "${currentPlayer.displayName}'s turn - Roll the dice!"
            }
        }
    }

    fun resetGame() {
        tokens = PlayerColor.entries.flatMap { player ->
            (0..3).map { Token(player.ordinal * 4 + it, player) }
        }
        currentPlayer = PlayerColor.RED
        diceValue = 1
        hasRolled = false
        winner = null
        message = "Red's turn - Roll the dice!"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ludo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Message
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = currentPlayer.color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ludo Board
            LudoBoard(
                tokens = tokens,
                currentPlayer = currentPlayer,
                hasRolled = hasRolled,
                onTokenClick = { moveToken(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dice and controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Current player indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current", fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(currentPlayer.color)
                    )
                }

                // Dice
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotation)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(3.dp, currentPlayer.color, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isRolling && !hasRolled && winner == null) {
                            isRolling = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diceValue.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Roll button
                Button(
                    onClick = { isRolling = true },
                    enabled = !isRolling && !hasRolled && winner == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentPlayer.color
                    )
                ) {
                    Text("Roll")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (winner != null) {
                Button(onClick = { resetGame() }) {
                    Text("Play Again")
                }
            } else {
                TextButton(onClick = { resetGame() }) {
                    Text("Reset Game")
                }
            }
        }
    }
}

@Composable
fun LudoBoard(
    tokens: List<Token>,
    currentPlayer: PlayerColor,
    hasRolled: Boolean,
    onTokenClick: (Token) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
            .border(4.dp, Color(0xFF795548), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        // Draw the 4 home bases
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                // Red home (top-left)
                HomeBase(
                    color = PlayerColor.RED,
                    tokens = tokens.filter { it.player == PlayerColor.RED && it.position == -1 },
                    isCurrentPlayer = currentPlayer == PlayerColor.RED && hasRolled,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )

                // Top path
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Green home (top-right)
                HomeBase(
                    color = PlayerColor.GREEN,
                    tokens = tokens.filter { it.player == PlayerColor.GREEN && it.position == -1 },
                    isCurrentPlayer = currentPlayer == PlayerColor.GREEN && hasRolled,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.weight(0.4f)) {
                // Left path
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Center with active tokens display
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    // Show active tokens (simplified - not exact positions)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    ) {
                        tokens.filter { it.position in 0..56 && !it.isFinished }
                            .groupBy { it.player }
                            .forEach { (player, playerTokens) ->
                                Row {
                                    playerTokens.take(2).forEach { token ->
                                        TokenPiece(
                                            token = token,
                                            isSelectable = hasRolled && token.player == currentPlayer,
                                            onClick = { onTokenClick(token) },
                                            size = 20
                                        )
                                    }
                                }
                            }
                    }
                }

                // Right path
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }

            Row(modifier = Modifier.weight(1f)) {
                // Blue home (bottom-left)
                HomeBase(
                    color = PlayerColor.BLUE,
                    tokens = tokens.filter { it.player == PlayerColor.BLUE && it.position == -1 },
                    isCurrentPlayer = currentPlayer == PlayerColor.BLUE && hasRolled,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )

                // Bottom path
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Yellow home (bottom-right)
                HomeBase(
                    color = PlayerColor.YELLOW,
                    tokens = tokens.filter { it.player == PlayerColor.YELLOW && it.position == -1 },
                    isCurrentPlayer = currentPlayer == PlayerColor.YELLOW && hasRolled,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Finished tokens display
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp)
                .background(Color(0xFF4CAF50), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HOME", fontSize = 8.sp, color = Color.White)
                Text(
                    text = tokens.count { it.isFinished }.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HomeBase(
    color: PlayerColor,
    tokens: List<Token>,
    isCurrentPlayer: Boolean,
    onTokenClick: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color.color.copy(alpha = 0.3f))
            .border(2.dp, color.color)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                tokens.take(2).forEach { token ->
                    TokenPiece(
                        token = token,
                        isSelectable = isCurrentPlayer,
                        onClick = { onTokenClick(token) }
                    )
                }
                // Empty slots
                repeat(2 - tokens.take(2).size) {
                    EmptyTokenSlot(color.color)
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                tokens.drop(2).take(2).forEach { token ->
                    TokenPiece(
                        token = token,
                        isSelectable = isCurrentPlayer,
                        onClick = { onTokenClick(token) }
                    )
                }
                repeat(2 - tokens.drop(2).take(2).size) {
                    EmptyTokenSlot(color.color)
                }
            }
        }
    }
}

@Composable
fun TokenPiece(
    token: Token,
    isSelectable: Boolean,
    onClick: () -> Unit,
    size: Int = 30
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(token.player.color)
            .border(
                width = if (isSelectable) 3.dp else 1.dp,
                color = if (isSelectable) Color.White else token.player.color.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(enabled = isSelectable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (token.position >= 0) {
            Text(
                text = token.position.toString(),
                fontSize = (size / 3).sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyTokenSlot(color: Color) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(2.dp, color.copy(alpha = 0.3f), CircleShape)
    )
}
