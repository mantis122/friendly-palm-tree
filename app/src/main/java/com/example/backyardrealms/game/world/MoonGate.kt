package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class MoonGate(
    override val id: String,
    val bounds: RectF
) : Interactable {
    var unlocked: Boolean = false

    override val interactionBounds: RectF
        get() = RectF(bounds).apply { inset(-22f, -22f) }

    override fun interactionText(): String = if (unlocked) {
        "The Moon Gate stands open."
    } else {
        "A tiny brass lock holds the Moon Gate shut."
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (unlocked) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = 0x8879D7FF.toInt()
            canvas.drawRect(bounds, paint)
            paint.style = Paint.Style.FILL
            return
        }
        paint.color = 0xFF28324C.toInt()
        canvas.drawRect(bounds, paint)
        paint.color = 0xFF8FA7D8.toInt()
        for (x in listOf(bounds.left + 8f, bounds.centerX(), bounds.right - 8f)) {
            canvas.drawRect(x - 2f, bounds.top, x + 2f, bounds.bottom, paint)
        }
        paint.color = 0xFFE7C85B.toInt()
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), 4f, paint)
    }
}
