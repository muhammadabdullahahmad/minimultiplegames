package com.example.minimultiplegames.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class GameMode {
    MENU, VS_COMPUTER, TWO_PLAYER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(navController: NavController) {
    var gameMode by remember { mutableStateOf(GameMode.MENU) }
    var board by remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var scoreX by remember { mutableIntStateOf(0) }
    var scoreO by remember { mutableIntStateOf(0) }
    var isComputerThinking by remember { mutableStateOf(false) }

    fun checkWinner(board: List<String>): String? {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )

        for (pattern in winPatterns) {
            val (a, b, c) = pattern
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }

        return if (board.none { it.isEmpty() }) "Draw" else null
    }

    fun findBestMove(board: List<String>): Int {
        // Check if computer can win
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val testBoard = board.toMutableList()
                testBoard[i] = "O"
                if (checkWinner(testBoard) == "O") return i
            }
        }

        // Block player from winning
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val testBoard = board.toMutableList()
                testBoard[i] = "X"
                if (checkWinner(testBoard) == "X") return i
            }
        }

        // Take center if available
        if (board[4].isEmpty()) return 4

        // Take corners
        val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
        if (corners.isNotEmpty()) return corners.random()

        // Take any available space
        return board.indices.first { board[it].isEmpty() }
    }

    // Computer move
    LaunchedEffect(currentPlayer, gameMode, winner) {
        if (gameMode == GameMode.VS_COMPUTER && currentPlayer == "O" && winner == null && !isComputerThinking) {
            isComputerThinking = true
            delay(500) // Thinking delay
            val move = findBestMove(board)
            if (board[move].isEmpty()) {
                board = board.toMutableList().apply { this[move] = "O" }
                winner = checkWinner(board)
                if (winner == "O") scoreO++
                else if (winner == null) currentPlayer = "X"
            }
            isComputerThinking = false
        }
    }

    fun onCellClick(index: Int) {
        if (board[index].isEmpty() && winner == null && !isComputerThinking) {
            if (gameMode == GameMode.VS_COMPUTER && currentPlayer == "O") return

            board = board.toMutableList().apply { this[index] = currentPlayer }
            winner = checkWinner(board)
            if (winner != null) {
                when (winner) {
                    "X" -> scoreX++
                    "O" -> scoreO++
                }
            } else {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
            }
        }
    }

    fun resetGame() {
        board = List(9) { "" }
        currentPlayer = "X"
        winner = null
        isComputerThinking = false
    }

    fun backToMenu() {
        resetGame()
        scoreX = 0
        scoreO = 0
        gameMode = GameMode.MENU
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic Tac Toe") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (gameMode == GameMode.MENU) navController.popBackStack()
                        else backToMenu()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        }
    ) { padding ->
        if (gameMode == GameMode.MENU) {
            // Mode Selection Menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Select Game Mode",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Play with Computer
                Button(
                    onClick = { gameMode = GameMode.VS_COMPUTER },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Play with Computer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("You vs AI", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Play Alone (2 Player)
                Button(
                    onClick = { gameMode = GameMode.TWO_PLAYER },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Play Alone", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("2 Players on same device", fontSize = 14.sp)
                    }
                }
            }
        } else {
            // Game Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode indicator
                Text(
                    text = if (gameMode == GameMode.VS_COMPUTER) "VS Computer" else "2 Players",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scoreboard
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreCard(
                        player = if (gameMode == GameMode.VS_COMPUTER) "You (X)" else "Player X",
                        score = scoreX,
                        color = Color(0xFF2196F3)
                    )
                    ScoreCard(
                        player = if (gameMode == GameMode.VS_COMPUTER) "Computer (O)" else "Player O",
                        score = scoreO,
                        color = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status text
                Text(
                    text = when {
                        winner == "Draw" -> "It's a Draw!"
                        winner != null -> {
                            if (gameMode == GameMode.VS_COMPUTER) {
                                if (winner == "X") "You Win!" else "Computer Wins!"
                            } else {
                                "Player $winner Wins!"
                            }
                        }
                        isComputerThinking -> "Computer is thinking..."
                        else -> {
                            if (gameMode == GameMode.VS_COMPUTER) {
                                if (currentPlayer == "X") "Your Turn" else "Computer's Turn"
                            } else {
                                "Player $currentPlayer's Turn"
                            }
                        }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        winner == "X" -> Color(0xFF2196F3)
                        winner == "O" -> Color(0xFFE91E63)
                        currentPlayer == "X" -> Color(0xFF2196F3)
                        else -> Color(0xFFE91E63)
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Game Board
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        for (row in 0..2) {
                            Row {
                                for (col in 0..2) {
                                    val index = row * 3 + col
                                    TicTacToeCell(
                                        value = board[index],
                                        onClick = { onCellClick(index) },
                                        enabled = !isComputerThinking,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { resetGame() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("New Game")
                    }

                    OutlinedButton(
                        onClick = { backToMenu() }
                    ) {
                        Text("Change Mode")
                    }
                }
            }
        }
    }
}

@Composable
fun TicTacToeCell(
    value: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF2196F3), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = if (value == "X") Color(0xFF2196F3) else Color(0xFFE91E63)
        )
    }
}

@Composable
fun ScoreCard(player: String, score: Int, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = player,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 14.sp
            )
            Text(
                text = score.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
