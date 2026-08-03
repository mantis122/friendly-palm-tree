package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class Landmark(
    val collisionBounds: RectF,
    private val color: Int,
    private val label: String,
    private val message: String
) : Interactable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override val interactionBounds: RectF = RectF(collisionBounds).apply { inset(-15f, -15f) }

    override fun interactionText(): String = message

    fun draw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRect(collisionBounds, paint)
        paint.color = 0xDDFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        canvas.drawText(label, collisionBounds.centerX(), collisionBounds.centerY() + 3f, paint)
    }
}
