package com.example.minimultiplegames.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object WaterSort : NavRoutes("water_sort")
    object TicTacToe : NavRoutes("tic_tac_toe")
    object RollingDice : NavRoutes("rolling_dice")
    object Hangman : NavRoutes("hangman")
    object Snake : NavRoutes("snake")
    object SnakeGame : NavRoutes("snake_game/{mode}") {
        fun createRoute(mode: String) = "snake_game/$mode"
    }
    object HighScores : NavRoutes("high_scores")
    object MemoryGame : NavRoutes("memory_game")
    object Game2048 : NavRoutes("game_2048")
    object RockPaperScissors : NavRoutes("rock_paper_scissors")
    object SimonSays : NavRoutes("simon_says")
}
