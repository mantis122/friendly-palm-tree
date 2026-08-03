package com.example.backyardrealms.game.world

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.theme.ImaginationTheme

data class LandmarkAppearance(val color: Int, val label: String, val message: String)

class Landmark(
    val id: String,
    val collisionBounds: RectF,
    private val real: LandmarkAppearance,
    private val fantasy: LandmarkAppearance
) : Interactable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var theme: ImaginationTheme = ImaginationTheme.REAL
    override val interactionBounds: RectF = RectF(collisionBounds).apply { inset(-15f, -15f) }
    private fun appearance() = if (theme == ImaginationTheme.REAL) real else fantasy
    override fun interactionText(): String = appearance().message
    fun draw(canvas: Canvas) {
        val a = appearance()
        paint.style = Paint.Style.FILL
        paint.color = a.color
        canvas.drawRect(collisionBounds, paint)
        paint.color = 0xDDFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        canvas.drawText(a.label, collisionBounds.centerX(), collisionBounds.centerY() + 3f, paint)
    }
}
