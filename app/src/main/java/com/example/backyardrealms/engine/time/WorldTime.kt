package com.example.backyardrealms.engine.time

enum class WorldTime(val overlayColor: Int) {
    MORNING(0x10FFD98A),
    AFTERNOON(0x00000000),
    EVENING(0x185A3B78),
    NIGHT(0x40304A78);

    fun next(): WorldTime = entries[(ordinal + 1) % entries.size]
}
