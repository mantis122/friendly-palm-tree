package com.example.backyardrealms.game.world

enum class RoomId {
    BACKYARD,
    GOBLIN_FORT_ENTRY,
    GOBLIN_FORT_TREASURE,
    GOBLIN_FORT_BOSS;

    companion object {
        fun from(value: String?): RoomId = runCatching { valueOf(value ?: BACKYARD.name) }
            .getOrDefault(BACKYARD)
    }
}
