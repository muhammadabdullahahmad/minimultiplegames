package com.example.minimultiplegames.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.minimultiplegames.games.HomeScreen
import com.example.minimultiplegames.games.WaterSortScreen
import com.example.minimultiplegames.games.TicTacToeScreen
import com.example.minimultiplegames.games.RollingDiceScreen
import com.example.minimultiplegames.games.HangmanScreen
import com.example.minimultiplegames.games.snake.SnakeMenuScreen
import com.example.minimultiplegames.games.snake.SnakeGameScreen
import com.example.minimultiplegames.games.snake.HighScoresScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(NavRoutes.WaterSort.route) {
            WaterSortScreen(navController = navController)
        }
        composable(NavRoutes.TicTacToe.route) {
            TicTacToeScreen(navController = navController)
        }
        composable(NavRoutes.RollingDice.route) {
            RollingDiceScreen(navController = navController)
        }
        composable(NavRoutes.Hangman.route) {
            HangmanScreen(navController = navController)
        }
        composable(NavRoutes.Snake.route) {
            SnakeMenuScreen(navController = navController)
        }
        composable(
            route = NavRoutes.SnakeGame.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "classic"
            SnakeGameScreen(navController = navController, gameMode = mode)
        }
        composable(NavRoutes.HighScores.route) {
            HighScoresScreen(navController = navController)
        }
    }
}
