package com.example.backyardrealms.engine.events

sealed interface GameEvent {
    data class ThemeChanged(val themeName: String) : GameEvent
    data class InteractionStarted(val targetId: String) : GameEvent
    data class PlayerMoved(val x: Float, val y: Float) : GameEvent
    data object AttackStarted : GameEvent
    data class DamageDealt(val sourceId: String, val targetId: String, val amount: Int) : GameEvent
    data class EntityDefeated(val entityId: String) : GameEvent
    data class PlayerHealthChanged(val current: Int, val maximum: Int) : GameEvent
    data object PlayerRespawned : GameEvent
}

class EventBus {
    private val listeners = mutableListOf<(GameEvent) -> Unit>()
    var lastEvent: GameEvent? = null
        private set

    fun subscribe(listener: (GameEvent) -> Unit) { listeners += listener }
    fun post(event: GameEvent) {
        lastEvent = event
        listeners.toList().forEach { it(event) }
    }
}
