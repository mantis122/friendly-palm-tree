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
    private val initialX = x
    private val initialY = y
    var claimed = false

    /**
     * Place the fragment where the defeated boss fell. Coordinates are the
     * desired center of the dropped fragment.
     */
    fun dropAt(centerX: Float, centerY: Float) {
        bounds.offsetTo(centerX - bounds.width() * 0.5f, centerY - bounds.height() * 0.5f)
        claimed = false
    }

    /** Restore the reward to its clean Chapter 1 starting state. */
    fun reset() {
        bounds.offsetTo(initialX, initialY)
        claimed = false
    }

    override val interactionBounds: RectF
        get() = RectF(bounds).apply { inset(-20f, -20f) }

    override fun interactionText(): String = if (claimed) {
        "Only a few silver-painted cardboard flakes remain."
    } else {
        "A curved piece of silver cardboard lies where the Blanket King fell."
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
