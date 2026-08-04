package com.example.backyardrealms.game.enemy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.combat.DamageHit
import com.example.backyardrealms.engine.combat.Damageable
import com.example.backyardrealms.engine.combat.HealthComponent
import com.example.backyardrealms.engine.entity.CharacterEntity
import kotlin.math.sqrt

class GoblinScout(
    override val id: String,
    private val spawnX: Float,
    private val spawnY: Float
) : CharacterEntity(), Damageable {
    override var active = true
    override val health = HealthComponent(4)
    override val isInvulnerable: Boolean get() = hurtTimer > 0f
    private val body = RectF(spawnX, spawnY, spawnX + 18f, spawnY + 22f)
    override val hurtBounds: RectF get() = RectF(body)
    val contactBounds: RectF get() = RectF(body).apply { inset(2f, 2f) }
    val centerX get() = body.centerX()
    val centerY get() = body.centerY()
    private var vx = 36f
    private var vy = 0f
    private var hurtTimer = 0f
    private var chargeTimer = 0f
    private var lastAttackId = -1
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun update(dt: Float, playerX: Float, playerY: Float, obstacles: List<RectF>, bounds: RectF) {
        if (!active) return
        if (hurtTimer > 0f) {
            hurtTimer = (hurtTimer - dt).coerceAtLeast(0f)
            move(vx * dt, vy * dt, obstacles, bounds)
            vx *= .84f; vy *= .84f
            return
        }
        val dx = playerX - centerX
        val dy = playerY - centerY
        val d = sqrt(dx * dx + dy * dy)
        chargeTimer -= dt
        if (d < 150f && chargeTimer <= 0f) {
            val safe = d.coerceAtLeast(.001f)
            vx = dx / safe * 82f
            vy = dy / safe * 82f
            chargeTimer = 1.15f
        } else if (chargeTimer < .55f) {
            vy *= .8f
            if (kotlin.math.abs(vx) < 25f) vx = if (dx >= 0f) 36f else -36f
        }
        move(vx * dt, 0f, obstacles, bounds)
        move(0f, vy * dt, obstacles, bounds)
    }

    override fun update(dt: Float) = Unit

    override fun receiveHit(hit: DamageHit): Boolean {
        if (!active || isInvulnerable || hit.attackId == lastAttackId) return false
        lastAttackId = hit.attackId
        if (health.damage(hit.amount) <= 0) return false
        vx = hit.knockbackX; vy = hit.knockbackY; hurtTimer = .26f
        if (!health.isAlive) active = false
        return true
    }

    fun restore() {
        active = true; health.restore(); body.offsetTo(spawnX, spawnY)
        vx = 36f; vy = 0f; hurtTimer = 0f; chargeTimer = 0f; lastAttackId = -1
    }

    override fun draw(canvas: Canvas) {
        if (!active) return
        paint.color = if (hurtTimer > 0f) 0xFFFFE5D0.toInt() else 0xFF6FAA46.toInt()
        canvas.drawRoundRect(body, 4f, 4f, paint)
        paint.color = 0xFF384426.toInt()
        canvas.drawCircle(body.left + 3f, body.top + 3f, 4f, paint)
        canvas.drawCircle(body.right - 3f, body.top + 3f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(body.centerX() - 4f, body.top + 9f, 2f, paint)
        canvas.drawCircle(body.centerX() + 4f, body.top + 9f, 2f, paint)
    }

    private fun move(dx: Float, dy: Float, obstacles: List<RectF>, bounds: RectF) {
        body.offset(dx, dy)
        if (body.left < bounds.left) { body.offset(bounds.left - body.left, 0f); vx = kotlin.math.abs(vx) }
        if (body.right > bounds.right) { body.offset(bounds.right - body.right, 0f); vx = -kotlin.math.abs(vx) }
        if (body.top < bounds.top) { body.offset(0f, bounds.top - body.top); vy = kotlin.math.abs(vy) }
        if (body.bottom > bounds.bottom) { body.offset(0f, bounds.bottom - body.bottom); vy = -kotlin.math.abs(vy) }
        for (o in obstacles) if (RectF.intersects(body, o)) {
            if (dx > 0) { body.offset(o.left - body.right, 0f); vx = -kotlin.math.abs(vx) }
            if (dx < 0) { body.offset(o.right - body.left, 0f); vx = kotlin.math.abs(vx) }
            if (dy > 0) { body.offset(0f, o.top - body.bottom); vy = -kotlin.math.abs(vy) }
            if (dy < 0) { body.offset(0f, o.bottom - body.top); vy = kotlin.math.abs(vy) }
        }
    }
}
