package com.example.backyardrealms.engine.combat

class HealthComponent(val maximum: Int) {
    var current: Int = maximum
        private set

    val isAlive: Boolean get() = current > 0

    fun damage(amount: Int): Int {
        if (amount <= 0 || !isAlive) return 0
        val before = current
        current = (current - amount).coerceAtLeast(0)
        return before - current
    }

    fun heal(amount: Int): Int {
        if (amount <= 0 || current >= maximum) return 0
        val before = current
        current = (current + amount).coerceAtMost(maximum)
        return current - before
    }

    fun restore() {
        current = maximum
    }
}
