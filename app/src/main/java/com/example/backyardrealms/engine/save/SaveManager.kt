package com.example.backyardrealms.engine.save

import android.content.Context
import com.example.backyardrealms.engine.time.WorldTime
import com.example.backyardrealms.game.theme.ImaginationTheme

data class GameSave(
    val playerX: Float,
    val playerY: Float,
    val theme: ImaginationTheme,
    val worldTime: WorldTime,
    val inventory: String,
    val equippedItemId: String?,
    val questFlags: String,
    val openedChests: String,
    val collectedPickups: String
)

class SaveManager(context: Context) {
    private val prefs = context.getSharedPreferences("backyard_realms_save", Context.MODE_PRIVATE)
    fun load(): GameSave = GameSave(
        prefs.getFloat("player_x", 460f),
        prefs.getFloat("player_y", 390f),
        runCatching { ImaginationTheme.valueOf(prefs.getString("theme", ImaginationTheme.REAL.name)!!) }.getOrDefault(ImaginationTheme.REAL),
        runCatching { WorldTime.valueOf(prefs.getString("world_time", WorldTime.AFTERNOON.name)!!) }.getOrDefault(WorldTime.AFTERNOON),
        prefs.getString("inventory", "") ?: "",
        prefs.getString("equipped_item", null),
        prefs.getString("quest_flags", "") ?: "",
        prefs.getString("opened_chests", "") ?: "",
        prefs.getString("collected_pickups", "") ?: ""
    )

    fun save(save: GameSave) {
        prefs.edit()
            .putFloat("player_x", save.playerX).putFloat("player_y", save.playerY)
            .putString("theme", save.theme.name).putString("world_time", save.worldTime.name)
            .putString("inventory", save.inventory).putString("equipped_item", save.equippedItemId)
            .putString("quest_flags", save.questFlags).putString("opened_chests", save.openedChests)
            .putString("collected_pickups", save.collectedPickups).apply()
    }
}
