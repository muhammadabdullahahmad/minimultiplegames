package com.example.minimultiplegames.games

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs
import kotlin.random.Random

private val GameBackground = Color(0xFFBBADA0)
private val EmptyTile = Color(0xFFCDC1B4)
private val GameOrange = Color(0xFFFF9800)

private fun getTileColor(value: Int): Color = when (value) {
    2 -> Color(0xFFEEE4DA)
    4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179)
    16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F)
    64 -> Color(0xFFF65E3B)
    128 -> Color(0xFFEDCF72)
    256 -> Color(0xFFEDCC61)
    512 -> Color(0xFFEDC850)
    1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E)
    else -> Color(0xFF3C3A32)
}

private fun getTextColor(value: Int): Color = if (value <= 4) Color(0xFF776E65) else Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Game2048Screen(navController: NavController) {
    var board by remember { mutableStateOf(initBoard()) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var gameState by remember { mutableStateOf("playing") } // playing, won, lost

    fun resetGame() {
        board = initBoard()
        score = 0
        gameState = "playing"
    }

    fun move(direction: Direction) {
        if (gameState == "lost") return

        val (newBoard, points, moved) = performMove(board, direction)
        if (moved) {
            board = addRandomTile(newBoard)
            score += points
            if (score > bestScore) bestScore = score

            if (board.flatten().any { it == 2048 } && gameState != "won") {
                gameState = "won"
            }
            if (!canMove(board)) {
                gameState = "lost"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2048", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Default.Refresh, "New Game", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GameOrange)
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
                ScoreCard("SCORE", score)
                ScoreCard("BEST", bestScore)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game board
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GameBackground)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > 50 || abs(y) > 50) {
                                when {
                                    abs(x) > abs(y) && x > 0 -> move(Direction.RIGHT)
                                    abs(x) > abs(y) && x < 0 -> move(Direction.LEFT)
                                    abs(y) > abs(x) && y > 0 -> move(Direction.DOWN)
                                    abs(y) > abs(x) && y < 0 -> move(Direction.UP)
                                }
                            }
                        }
                    }
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    board.forEach { row ->
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { value ->
                                TileCell(
                                    value = value,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Game over overlay
                if (gameState == "lost") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Game Over!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { resetGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = GameOrange)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                // Win overlay
                if (gameState == "won") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEDC22E).copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "🎉 You Win!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { gameState = "playing" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Continue", color = GameOrange)
                                }
                                Button(
                                    onClick = { resetGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GameOrange)
                                ) {
                                    Text("New Game")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Swipe to move tiles.\nMatch numbers to reach 2048!",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ScoreCard(label: String, score: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GameBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFFEEE4DA),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TileCell(value: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(if (value == 0) EmptyTile else getTileColor(value)),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                fontSize = when {
                    value < 100 -> 28.sp
                    value < 1000 -> 22.sp
                    else -> 18.sp
                },
                fontWeight = FontWeight.Bold,
                color = getTextColor(value)
            )
        }
    }
}

private enum class Direction { UP, DOWN, LEFT, RIGHT }

private fun initBoard(): List<List<Int>> {
    val board = MutableList(4) { MutableList(4) { 0 } }
    repeat(2) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) for (j in 0..3) if (board[i][j] == 0) emptyCells.add(i to j)
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            board[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }
    return board
}

private fun addRandomTile(board: List<List<Int>>): List<List<Int>> {
    val newBoard = board.map { it.toMutableList() }
    val emptyCells = mutableListOf<Pair<Int, Int>>()
    for (i in 0..3) for (j in 0..3) if (newBoard[i][j] == 0) emptyCells.add(i to j)
    if (emptyCells.isNotEmpty()) {
        val (r, c) = emptyCells.random()
        newBoard[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
    }
    return newBoard
}

private fun performMove(board: List<List<Int>>, direction: Direction): Triple<List<List<Int>>, Int, Boolean> {
    val newBoard = board.map { it.toMutableList() }
    var points = 0
    var moved = false

    fun slideLine(line: MutableList<Int>): Int {
        var linePoints = 0
        // Remove zeros
        val nonZero = line.filter { it != 0 }.toMutableList()
        // Merge
        var i = 0
        while (i < nonZero.size - 1) {
            if (nonZero[i] == nonZero[i + 1]) {
                nonZero[i] *= 2
                linePoints += nonZero[i]
                nonZero.removeAt(i + 1)
            }
            i++
        }
        // Pad with zeros
        while (nonZero.size < 4) nonZero.add(0)
        // Check if changed
        for (j in 0..3) {
            if (line[j] != nonZero[j]) moved = true
            line[j] = nonZero[j]
        }
        return linePoints
    }

    when (direction) {
        Direction.LEFT -> {
            for (i in 0..3) points += slideLine(newBoard[i])
        }
        Direction.RIGHT -> {
            for (i in 0..3) {
                newBoard[i].reverse()
                points += slideLine(newBoard[i])
                newBoard[i].reverse()
            }
        }
        Direction.UP -> {
            for (j in 0..3) {
                val col = mutableListOf(newBoard[0][j], newBoard[1][j], newBoard[2][j], newBoard[3][j])
                points += slideLine(col)
                for (i in 0..3) newBoard[i][j] = col[i]
            }
        }
        Direction.DOWN -> {
            for (j in 0..3) {
                val col = mutableListOf(newBoard[3][j], newBoard[2][j], newBoard[1][j], newBoard[0][j])
                points += slideLine(col)
                for (i in 0..3) newBoard[3 - i][j] = col[i]
            }
        }
    }

    return Triple(newBoard, points, moved)
}

private fun canMove(board: List<List<Int>>): Boolean {
    // Check for empty cells
    if (board.flatten().any { it == 0 }) return true
    // Check for adjacent equal cells
    for (i in 0..3) {
        for (j in 0..3) {
            val current = board[i][j]
            if (j < 3 && current == board[i][j + 1]) return true
            if (i < 3 && current == board[i + 1][j]) return true
        }
    }
    return false
}
