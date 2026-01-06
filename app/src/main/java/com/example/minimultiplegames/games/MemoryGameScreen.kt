package com.example.minimultiplegames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private val MemoryBlue = Color(0xFF3F51B5)
private val MemoryGreen = Color(0xFF4CAF50)

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

enum class MemoryDifficulty(val label: String, val pairs: Int, val columns: Int, val color: Color) {
    EASY("Easy", 6, 3, Color(0xFF4CAF50)),
    MEDIUM("Medium", 8, 4, Color(0xFFFF9800)),
    HARD("Hard", 12, 4, Color(0xFFF44336))
}

private val allEmojis = listOf(
    "🍎", "🍊", "🍋", "🍇", "🍉", "🍓", "🫐", "🍑",
    "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
    "⭐", "🌙", "☀️", "🌈", "❤️", "💎", "🎈", "🎁"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(navController: NavController) {
    var gameState by remember { mutableStateOf("select") } // select, playing, won
    var difficulty by remember { mutableStateOf(MemoryDifficulty.EASY) }
    var cards by remember { mutableStateOf(listOf<MemoryCard>()) }
    var firstCard by remember { mutableStateOf<MemoryCard?>(null) }
    var secondCard by remember { mutableStateOf<MemoryCard?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var matches by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }

    fun startGame(selectedDifficulty: MemoryDifficulty) {
        difficulty = selectedDifficulty
        val emojis = allEmojis.shuffled().take(selectedDifficulty.pairs)
        val cardPairs = (emojis + emojis).shuffled()
        cards = cardPairs.mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }
        firstCard = null
        secondCard = null
        moves = 0
        matches = 0
        isChecking = false
        gameState = "playing"
    }

    fun flipCard(card: MemoryCard) {
        if (isChecking || card.isFlipped || card.isMatched) return

        cards = cards.map {
            if (it.id == card.id) it.copy(isFlipped = true) else it
        }

        if (firstCard == null) {
            firstCard = card
        } else {
            secondCard = card
            moves++
            isChecking = true
        }
    }

    LaunchedEffect(secondCard) {
        if (secondCard != null && firstCard != null) {
            delay(800)
            if (firstCard!!.emoji == secondCard!!.emoji) {
                cards = cards.map {
                    if (it.id == firstCard!!.id || it.id == secondCard!!.id) {
                        it.copy(isMatched = true)
                    } else it
                }
                matches++
                if (matches == difficulty.pairs) {
                    gameState = "won"
                }
            } else {
                cards = cards.map {
                    if (it.id == firstCard!!.id || it.id == secondCard!!.id) {
                        it.copy(isFlipped = false)
                    } else it
                }
            }
            firstCard = null
            secondCard = null
            isChecking = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Game", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (gameState == "select") {
                            navController.popBackStack()
                        } else {
                            gameState = "select"
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MemoryBlue)
            )
        }
    ) { padding ->
        when (gameState) {
            "select" -> {
                DifficultySelection(
                    modifier = Modifier.padding(padding),
                    onSelect = { startGame(it) }
                )
            }
            "playing" -> {
                GameBoard(
                    modifier = Modifier.padding(padding),
                    cards = cards,
                    difficulty = difficulty,
                    moves = moves,
                    matches = matches,
                    onCardClick = { flipCard(it) }
                )
            }
            "won" -> {
                WinScreen(
                    modifier = Modifier.padding(padding),
                    moves = moves,
                    onPlayAgain = { startGame(difficulty) },
                    onChangeDifficulty = { gameState = "select" }
                )
            }
        }
    }
}

@Composable
private fun DifficultySelection(
    modifier: Modifier = Modifier,
    onSelect: (MemoryDifficulty) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🧠",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Memory Game",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MemoryBlue
        )
        Text(
            text = "Match all the pairs!",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        MemoryDifficulty.entries.forEach { diff ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(diff) },
                colors = CardDefaults.cardColors(containerColor = diff.color),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diff.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${diff.pairs} pairs",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBoard(
    modifier: Modifier = Modifier,
    cards: List<MemoryCard>,
    difficulty: MemoryDifficulty,
    moves: Int,
    matches: Int,
    onCardClick: (MemoryCard) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Moves", moves.toString(), MemoryBlue)
            StatCard("Matches", "$matches/${difficulty.pairs}", MemoryGreen)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(difficulty.columns),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(cards) { card ->
                MemoryCardItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun MemoryCardItem(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(400),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                card.isMatched -> MemoryGreen
                rotation > 90f -> Color.White
                else -> MemoryBlue
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation > 90f) {
                Text(
                    text = card.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            } else {
                Text(
                    text = "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun WinScreen(
    modifier: Modifier = Modifier,
    moves: Int,
    onPlayAgain: () -> Unit,
    onChangeDifficulty: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You Won!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MemoryGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Completed in $moves moves",
            fontSize = 18.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MemoryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onChangeDifficulty,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Change Difficulty", fontSize = 18.sp, color = MemoryBlue)
        }
    }
}
