package com.example.backyardrealms.engine.save

import android.content.Context
import com.example.backyardrealms.engine.time.WorldTime
import com.example.backyardrealms.game.theme.ImaginationTheme

data class GameSave(val playerX: Float, val playerY: Float, val theme: ImaginationTheme, val worldTime: WorldTime)

class SaveManager(context: Context) {
    private val prefs = context.getSharedPreferences("backyard_realms_save", Context.MODE_PRIVATE)
    fun load(): GameSave = GameSave(
        prefs.getFloat("player_x", 460f),
        prefs.getFloat("player_y", 390f),
        runCatching { ImaginationTheme.valueOf(prefs.getString("theme", ImaginationTheme.REAL.name)!!) }.getOrDefault(ImaginationTheme.REAL),
        runCatching { WorldTime.valueOf(prefs.getString("world_time", WorldTime.AFTERNOON.name)!!) }.getOrDefault(WorldTime.AFTERNOON)
    )
    fun save(save: GameSave) {
        prefs.edit().putFloat("player_x", save.playerX).putFloat("player_y", save.playerY)
            .putString("theme", save.theme.name).putString("world_time", save.worldTime.name).apply()
    }
}
