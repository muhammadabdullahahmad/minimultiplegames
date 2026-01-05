package com.example.minimultiplegames.games.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class HighScoreItem(
    val mode: String,
    val score: Int,
    val emoji: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighScoresScreen(navController: NavController) {
    val context = LocalContext.current
    var highScores by remember { mutableStateOf(HighScoreManager.getAllHighScores(context)) }
    var showResetDialog by remember { mutableStateOf(false) }

    val scoreItems = listOf(
        HighScoreItem("Classic", highScores["Classic"] ?: 0, "🐍", Color(0xFF4CAF50)),
        HighScoreItem("Box", highScores["Box"] ?: 0, "📦", Color(0xFF2196F3)),
        HighScoreItem("Tunnel", highScores["Tunnel"] ?: 0, "🚇", Color(0xFF9C27B0)),
        HighScoreItem("Apartment", highScores["Apartment"] ?: 0, "🏠", Color(0xFFFF5722))
    )

    val totalScore = scoreItems.sumOf { it.score }
    val bestMode = scoreItems.maxByOrNull { it.score }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Scores?") },
            text = { Text("This will permanently delete all your high scores. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HighScoreManager.resetAllScores(context)
                        highScores = HighScoreManager.getAllHighScores(context)
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("High Scores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Reset Scores")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFD700)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8E1),
                            Color(0xFFFFECB3)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Trophy header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Points",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = totalScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    if (bestMode != null && bestMode.score > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Best Mode: ${bestMode.emoji} ${bestMode.mode}",
                            fontSize = 14.sp,
                            color = bestMode.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Scores by Mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scoreItems.sortedByDescending { it.score }) { item ->
                    HighScoreCard(item = item, isTopScore = item == bestMode && item.score > 0)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Motivational text
            if (totalScore == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Play some games to set your first high scores!",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun HighScoreCard(
    item: HighScoreItem,
    isTopScore: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTopScore) item.color.copy(alpha = 0.2f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTopScore) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji and mode name
            Text(
                text = item.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.mode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.color
                    )
                    if (isTopScore) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Top Score",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (item.score > 0) {
                    Text(
                        text = when {
                            item.score >= 500 -> "Master"
                            item.score >= 200 -> "Expert"
                            item.score >= 100 -> "Intermediate"
                            else -> "Beginner"
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Score
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.score.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.score > 0) item.color else Color.Gray
                )
                Text(
                    text = "points",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
