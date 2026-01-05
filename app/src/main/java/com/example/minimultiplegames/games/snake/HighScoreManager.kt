package com.example.minimultiplegames.games.snake

import android.content.Context
import android.content.SharedPreferences

object HighScoreManager {
    private const val PREFS_NAME = "snake_high_scores"
    private const val KEY_CLASSIC = "high_score_classic"
    private const val KEY_BOX = "high_score_box"
    private const val KEY_TUNNEL = "high_score_tunnel"
    private const val KEY_APARTMENT = "high_score_apartment"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getHighScore(context: Context, mode: String): Int {
        val key = when (mode) {
            "classic" -> KEY_CLASSIC
            "box" -> KEY_BOX
            "tunnel" -> KEY_TUNNEL
            "apartment" -> KEY_APARTMENT
            else -> KEY_CLASSIC
        }
        return getPrefs(context).getInt(key, 0)
    }

    fun saveHighScore(context: Context, mode: String, score: Int): Boolean {
        val currentHigh = getHighScore(context, mode)
        if (score > currentHigh) {
            val key = when (mode) {
                "classic" -> KEY_CLASSIC
                "box" -> KEY_BOX
                "tunnel" -> KEY_TUNNEL
                "apartment" -> KEY_APARTMENT
                else -> KEY_CLASSIC
            }
            getPrefs(context).edit().putInt(key, score).apply()
            return true // New high score!
        }
        return false
    }

    fun getAllHighScores(context: Context): Map<String, Int> {
        val prefs = getPrefs(context)
        return mapOf(
            "Classic" to prefs.getInt(KEY_CLASSIC, 0),
            "Box" to prefs.getInt(KEY_BOX, 0),
            "Tunnel" to prefs.getInt(KEY_TUNNEL, 0),
            "Apartment" to prefs.getInt(KEY_APARTMENT, 0)
        )
    }

    fun resetAllScores(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
