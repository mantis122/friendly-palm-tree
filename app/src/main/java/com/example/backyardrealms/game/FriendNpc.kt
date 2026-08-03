package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.entity.Behavior
import com.example.backyardrealms.engine.entity.CharacterEntity
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.theme.ImaginationTheme
import kotlin.math.sin

class FriendNpc(x: Float, y: Float) : CharacterEntity(), Interactable {
    override val id = "mia"
    override var active = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(x, y, x + 14f, y + 18f)
    private var idleTime = 0f
    private var blink = 0f
    private val idleBehavior: Behavior<FriendNpc> = object : Behavior<FriendNpc> {
        override fun update(owner: FriendNpc, dt: Float) {
            owner.idleTime += dt
            owner.blink = if ((owner.idleTime % 3.7f) > 3.52f) 1f else 0f
        }
    }
    var theme: ImaginationTheme = ImaginationTheme.REAL
    override val interactionBounds: RectF = RectF(body).apply { inset(-22f, -22f) }
    override fun interactionText(): String = if (theme == ImaginationTheme.REAL) "Want to play? Meet me inside the fort!" else "Sir Mia says the Moon Goblins stole the yard's crown!"
    override fun update(dt: Float) = idleBehavior.update(this, dt)
    override fun draw(canvas: Canvas) {
        val bob = sin(idleTime * 2f) * 0.6f
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF9A4770.toInt() else 0xFF3857A8.toInt()
        canvas.drawRect(body.left, body.top + bob, body.right, body.bottom + bob, paint)
        paint.color = 0xFFF1C89A.toInt(); canvas.drawCircle(body.centerX(), body.top + 3f + bob, 5f, paint)
        if (blink < 0.5f) { paint.color = 0xFF2B211B.toInt(); canvas.drawCircle(body.centerX()-1.5f, body.top+2.5f+bob, .55f, paint); canvas.drawCircle(body.centerX()+1.5f, body.top+2.5f+bob, .55f, paint) }
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 8f
        canvas.drawText(if (theme == ImaginationTheme.REAL) "MIA" else "SIR MIA", body.centerX(), body.bottom + 10f, paint)
    }
}
