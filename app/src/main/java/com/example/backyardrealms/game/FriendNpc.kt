package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.theme.ImaginationTheme

class FriendNpc(x: Float, y: Float) : Interactable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(x, y, x + 14f, y + 18f)
    var theme: ImaginationTheme = ImaginationTheme.REAL
    override val interactionBounds: RectF = RectF(body).apply { inset(-22f, -22f) }
    override fun interactionText(): String = if (theme == ImaginationTheme.REAL) "Want to play? Meet me inside the fort!" else "Sir Rowan says the Moon Goblins stole the yard's crown!"
    fun draw(canvas: Canvas) {
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF9A4770.toInt() else 0xFF3857A8.toInt()
        canvas.drawRect(body, paint)
        paint.color = 0xFFF1C89A.toInt()
        canvas.drawCircle(body.centerX(), body.top + 3f, 5f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8f
        canvas.drawText(if (theme == ImaginationTheme.REAL) "MIA" else "SIR MIA", body.centerX(), body.bottom + 10f, paint)
    }
}
