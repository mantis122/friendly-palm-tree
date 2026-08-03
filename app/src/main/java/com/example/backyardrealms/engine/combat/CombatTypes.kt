package com.example.backyardrealms.engine.combat

import android.graphics.RectF

data class DamageHit(
    val sourceId: String,
    val amount: Int,
    val knockbackX: Float,
    val knockbackY: Float,
    val attackId: Int
)

interface Damageable {
    val hurtBounds: RectF
    val health: HealthComponent
    val isInvulnerable: Boolean
    fun receiveHit(hit: DamageHit): Boolean
}
