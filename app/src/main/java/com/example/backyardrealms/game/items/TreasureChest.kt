package com.example.backyardrealms.game.items

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class TreasureChest(
    val id: String,
    x: Float,
    y: Float,
    val contents: List<Pair<String, Int>>
) : Interactable {
    val bounds = RectF(x, y, x + 30f, y + 22f)
    var opened = false
    override val interactionBounds: RectF get() = RectF(bounds.left - 22f, bounds.top - 22f, bounds.right + 22f, bounds.bottom + 22f)
    override fun interactionText(): String = if (opened) "The chest is empty." else "You open the chest."

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = if (opened) 0xFF76522F.toInt() else 0xFFA86B2B.toInt()
        canvas.drawRoundRect(bounds, 3f, 3f, paint)
        paint.color = 0xFFE8C24D.toInt()
        canvas.drawRect(bounds.centerX() - 2f, bounds.top + 8f, bounds.centerX() + 2f, bounds.bottom - 4f, paint)
        if (opened) {
            paint.color = 0xFF3A2617.toInt()
            canvas.drawRect(bounds.left + 3f, bounds.top + 3f, bounds.right - 3f, bounds.top + 8f, paint)
        }
    }
}
