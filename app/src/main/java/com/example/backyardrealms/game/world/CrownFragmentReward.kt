package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class CrownFragmentReward(
    val id: String,
    x: Float,
    y: Float
) : Interactable {
    val bounds = RectF(x, y, x + 30f, y + 24f)
    var claimed = false

    override val interactionBounds: RectF
        get() = RectF(bounds).apply { inset(-20f, -20f) }

    override fun interactionText(): String = if (claimed) {
        "Only a few silver-painted cardboard flakes remain."
    } else {
        "A curved piece of silver cardboard glows beneath the fallen crown."
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (claimed) return
        val path = Path().apply {
            moveTo(bounds.left, bounds.bottom)
            lineTo(bounds.left + 5f, bounds.top + 7f)
            lineTo(bounds.left + 12f, bounds.top + 13f)
            lineTo(bounds.left + 18f, bounds.top + 3f)
            lineTo(bounds.right, bounds.bottom)
            close()
        }
        paint.color = 0xFFDDE6F2.toInt()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFF7F91B2.toInt()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }
}
