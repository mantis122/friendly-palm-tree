package com.example.backyardrealms.game.items

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.inventory.ItemCatalog

class WorldPickup(
    val id: String,
    val itemId: String,
    x: Float,
    y: Float,
    private val catalog: ItemCatalog
) {
    val bounds = RectF(x, y, x + 18f, y + 18f)
    var collected = false
    private var phase = 0f

    fun update(dt: Float) { phase += dt }
    fun draw(canvas: Canvas, paint: Paint) {
        if (collected) return
        val definition = catalog[itemId] ?: return
        val bob = kotlin.math.sin(phase * 3.2f) * 2f
        paint.color = 0x88000000.toInt()
        canvas.drawOval(bounds.left + 3f, bounds.bottom - 2f, bounds.right - 3f, bounds.bottom + 2f, paint)
        paint.color = definition.iconColor
        canvas.drawRoundRect(bounds.left + 2f, bounds.top + bob, bounds.right - 2f, bounds.bottom + bob, 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(bounds.left + 2f, bounds.top + bob, bounds.right - 2f, bounds.bottom + bob, 4f, 4f, paint)
        paint.style = Paint.Style.FILL
    }
}
