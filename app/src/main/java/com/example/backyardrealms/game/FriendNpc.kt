package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable

class FriendNpc(x: Float, y: Float) : Interactable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(x, y, x + 14f, y + 18f)

    override val interactionBounds: RectF = RectF(body).apply { inset(-22f, -22f) }

    override fun interactionText(): String = "Want to play?"

    fun draw(canvas: Canvas) {
        paint.color = 0xFF9A4770.toInt()
        canvas.drawRect(body, paint)
        paint.color = 0xFFF1C89A.toInt()
        canvas.drawCircle(body.centerX(), body.top + 3f, 5f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8f
        canvas.drawText("MIA", body.centerX(), body.bottom + 10f, paint)
    }
}
