package com.example.minimultiplegames.games

/**
 * HANGMAN GAME - Instructions
 *
 * Difficulty Levels:
 * - EASY: 3 letter words, 10 attempts
 * - MEDIUM: 4-5 letter words, 7 attempts
 * - HARD: 6+ letter words, 5 attempts
 */

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Difficulty levels
enum class Difficulty(val label: String, val emoji: String, val maxAttempts: Int, val color: Color) {
    EASY("Easy", "😊", 10, Color(0xFF4CAF50)),
    MEDIUM("Medium", "🤔", 7, Color(0xFFFF9800)),
    HARD("Hard", "😈", 5, Color(0xFFF44336))
}

// Word categories with difficulty-based words
data class WordCategory(val name: String, val easyWords: List<String>, val mediumWords: List<String>, val hardWords: List<String>)

private val categories = listOf(
    WordCategory(
        "Animals",
        easyWords = listOf("CAT", "DOG", "COW", "PIG", "HEN", "ANT", "BEE", "BAT", "OWL", "FOX"),
        mediumWords = listOf("LION", "BEAR", "FROG", "DUCK", "FISH", "BIRD", "TIGER", "HORSE", "MOUSE", "SHEEP"),
        hardWords = listOf("ELEPHANT", "GIRAFFE", "DOLPHIN", "PENGUIN", "BUTTERFLY", "CROCODILE", "KANGAROO", "SQUIRREL")
    ),
    WordCategory(
        "Colors",
        easyWords = listOf("RED", "BLUE", "PINK", "GOLD", "GRAY"),
        mediumWords = listOf("GREEN", "BLACK", "WHITE", "BROWN", "CREAM"),
        hardWords = listOf("ORANGE", "YELLOW", "PURPLE", "SILVER", "VIOLET", "MAGENTA")
    ),
    WordCategory(
        "Fruits",
        easyWords = listOf("FIG", "PEAR", "PLUM", "KIWI", "LIME"),
        mediumWords = listOf("APPLE", "GRAPE", "MANGO", "PEACH", "LEMON", "MELON", "BERRY"),
        hardWords = listOf("BANANA", "CHERRY", "ORANGE", "PINEAPPLE", "STRAWBERRY", "WATERMELON")
    ),
    WordCategory(
        "Food",
        easyWords = listOf("EGG", "PIE", "JAM", "HAM", "NUT", "PEA", "BUN", "ICE"),
        mediumWords = listOf("CAKE", "PIZZA", "BREAD", "RICE", "SOUP", "MILK", "CANDY", "TOAST"),
        hardWords = listOf("CHEESE", "COOKIE", "BURGER", "SANDWICH", "CHOCOLATE", "SPAGHETTI")
    ),
    WordCategory(
        "Things",
        easyWords = listOf("CAR", "BUS", "CUP", "BAG", "BOX", "BED", "PEN", "KEY", "TOY", "HAT"),
        mediumWords = listOf("BALL", "BOOK", "CLOCK", "DOOR", "CHAIR", "TABLE", "LAMP", "PHONE"),
        hardWords = listOf("COMPUTER", "KEYBOARD", "BICYCLE", "UMBRELLA", "BACKPACK", "TELEVISION")
    ),
    WordCategory(
        "Nature",
        easyWords = listOf("SUN", "SKY", "SEA", "MUD", "DEW", "FOG", "ICE", "BAY"),
        mediumWords = listOf("RAIN", "SNOW", "WIND", "MOON", "LEAF", "ROCK", "TREE", "LAKE"),
        hardWords = listOf("CLOUD", "RIVER", "OCEAN", "MOUNTAIN", "RAINBOW", "WATERFALL", "SUNSHINE")
    ),
    WordCategory(
        "Body",
        easyWords = listOf("EYE", "EAR", "ARM", "LEG", "TOE", "LIP", "JAW", "RIB"),
        mediumWords = listOf("HAND", "FOOT", "HEAD", "NOSE", "NECK", "HAIR", "FACE", "KNEE"),
        hardWords = listOf("MOUTH", "FINGER", "SHOULDER", "STOMACH", "ELBOW", "TONGUE")
    )
)

// Primary color for the game theme
private val HangmanPurple = Color(0xFF9C27B0)
private val HangmanPurpleLight = Color(0xFFF3E5F5)

// Game state enum
enum class GameState {
    SELECT_DIFFICULTY, PLAYING, WON, LOST
}

// Get words based on difficulty
private fun getWordsForDifficulty(category: WordCategory, difficulty: Difficulty): List<String> {
    return when (difficulty) {
        Difficulty.EASY -> category.easyWords
        Difficulty.MEDIUM -> category.mediumWords
        Difficulty.HARD -> category.hardWords
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanScreen(navController: NavController) {
    // Game state variables
    var difficulty by remember { mutableStateOf(Difficulty.EASY) }
    var currentCategory by remember { mutableStateOf(categories.random()) }
    var targetWord by remember { mutableStateOf("") }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableIntStateOf(0) }
    var gameState by remember { mutableStateOf(GameState.SELECT_DIFFICULTY) }

    // Calculate remaining attempts based on difficulty
    val maxWrongGuesses = difficulty.maxAttempts
    val remainingAttempts = maxWrongGuesses - wrongGuesses

    // Get incorrect guessed letters
    val incorrectLetters = guessedLetters.filter { it !in targetWord }

    // Check win/lose conditions
    LaunchedEffect(guessedLetters, wrongGuesses, gameState) {
        if (gameState == GameState.PLAYING) {
            when {
                targetWord.isNotEmpty() && targetWord.all { it in guessedLetters } -> gameState = GameState.WON
                wrongGuesses >= maxWrongGuesses -> gameState = GameState.LOST
            }
        }
    }

    // Function to handle letter guess
    fun guessLetter(letter: Char) {
        if (gameState != GameState.PLAYING) return
        if (letter in guessedLetters) return

        guessedLetters = guessedLetters + letter

        if (letter !in targetWord) {
            wrongGuesses++
        }
    }

    // Function to start game with selected difficulty
    fun startGame(selectedDifficulty: Difficulty) {
        difficulty = selectedDifficulty
        currentCategory = categories.random()
        val words = getWordsForDifficulty(currentCategory, selectedDifficulty)
        targetWord = words.random()
        guessedLetters = emptySet()
        wrongGuesses = 0
        gameState = GameState.PLAYING
    }

    // Function to go back to difficulty selection
    fun selectDifficulty() {
        gameState = GameState.SELECT_DIFFICULTY
        guessedLetters = emptySet()
        wrongGuesses = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hangman", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (gameState == GameState.SELECT_DIFFICULTY) {
                            navController.popBackStack()
                        } else {
                            selectDifficulty()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HangmanPurple
                )
            )
        }
    ) { padding ->
        when (gameState) {
            GameState.SELECT_DIFFICULTY -> {
                // Difficulty Selection Screen
                DifficultySelectionScreen(
                    modifier = Modifier.padding(padding),
                    onDifficultySelected = { startGame(it) }
                )
            }
            else -> {
                // Game Screen
                GamePlayScreen(
                    modifier = Modifier.padding(padding),
                    difficulty = difficulty,
                    currentCategory = currentCategory,
                    targetWord = targetWord,
                    guessedLetters = guessedLetters,
                    wrongGuesses = wrongGuesses,
                    maxWrongGuesses = maxWrongGuesses,
                    remainingAttempts = remainingAttempts,
                    incorrectLetters = incorrectLetters,
                    gameState = gameState,
                    onLetterGuess = { guessLetter(it) },
                    onPlayAgain = { startGame(difficulty) },
                    onChangeDifficulty = { selectDifficulty() }
                )
            }
        }
    }
}

@Composable
fun DifficultySelectionScreen(
    modifier: Modifier = Modifier,
    onDifficultySelected: (Difficulty) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Difficulty",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = HangmanPurple
        )

        Spacer(modifier = Modifier.height(32.dp))

        Difficulty.entries.forEach { difficulty ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onDifficultySelected(difficulty) },
                colors = CardDefaults.cardColors(containerColor = difficulty.color),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = difficulty.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = difficulty.label,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (difficulty) {
                                    Difficulty.EASY -> "3 letter words"
                                    Difficulty.MEDIUM -> "4-5 letter words"
                                    Difficulty.HARD -> "6+ letter words"
                                },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = "${difficulty.maxAttempts} tries",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun GamePlayScreen(
    modifier: Modifier = Modifier,
    difficulty: Difficulty,
    currentCategory: WordCategory,
    targetWord: String,
    guessedLetters: Set<Char>,
    wrongGuesses: Int,
    maxWrongGuesses: Int,
    remainingAttempts: Int,
    incorrectLetters: List<Char>,
    gameState: GameState,
    onLetterGuess: (Char) -> Unit,
    onPlayAgain: () -> Unit,
    onChangeDifficulty: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Difficulty & Category Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = difficulty.color),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${difficulty.emoji} ${difficulty.label}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = HangmanPurple),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = currentCategory.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Attempts: $remainingAttempts/$maxWrongGuesses",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (remainingAttempts <= 2) Color.Red else Color.Black
            )
            Text(
                text = when (gameState) {
                    GameState.WON -> "🎉 YOU WIN!"
                    GameState.LOST -> "💀 GAME OVER"
                    else -> "Guess the word!"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when (gameState) {
                    GameState.WON -> Color(0xFF4CAF50)
                    GameState.LOST -> Color.Red
                    else -> HangmanPurple
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hangman Figure
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = HangmanPurpleLight),
            shape = RoundedCornerShape(16.dp)
        ) {
            HangmanFigure(wrongGuesses = wrongGuesses, maxWrongGuesses = maxWrongGuesses)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Word Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    targetWord.forEach { char ->
                        Text(
                            text = if (char in guessedLetters || gameState == GameState.LOST)
                                char.toString()
                            else "_",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState == GameState.LOST && char !in guessedLetters)
                                Color.Red
                            else HangmanPurple
                        )
                    }
                }

                if (guessedLetters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Guessed: ${guessedLetters.sorted().joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Game Over or Keyboard
        if (gameState == GameState.WON || gameState == GameState.LOST) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (gameState == GameState.WON)
                        "Congratulations! 🎉"
                    else "The word was: $targetWord",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (gameState == GameState.WON) Color(0xFF4CAF50) else Color.Red
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = difficulty.color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Play Again (${difficulty.label})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onChangeDifficulty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Change Difficulty", fontSize = 16.sp, color = HangmanPurple)
                }
            }
        } else {
            LetterKeyboard(
                guessedLetters = guessedLetters,
                targetWord = targetWord,
                onLetterClick = onLetterGuess,
                modifier = Modifier.weight(1f)
            )
        }

        // Wrong letters display
        if (incorrectLetters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Wrong: ${incorrectLetters.sorted().joinToString(", ")}",
                fontSize = 14.sp,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HangmanFigure(wrongGuesses: Int, maxWrongGuesses: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val strokeWidth = 6f
        val color = Color(0xFF5D4037)
        val bodyColor = Color.Black

        val centerX = size.width / 2
        val baseY = size.height - 10
        val topY = 20f

        // Draw gallows (always visible)
        drawLine(color = color, start = Offset(centerX - 70, baseY), end = Offset(centerX + 70, baseY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(centerX - 45, baseY), end = Offset(centerX - 45, topY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(centerX - 45, topY), end = Offset(centerX + 25, topY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(centerX + 25, topY), end = Offset(centerX + 25, topY + 20), strokeWidth = 4f, cap = StrokeCap.Round)

        val headRadius = 18f
        val headCenter = Offset(centerX + 25, topY + 20 + headRadius)
        val bodyTop = headCenter.y + headRadius
        val bodyBottom = bodyTop + 45

        // Calculate which parts to show based on wrong guesses and max attempts
        val partsToShow = when (maxWrongGuesses) {
            10 -> wrongGuesses // Easy: 10 parts for 10 attempts
            7 -> (wrongGuesses * 10 / 7).coerceAtMost(10) // Medium: scale to 10 parts
            5 -> (wrongGuesses * 10 / 5).coerceAtMost(10) // Hard: scale to 10 parts
            else -> wrongGuesses
        }

        // 1. Head
        if (partsToShow >= 1) {
            drawCircle(color = bodyColor, radius = headRadius, center = headCenter, style = Stroke(width = 4f))
        }
        // 2. Body
        if (partsToShow >= 2) {
            drawLine(color = bodyColor, start = Offset(centerX + 25, bodyTop), end = Offset(centerX + 25, bodyBottom), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        // 3. Left arm
        if (partsToShow >= 3) {
            drawLine(color = bodyColor, start = Offset(centerX + 25, bodyTop + 10), end = Offset(centerX, bodyTop + 30), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        // 4. Right arm
        if (partsToShow >= 4) {
            drawLine(color = bodyColor, start = Offset(centerX + 25, bodyTop + 10), end = Offset(centerX + 50, bodyTop + 30), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        // 5. Left leg
        if (partsToShow >= 5) {
            drawLine(color = bodyColor, start = Offset(centerX + 25, bodyBottom), end = Offset(centerX + 5, bodyBottom + 30), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        // 6. Right leg
        if (partsToShow >= 6) {
            drawLine(color = bodyColor, start = Offset(centerX + 25, bodyBottom), end = Offset(centerX + 45, bodyBottom + 30), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        // 7. Left hand
        if (partsToShow >= 7) {
            drawCircle(color = bodyColor, radius = 4f, center = Offset(centerX - 2, bodyTop + 32))
        }
        // 8. Right hand
        if (partsToShow >= 8) {
            drawCircle(color = bodyColor, radius = 4f, center = Offset(centerX + 52, bodyTop + 32))
        }
        // 9. Left eye
        if (partsToShow >= 9) {
            drawCircle(color = bodyColor, radius = 3f, center = Offset(headCenter.x - 6, headCenter.y - 4))
        }
        // 10. Right eye + sad mouth
        if (partsToShow >= 10) {
            drawCircle(color = bodyColor, radius = 3f, center = Offset(headCenter.x + 6, headCenter.y - 4))
            drawLine(color = bodyColor, start = Offset(headCenter.x - 6, headCenter.y + 8), end = Offset(headCenter.x + 6, headCenter.y + 8), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun LetterKeyboard(
    guessedLetters: Set<Char>,
    targetWord: String,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').toList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        items(alphabet) { letter ->
            val isGuessed = letter in guessedLetters
            val isCorrect = letter in targetWord

            val backgroundColor = when {
                !isGuessed -> HangmanPurple
                isCorrect -> Color(0xFF4CAF50)
                else -> Color(0xFFE57373)
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
                    .then(if (!isGuessed) Modifier.clickable { onLetterClick(letter) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
