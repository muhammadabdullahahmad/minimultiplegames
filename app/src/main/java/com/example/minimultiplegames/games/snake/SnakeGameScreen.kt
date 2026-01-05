package com.example.minimultiplegames.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class Position(val x: Int, val y: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(navController: NavController, gameMode: String) {
    val context = LocalContext.current
    val gridSize = 20
    val walls = remember { generateWalls(gameMode, gridSize) }

    var snake by remember { mutableStateOf(listOf(Position(10, 10))) }
    var food by remember { mutableStateOf(generateFood(snake, walls, gridSize)) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(HighScoreManager.getHighScore(context, gameMode)) }
    var isNewHighScore by remember { mutableStateOf(false) }

    val modeColor = when (gameMode) {
        "classic" -> Color(0xFF4CAF50)
        "box" -> Color(0xFF2196F3)
        "tunnel" -> Color(0xFF9C27B0)
        "apartment" -> Color(0xFFFF5722)
        else -> Color(0xFF4CAF50)
    }

    val modeName = when (gameMode) {
        "classic" -> "Classic"
        "box" -> "Box"
        "tunnel" -> "Tunnel"
        "apartment" -> "Apartment"
        else -> "Classic"
    }

    // Game loop
    LaunchedEffect(isPaused, isGameOver) {
        while (!isPaused && !isGameOver) {
            delay((150L - (score / 5) * 5L).coerceAtLeast(80L)) // Speed increases with score

            val head = snake.first()
            var newHead = when (direction) {
                Direction.UP -> Position(head.x, head.y - 1)
                Direction.DOWN -> Position(head.x, head.y + 1)
                Direction.LEFT -> Position(head.x - 1, head.y)
                Direction.RIGHT -> Position(head.x + 1, head.y)
            }

            // Handle wrapping for classic mode
            if (gameMode == "classic") {
                newHead = Position(
                    (newHead.x + gridSize) % gridSize,
                    (newHead.y + gridSize) % gridSize
                )
            }

            // Check collisions
            val hitWall = newHead.x < 0 || newHead.x >= gridSize ||
                    newHead.y < 0 || newHead.y >= gridSize ||
                    walls.contains(newHead)
            val hitSelf = snake.contains(newHead)

            if ((hitWall && gameMode != "classic") || hitSelf) {
                isGameOver = true
                if (HighScoreManager.saveHighScore(context, gameMode, score)) {
                    isNewHighScore = true
                    highScore = score
                }
            } else {
                // Move snake
                val newSnake = mutableListOf(newHead)
                newSnake.addAll(snake)

                // Check if ate food
                if (newHead == food) {
                    score += 10
                    food = generateFood(newSnake, walls, gridSize)
                } else {
                    newSnake.removeAt(newSnake.lastIndex)
                }

                snake = newSnake
            }
        }
    }

    fun resetGame() {
        snake = listOf(Position(10, 10))
        food = generateFood(snake, walls, gridSize)
        direction = Direction.RIGHT
        isGameOver = false
        isPaused = true
        score = 0
        isNewHighScore = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake - $modeName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = modeColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1B1B1B)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Score", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("High Score", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = highScore.toString(),
                        color = Color(0xFFFFD700),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Game board
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                direction = if (x > 0 && direction != Direction.LEFT) {
                                    Direction.RIGHT
                                } else if (x < 0 && direction != Direction.RIGHT) {
                                    Direction.LEFT
                                } else direction
                            } else {
                                direction = if (y > 0 && direction != Direction.UP) {
                                    Direction.DOWN
                                } else if (y < 0 && direction != Direction.DOWN) {
                                    Direction.UP
                                } else direction
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val cellSize = size.width / gridSize

                    // Draw walls
                    walls.forEach { wall ->
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(wall.x * cellSize, wall.y * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }

                    // Draw grid lines (subtle)
                    for (i in 0..gridSize) {
                        drawLine(
                            color = Color(0xFF3D3D3D),
                            start = Offset(i * cellSize, 0f),
                            end = Offset(i * cellSize, size.height),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0xFF3D3D3D),
                            start = Offset(0f, i * cellSize),
                            end = Offset(size.width, i * cellSize),
                            strokeWidth = 1f
                        )
                    }

                    // Draw food
                    drawCircle(
                        color = Color(0xFFE53935),
                        radius = cellSize / 2 - 2,
                        center = Offset(
                            food.x * cellSize + cellSize / 2,
                            food.y * cellSize + cellSize / 2
                        )
                    )

                    // Draw snake
                    snake.forEachIndexed { index, pos ->
                        val isHead = index == 0
                        drawRoundRect(
                            color = if (isHead) modeColor else modeColor.copy(alpha = 0.7f),
                            topLeft = Offset(pos.x * cellSize + 1, pos.y * cellSize + 1),
                            size = Size(cellSize - 2, cellSize - 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }

                // Overlay for game over or paused
                if (isGameOver || isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isGameOver) {
                                Text(
                                    text = "Game Over!",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isNewHighScore) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "NEW HIGH SCORE!",
                                        color = Color(0xFFFFD700),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Score: $score",
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { resetGame() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = modeColor
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Play Again")
                                }
                            } else {
                                Text(
                                    text = "Swipe to move",
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { isPaused = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = modeColor
                                    )
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            if (!isGameOver && !isPaused) {
                DirectionalControls(
                    onDirectionChange = { newDir ->
                        // Prevent 180 degree turns
                        val isValidMove = when (newDir) {
                            Direction.UP -> direction != Direction.DOWN
                            Direction.DOWN -> direction != Direction.UP
                            Direction.LEFT -> direction != Direction.RIGHT
                            Direction.RIGHT -> direction != Direction.LEFT
                        }
                        if (isValidMove) direction = newDir
                    },
                    buttonColor = modeColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pause/Resume button
            if (!isGameOver) {
                Button(
                    onClick = { isPaused = !isPaused },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) modeColor else Color.Gray
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(if (isPaused) "Start Game" else "Pause")
                }
            }
        }
    }
}

@Composable
fun DirectionalControls(
    onDirectionChange: (Direction) -> Unit,
    buttonColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up button
        Button(
            onClick = { onDirectionChange(Direction.UP) },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            modifier = Modifier.size(60.dp)
        ) {
            Text("^", fontSize = 24.sp)
        }

        Row {
            // Left button
            Button(
                onClick = { onDirectionChange(Direction.LEFT) },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.size(60.dp)
            ) {
                Text("<", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(60.dp))

            // Right button
            Button(
                onClick = { onDirectionChange(Direction.RIGHT) },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.size(60.dp)
            ) {
                Text(">", fontSize = 24.sp)
            }
        }

        // Down button
        Button(
            onClick = { onDirectionChange(Direction.DOWN) },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            modifier = Modifier.size(60.dp)
        ) {
            Text("v", fontSize = 24.sp)
        }
    }
}

private fun generateFood(snake: List<Position>, walls: Set<Position>, gridSize: Int): Position {
    var food: Position
    do {
        food = Position(Random.nextInt(gridSize), Random.nextInt(gridSize))
    } while (snake.contains(food) || walls.contains(food))
    return food
}

private fun generateWalls(mode: String, gridSize: Int): Set<Position> {
    return when (mode) {
        "box" -> {
            // Walls on all edges
            val walls = mutableSetOf<Position>()
            for (i in 0 until gridSize) {
                walls.add(Position(i, 0))
                walls.add(Position(i, gridSize - 1))
                walls.add(Position(0, i))
                walls.add(Position(gridSize - 1, i))
            }
            walls
        }
        "tunnel" -> {
            // Horizontal tunnels
            val walls = mutableSetOf<Position>()
            // Top and bottom walls
            for (i in 0 until gridSize) {
                walls.add(Position(i, 0))
                walls.add(Position(i, gridSize - 1))
            }
            // Middle horizontal walls with gaps
            for (i in 0 until gridSize) {
                if (i !in 8..12) {
                    walls.add(Position(i, 5))
                    walls.add(Position(i, 14))
                }
            }
            // Some vertical barriers
            for (i in 6..13) {
                if (i != 10) {
                    walls.add(Position(3, i))
                    walls.add(Position(16, i))
                }
            }
            walls
        }
        "apartment" -> {
            // Maze-like structure
            val walls = mutableSetOf<Position>()
            // Outer walls
            for (i in 0 until gridSize) {
                walls.add(Position(i, 0))
                walls.add(Position(i, gridSize - 1))
                walls.add(Position(0, i))
                walls.add(Position(gridSize - 1, i))
            }
            // Room 1 (top-left)
            for (i in 1..7) {
                if (i != 4) walls.add(Position(i, 7))
            }
            for (i in 1..6) {
                if (i != 3) walls.add(Position(7, i))
            }
            // Room 2 (top-right)
            for (i in 12..18) {
                if (i != 15) walls.add(Position(i, 7))
            }
            for (i in 1..6) {
                if (i != 3) walls.add(Position(12, i))
            }
            // Room 3 (bottom-left)
            for (i in 1..7) {
                if (i != 4) walls.add(Position(i, 12))
            }
            for (i in 13..18) {
                if (i != 16) walls.add(Position(7, i))
            }
            // Room 4 (bottom-right)
            for (i in 12..18) {
                if (i != 15) walls.add(Position(i, 12))
            }
            for (i in 13..18) {
                if (i != 16) walls.add(Position(12, i))
            }
            // Center corridors
            walls.add(Position(9, 9))
            walls.add(Position(10, 9))
            walls.add(Position(9, 10))
            walls.add(Position(10, 10))
            walls
        }
        else -> emptySet() // Classic mode - no walls
    }
}
