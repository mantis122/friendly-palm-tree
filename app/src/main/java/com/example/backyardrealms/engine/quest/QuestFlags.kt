package com.example.backyardrealms.engine.quest

class QuestFlags {
    private val flags = linkedSetOf<String>()
    fun set(flag: String) { flags += flag }
    fun clear(flag: String) { flags -= flag }
    fun has(flag: String): Boolean = flag in flags
    fun all(): Set<String> = flags.toSet()
    fun restore(serialized: String) { flags.clear(); flags += serialized.split(',').filter { it.isNotBlank() } }
    fun serialize(): String = flags.joinToString(",")
}
