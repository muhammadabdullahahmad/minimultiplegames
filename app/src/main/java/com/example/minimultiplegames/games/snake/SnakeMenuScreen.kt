package com.example.minimultiplegames.games.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minimultiplegames.navigation.NavRoutes

data class GameMode(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeMenuScreen(navController: NavController) {
    val context = LocalContext.current

    val gameModes = listOf(
        GameMode(
            "classic",
            "Classic",
            "No walls - wrap around edges",
            "🐍",
            Color(0xFF4CAF50)
        ),
        GameMode(
            "box",
            "Box",
            "Walls on all sides - don't hit them!",
            "📦",
            Color(0xFF2196F3)
        ),
        GameMode(
            "tunnel",
            "Tunnel",
            "Navigate through narrow passages",
            "🚇",
            Color(0xFF9C27B0)
        ),
        GameMode(
            "apartment",
            "Apartment",
            "Maze with rooms - find your way!",
            "🏠",
            Color(0xFFFF5722)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake Game") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.HighScores.route) }) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "High Scores",
                            tint = Color(0xFFFFD700)
                        )
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
                .background(Color(0xFFE8F5E9))
                .padding(16.dp)
        ) {
            Text(
                text = "Select Game Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gameModes) { mode ->
                    val highScore = HighScoreManager.getHighScore(context, mode.id)
                    GameModeCard(
                        mode = mode,
                        highScore = highScore,
                        onClick = {
                            navController.navigate(NavRoutes.SnakeGame.createRoute(mode.id))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // High Scores Button
            OutlinedButton(
                onClick = { navController.navigate(NavRoutes.HighScores.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View All High Scores")
            }
        }
    }
}

@Composable
fun GameModeCard(
    mode: GameMode,
    highScore: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(mode.color.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = mode.emoji,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = mode.color
                )
                Text(
                    text = mode.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // High Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Best",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = highScore.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = mode.color
                )
            }
        }
    }
}
