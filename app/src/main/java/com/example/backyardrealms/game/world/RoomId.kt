package com.example.backyardrealms.game.world

enum class RoomId {
    BACKYARD,
    GOBLIN_FORT_ENTRY,
    GOBLIN_FORT_TREASURE;

    companion object {
        fun from(value: String?): RoomId = runCatching { valueOf(value ?: BACKYARD.name) }
            .getOrDefault(BACKYARD)
    }
}
