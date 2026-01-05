package com.example.minimultiplegames.games

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Bolt(
    val id: Int,
    val nuts: MutableList<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutsAndBoltsScreen(navController: NavController) {
    val nutColors = listOf(
        Color(0xFFE53935), // Red
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFFFB300)  // Yellow
    )

    var bolts by remember { mutableStateOf(generatePuzzle(nutColors)) }
    var selectedBoltIndex by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var isWon by remember { mutableStateOf(false) }

    fun checkWin(): Boolean {
        return bolts.all { bolt ->
            bolt.nuts.isEmpty() || bolt.nuts.all { it == bolt.nuts.first() }
        }
    }

    fun onBoltClick(index: Int) {
        if (isWon) return

        if (selectedBoltIndex == null) {
            // Select bolt if it has nuts
            if (bolts[index].nuts.isNotEmpty()) {
                selectedBoltIndex = index
            }
        } else {
            // Try to move nut
            val sourceBolt = bolts[selectedBoltIndex!!]
            val targetBolt = bolts[index]

            if (selectedBoltIndex != index && sourceBolt.nuts.isNotEmpty()) {
                val nutToMove = sourceBolt.nuts.last()
                // Can move if target is empty or has same color on top, and has space
                if (targetBolt.nuts.size < 4 &&
                    (targetBolt.nuts.isEmpty() || targetBolt.nuts.last() == nutToMove)) {
                    bolts = bolts.map { bolt ->
                        when (bolt.id) {
                            sourceBolt.id -> Bolt(bolt.id, bolt.nuts.dropLast(1).toMutableList())
                            targetBolt.id -> Bolt(bolt.id, (bolt.nuts + nutToMove).toMutableList())
                            else -> bolt
                        }
                    }
                    moves++
                    isWon = checkWin()
                }
            }
            selectedBoltIndex = null
        }
    }

    fun nextLevel() {
        level++
        bolts = generatePuzzle(nutColors)
        moves = 0
        isWon = false
        selectedBoltIndex = null
    }

    fun resetLevel() {
        bolts = generatePuzzle(nutColors)
        moves = 0
        isWon = false
        selectedBoltIndex = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuts & Bolts Puzzle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF795548)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8E1))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Level: $level", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Moves: $moves", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (selectedBoltIndex != null) "Tap another bolt to move nut" else "Tap a bolt to select",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bolts Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bolts.take(3).forEachIndexed { index, bolt ->
                    BoltView(
                        bolt = bolt,
                        isSelected = selectedBoltIndex == index,
                        onClick = { onBoltClick(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bolts.drop(3).forEachIndexed { index, bolt ->
                    BoltView(
                        bolt = bolt,
                        isSelected = selectedBoltIndex == index + 3,
                        onClick = { onBoltClick(index + 3) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isWon) {
                Text(
                    text = "Congratulations!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548))
                ) {
                    Text("Next Level")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { resetLevel() }) {
                Text("Reset Level")
            }
        }
    }
}

@Composable
fun BoltView(
    bolt: Bolt,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Bolt head
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF5D4037) else Color(0xFF8D6E63))
                .border(3.dp, if (isSelected) Color(0xFFFFEB3B) else Color(0xFF6D4C41), CircleShape)
        )

        // Bolt shaft with nuts
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(160.dp)
                .background(Color(0xFF8D6E63))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                bolt.nuts.reversed().forEach { color ->
                    NutView(color = color)
                }
            }
        }

        // Base
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 15.dp)
                .background(Color(0xFF5D4037), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun NutView(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 35.dp)
            .offset(x = (-15).dp)
            .background(color, RoundedCornerShape(6.dp))
            .border(2.dp, color.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
    )
}

private fun generatePuzzle(colors: List<Color>): List<Bolt> {
    // Create 4 nuts of each color
    val allNuts = colors.flatMap { color -> List(4) { color } }.shuffled()

    // Distribute to 4 bolts, leave 2 empty
    return listOf(
        Bolt(0, allNuts.subList(0, 4).toMutableList()),
        Bolt(1, allNuts.subList(4, 8).toMutableList()),
        Bolt(2, allNuts.subList(8, 12).toMutableList()),
        Bolt(3, allNuts.subList(12, 16).toMutableList()),
        Bolt(4, mutableListOf()),
        Bolt(5, mutableListOf())
    )
}
