package com.example.minimultiplegames.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Bottle(
    val id: Int,
    val colors: MutableList<Color>,
    val maxCapacity: Int = 4
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterSortScreen(navController: NavController) {
    val gameColors = listOf(
        Color(0xFFE53935), // Red
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFFFB300), // Yellow
        Color(0xFF8E24AA), // Purple
        Color(0xFFFF7043)  // Orange
    )

    var bottles by remember { mutableStateOf(generateLevel(gameColors)) }
    var selectedBottle by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var isWon by remember { mutableStateOf(false) }

    fun checkWin(): Boolean {
        return bottles.all { bottle ->
            bottle.colors.isEmpty() ||
            (bottle.colors.size == bottle.maxCapacity &&
             bottle.colors.all { it == bottle.colors.first() })
        }
    }

    fun pourWater(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val fromBottle = bottles[fromIndex]
        val toBottle = bottles[toIndex]

        if (fromBottle.colors.isEmpty()) return
        if (toBottle.colors.size >= toBottle.maxCapacity) return

        val colorToPour = fromBottle.colors.last()

        // Can pour if target is empty or has same color on top
        if (toBottle.colors.isNotEmpty() && toBottle.colors.last() != colorToPour) return

        // Pour as many of the same color as possible
        val newFromColors = fromBottle.colors.toMutableList()
        val newToColors = toBottle.colors.toMutableList()

        while (newFromColors.isNotEmpty() &&
               newFromColors.last() == colorToPour &&
               newToColors.size < toBottle.maxCapacity) {
            newFromColors.removeAt(newFromColors.lastIndex)
            newToColors.add(colorToPour)
        }

        bottles = bottles.mapIndexed { index, bottle ->
            when (index) {
                fromIndex -> Bottle(bottle.id, newFromColors)
                toIndex -> Bottle(bottle.id, newToColors)
                else -> bottle
            }
        }

        moves++
        isWon = checkWin()
    }

    fun onBottleClick(index: Int) {
        if (isWon) return

        if (selectedBottle == null) {
            if (bottles[index].colors.isNotEmpty()) {
                selectedBottle = index
            }
        } else {
            pourWater(selectedBottle!!, index)
            selectedBottle = null
        }
    }

    fun nextLevel() {
        level++
        bottles = generateLevel(gameColors, level)
        moves = 0
        isWon = false
        selectedBottle = null
    }

    fun restartLevel() {
        bottles = generateLevel(gameColors, level)
        moves = 0
        isWon = false
        selectedBottle = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Sort") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00BCD4)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE0F7FA))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Level: $level",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )
                Text(
                    "Moves: $moves",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedBottle != null) "Tap another bottle to pour" else "Tap a bottle to select",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottles Grid - First Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottles.take(4).forEachIndexed { index, bottle ->
                    BottleView(
                        bottle = bottle,
                        isSelected = selectedBottle == index,
                        onClick = { onBottleClick(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottles Grid - Second Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottles.drop(4).forEachIndexed { index, bottle ->
                    BottleView(
                        bottle = bottle,
                        isSelected = selectedBottle == index + 4,
                        onClick = { onBottleClick(index + 4) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isWon) {
                Text(
                    text = "Congratulations!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "Completed in $moves moves",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                ) {
                    Text("Next Level", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Restart Button
            Button(
                onClick = { restartLevel() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restart", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun BottleView(
    bottle: Bottle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (isSelected) -20f else 0f,
        animationSpec = tween(200),
        label = "bottle_offset"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .width(60.dp)
                .height(160.dp)
        ) {
            val bottleWidth = size.width
            val bottleHeight = size.height
            val neckHeight = bottleHeight * 0.15f
            val neckWidth = bottleWidth * 0.4f
            val bodyHeight = bottleHeight - neckHeight
            val cornerRadius = 12f

            // Draw bottle outline (glass effect)
            drawRoundRect(
                color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFB0BEC5),
                topLeft = Offset((bottleWidth - neckWidth) / 2, 0f),
                size = Size(neckWidth, neckHeight + 10),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFB0BEC5),
                topLeft = Offset(0f, neckHeight),
                size = Size(bottleWidth, bodyHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // Draw bottle interior (white/transparent)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset((bottleWidth - neckWidth) / 2 + 4, 4f),
                size = Size(neckWidth - 8, neckHeight + 6),
                cornerRadius = CornerRadius(6f, 6f)
            )

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(4f, neckHeight + 4),
                size = Size(bottleWidth - 8, bodyHeight - 8),
                cornerRadius = CornerRadius(cornerRadius - 4, cornerRadius - 4)
            )

            // Draw liquid layers
            val liquidAreaHeight = bodyHeight - 16
            val layerHeight = liquidAreaHeight / bottle.maxCapacity

            bottle.colors.forEachIndexed { index, color ->
                val yPos = neckHeight + bodyHeight - 8 - (index + 1) * layerHeight
                drawRoundRect(
                    color = color,
                    topLeft = Offset(8f, yPos),
                    size = Size(bottleWidth - 16, layerHeight),
                    cornerRadius = if (index == 0) CornerRadius(cornerRadius - 8, cornerRadius - 8) else CornerRadius(0f, 0f)
                )
            }
        }
    }
}

private fun generateLevel(colors: List<Color>, level: Int = 1): List<Bottle> {
    val numColors = minOf(4 + (level - 1) / 2, colors.size) // Start with 4 colors, add more each 2 levels
    val selectedColors = colors.take(numColors)

    // Create 4 layers of each color
    val allLayers = selectedColors.flatMap { color -> List(4) { color } }.shuffled()

    // Distribute to bottles
    val filledBottles = allLayers.chunked(4).mapIndexed { index, layers ->
        Bottle(index, layers.toMutableList())
    }

    // Add 2 empty bottles
    val emptyBottles = listOf(
        Bottle(filledBottles.size, mutableListOf()),
        Bottle(filledBottles.size + 1, mutableListOf())
    )

    return filledBottles + emptyBottles
}
