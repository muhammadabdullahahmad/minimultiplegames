package com.example.minimultiplegames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollingDiceScreen(navController: NavController) {
    var diceCount by remember { mutableIntStateOf(1) }
    var diceValues by remember { mutableStateOf(listOf(1)) }
    var isRolling by remember { mutableStateOf(false) }
    var rollCount by remember { mutableIntStateOf(0) }
    var totalSum by remember { mutableIntStateOf(1) }

    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f * 3 else 0f,
        animationSpec = tween(
            durationMillis = if (isRolling) 800 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "dice_rotation"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            repeat(10) {
                diceValues = List(diceCount) { Random.nextInt(1, 7) }
                delay(80)
            }
            diceValues = List(diceCount) { Random.nextInt(1, 7) }
            totalSum = diceValues.sum()
            isRolling = false
        }
    }

    fun addDice() {
        if (diceCount < 10) {
            diceCount++
            diceValues = diceValues + Random.nextInt(1, 7)
            totalSum = diceValues.sum()
        }
    }

    fun removeDice() {
        if (diceCount > 1) {
            diceCount--
            diceValues = diceValues.dropLast(1)
            totalSum = diceValues.sum()
        }
    }

    fun rollDice() {
        if (!isRolling) {
            isRolling = true
            rollCount++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rolling Dice") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
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
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rolls: $rollCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total: $totalSum",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dice Count Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Number of Dice",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { removeDice() },
                            enabled = diceCount > 1 && !isRolling,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (diceCount > 1) Color(0xFF4CAF50) else Color.Gray,
                                    CircleShape
                                )
                        ) {
                            Text(
                                text = "−",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = diceCount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp),
                            color = Color(0xFF4CAF50)
                        )

                        IconButton(
                            onClick = { addDice() },
                            enabled = diceCount < 10 && !isRolling,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (diceCount < 10) Color(0xFF4CAF50) else Color.Gray,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dice Grid
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (diceCount == 1) 1 else if (diceCount <= 2) 2 else if (diceCount <= 6) 3 else 4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    itemsIndexed(diceValues) { index, value ->
                        Box(
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            DiceView(
                                value = value,
                                rotation = if (isRolling) rotation else 0f,
                                size = when {
                                    diceCount <= 2 -> 120
                                    diceCount <= 4 -> 100
                                    diceCount <= 6 -> 85
                                    else -> 70
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Roll Button
            Button(
                onClick = { rollDice() },
                enabled = !isRolling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isRolling) "Rolling..." else "Roll ${if (diceCount > 1) "$diceCount Dice" else "Dice"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reset Button
            TextButton(
                onClick = {
                    rollCount = 0
                    diceValues = List(diceCount) { 1 }
                    totalSum = diceCount
                }
            ) {
                Text("Reset Count", color = Color.Gray)
            }
        }
    }
}

@Composable
fun DiceView(
    value: Int,
    rotation: Float,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .rotate(rotation)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(3.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        DiceFaceNew(value = value, size = size)
    }
}

@Composable
fun DiceFaceNew(value: Int, size: Int) {
    val dotSize = (size / 5).dp
    val padding = (size / 6).dp

    val dotPositions = when (value) {
        1 -> listOf(Pair(1, 1))
        2 -> listOf(Pair(0, 0), Pair(2, 2))
        3 -> listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
        4 -> listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        5 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 1), Pair(2, 0), Pair(2, 2))
        6 -> listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(2, 0), Pair(2, 1), Pair(2, 2))
        else -> emptyList()
    }

    Box(
        modifier = Modifier
            .size((size - 16).dp)
            .padding(4.dp)
    ) {
        dotPositions.forEach { (row, col) ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(
                        x = (col * (size - 16) / 3 + (size - 16) / 12).dp,
                        y = (row * (size - 16) / 3 + (size - 16) / 12).dp
                    )
                    .background(Color(0xFF4CAF50), CircleShape)
            )
        }
    }
}
