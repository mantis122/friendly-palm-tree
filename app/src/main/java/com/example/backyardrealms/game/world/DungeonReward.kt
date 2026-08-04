package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class DungeonReward(
    val id: String,
    x: Float,
    y: Float
) : Interactable {
    val bounds = RectF(x, y, x + 34f, y + 24f)
    var claimed = false

    override val interactionBounds: RectF
        get() = RectF(bounds).apply { inset(-20f, -20f) }

    override fun interactionText(): String = if (claimed) {
        "The old toy box is empty, but the painted moon still glows."
    } else {
        "A toy box painted like a royal treasure chest."
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = if (claimed) 0xFF59483A.toInt() else 0xFF7A4D31.toInt()
        canvas.drawRoundRect(bounds, 4f, 4f, paint)
        paint.color = 0xFFE1B94E.toInt()
        canvas.drawRect(bounds.left + 3f, bounds.centerY() - 2f, bounds.right - 3f, bounds.centerY() + 2f, paint)
        if (!claimed) canvas.drawCircle(bounds.centerX(), bounds.centerY(), 4f, paint)
    }
}
