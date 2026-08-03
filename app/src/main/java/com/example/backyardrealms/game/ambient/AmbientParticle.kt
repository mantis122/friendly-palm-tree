package com.example.backyardrealms.game.ambient

import android.graphics.Canvas
import android.graphics.Paint
import com.example.backyardrealms.engine.entity.EffectEntity
import kotlin.math.sin

class AmbientParticle(
    override val id: String,
    private val worldWidth: Float,
    private val worldHeight: Float,
    private var x: Float,
    private var y: Float,
    private val speed: Float,
    private val phase: Float
) : EffectEntity() {
    override var active = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var age = phase
    override fun update(dt: Float) {
        age += dt
        x += speed * dt
        y += sin(age * 1.8f) * 5f * dt
        if (x > worldWidth + 8f) x = -8f
        if (y < 24f) y = worldHeight - 24f
        if (y > worldHeight - 24f) y = 24f
    }
    override fun draw(canvas: Canvas) {
        paint.color = 0xBBFFF1A8.toInt()
        canvas.drawCircle(x, y, 1.8f, paint)
        canvas.drawCircle(x - 3f, y - 1f, 1.5f, paint)
    }
}
