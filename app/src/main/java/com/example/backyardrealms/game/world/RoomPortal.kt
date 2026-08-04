package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class RoomPortal(
    val id: String,
    val bounds: RectF,
    private val label: String,
    private val message: String
) : Interactable {
    override val interactionBounds: RectF
        get() = RectF(bounds).apply { inset(-18f, -18f) }

    override fun interactionText(): String = message

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = 0xFF3E2B24.toInt()
        canvas.drawRect(bounds, paint)
        paint.color = 0xFFD8B36A.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRect(bounds, paint)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9f
        canvas.drawText(label, bounds.centerX(), bounds.top - 5f, paint)
    }
}
