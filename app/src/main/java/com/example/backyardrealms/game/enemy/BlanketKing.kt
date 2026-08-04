package com.example.backyardrealms.game.enemy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.combat.DamageHit
import com.example.backyardrealms.engine.combat.Damageable
import com.example.backyardrealms.engine.combat.HealthComponent
import com.example.backyardrealms.engine.entity.CharacterEntity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class BlanketKingPhase { PHASE_ONE, SHIELDED, VULNERABLE, FINAL, DEFEATED }

class BlanketKing(
    override val id: String,
    private val spawnX: Float,
    private val spawnY: Float
) : CharacterEntity(), Damageable {
    override var active = true
    override val health = HealthComponent(9)
    private val body = RectF(spawnX, spawnY, spawnX + 54f, spawnY + 62f)
    override val hurtBounds: RectF get() = RectF(body).apply { inset(5f, 4f) }
    override val isInvulnerable: Boolean get() = hurtTimer > 0f || phase == BlanketKingPhase.SHIELDED
    val contactBounds: RectF get() = RectF(body).apply { inset(7f, 7f) }
    val centerX get() = body.centerX()
    val centerY get() = body.centerY()
    val needsShieldBreak get() = active && phase == BlanketKingPhase.SHIELDED

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var vx = 0f
    private var vy = 0f
    private var age = 0f
    private var hurtTimer = 0f
    private var vulnerableTimer = 0f
    private var attackTimer = 1.2f
    private var lastAttackId = -1
    private var attackSerial = 1
    private val pillows = mutableListOf<Pillow>()
    var phase: BlanketKingPhase = BlanketKingPhase.PHASE_ONE
        private set

    data class Pillow(var bounds: RectF, var vx: Float, var vy: Float, var life: Float, val attackId: Int)

    fun update(dt: Float, playerX: Float, playerY: Float, obstacles: List<RectF>, bounds: RectF) {
        if (!active) return
        age += dt
        hurtTimer = (hurtTimer - dt).coerceAtLeast(0f)
        pillows.forEach { p -> p.bounds.offset(p.vx * dt, p.vy * dt); p.life -= dt }
        pillows.removeAll { it.life <= 0f || !RectF.intersects(it.bounds, bounds) }

        if (phase == BlanketKingPhase.VULNERABLE) {
            vulnerableTimer -= dt
            vx *= .84f; vy *= .84f
            if (vulnerableTimer <= 0f && health.current > 3) phase = BlanketKingPhase.SHIELDED
            return
        }
        if (phase == BlanketKingPhase.SHIELDED || hurtTimer > 0f) {
            vx *= .82f; vy *= .82f
            return
        }

        val dx = playerX - centerX
        val dy = playerY - centerY
        val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
        attackTimer -= dt
        val speed = if (phase == BlanketKingPhase.FINAL) 72f else 48f
        vx = dx / distance * speed
        vy = dy / distance * speed
        if (attackTimer <= 0f) {
            throwPillows(playerX, playerY)
            attackTimer = if (phase == BlanketKingPhase.FINAL) .8f else 1.45f
        }
        move(vx * dt, 0f, obstacles, bounds)
        move(0f, vy * dt, obstacles, bounds)
    }

    override fun update(dt: Float) = Unit

    fun shieldBash(seconds: Float = 2.4f): Boolean {
        if (!needsShieldBreak) return false
        phase = BlanketKingPhase.VULNERABLE
        vulnerableTimer = seconds
        vx = 0f; vy = 0f
        pillows.clear()
        return true
    }

    fun activeHazards(): List<Pair<RectF, Int>> = pillows.map { RectF(it.bounds) to it.attackId }

    override fun receiveHit(hit: DamageHit): Boolean {
        if (!active || isInvulnerable || hit.attackId == lastAttackId) return false
        lastAttackId = hit.attackId
        if (health.damage(hit.amount) <= 0) return false
        vx = hit.knockbackX * .35f
        vy = hit.knockbackY * .35f
        hurtTimer = .22f
        if (!health.isAlive) {
            active = false
            phase = BlanketKingPhase.DEFEATED
            pillows.clear()
        } else if (health.current <= 3) {
            phase = BlanketKingPhase.FINAL
            vulnerableTimer = 0f
        } else if (health.current <= 6 && phase == BlanketKingPhase.PHASE_ONE) {
            phase = BlanketKingPhase.SHIELDED
            pillows.clear()
        }
        return true
    }

    fun restore() {
        active = true
        health.restore()
        body.offsetTo(spawnX, spawnY)
        vx = 0f; vy = 0f; age = 0f; hurtTimer = 0f; vulnerableTimer = 0f
        attackTimer = 1.2f; lastAttackId = -1; pillows.clear()
        phase = BlanketKingPhase.PHASE_ONE
    }

    fun markDefeated() {
        active = false
        health.damage(health.maximum)
        phase = BlanketKingPhase.DEFEATED
        pillows.clear()
    }

    override fun draw(canvas: Canvas) {
        pillows.forEach { p ->
            paint.color = 0xFFE9D8B7.toInt()
            canvas.drawRoundRect(p.bounds, 5f, 5f, paint)
            paint.color = 0xFFB88E72.toInt()
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(p.bounds, 5f, 5f, paint)
            paint.style = Paint.Style.FILL
        }
        if (!active) return
        val wobble = sin(age * 5f) * 2f
        paint.color = when (phase) {
            BlanketKingPhase.SHIELDED -> 0xFF5B4C8C.toInt()
            BlanketKingPhase.VULNERABLE -> 0xFFE4D8F6.toInt()
            BlanketKingPhase.FINAL -> 0xFF9E4F68.toInt()
            else -> if (hurtTimer > 0f) 0xFFFFE7E7.toInt() else 0xFF76538F.toInt()
        }
        canvas.drawRoundRect(body.left, body.top + wobble, body.right, body.bottom, 12f, 12f, paint)
        paint.color = 0xFF59412F.toInt()
        canvas.drawRect(body.left + 8f, body.bottom - 10f, body.right - 8f, body.bottom + 4f, paint)
        paint.color = 0xFFFFE36B.toInt()
        canvas.drawCircle(body.centerX() - 11f, body.top + 18f, 4f, paint)
        canvas.drawCircle(body.centerX() + 11f, body.top + 18f, 4f, paint)
        paint.color = 0xFF2A2130.toInt()
        canvas.drawCircle(body.centerX() - 11f, body.top + 18f, 1.6f, paint)
        canvas.drawCircle(body.centerX() + 11f, body.top + 18f, 1.6f, paint)
        // cardboard crown
        paint.color = 0xFFE6B84D.toInt()
        canvas.drawRect(body.left + 10f, body.top - 8f, body.right - 10f, body.top + 4f, paint)
        canvas.drawCircle(body.left + 14f, body.top - 8f, 5f, paint)
        canvas.drawCircle(body.centerX(), body.top - 11f, 6f, paint)
        canvas.drawCircle(body.right - 14f, body.top - 8f, 5f, paint)
        paint.color = 0xCC000000.toInt()
        canvas.drawRect(body.left, body.top - 18f, body.right, body.top - 14f, paint)
        paint.color = 0xFF71D46C.toInt()
        canvas.drawRect(body.left, body.top - 18f, body.left + body.width() * health.current / health.maximum.toFloat(), body.top - 14f, paint)
    }

    private fun throwPillows(playerX: Float, playerY: Float) {
        val count = if (phase == BlanketKingPhase.FINAL) 3 else 1
        repeat(count) { index ->
            val dx = playerX - centerX
            val dy = playerY - centerY
            val base = kotlin.math.atan2(dy, dx)
            val spread = (index - (count - 1) / 2f) * .28f
            val angle = base + spread
            val speed = if (phase == BlanketKingPhase.FINAL) 100f else 82f
            pillows += Pillow(RectF(centerX - 7f, centerY - 5f, centerX + 7f, centerY + 5f), cos(angle) * speed, sin(angle) * speed, 3.2f, attackSerial++)
        }
    }

    private fun move(dx: Float, dy: Float, obstacles: List<RectF>, bounds: RectF) {
        body.offset(dx, dy)
        if (body.left < bounds.left) body.offset(bounds.left - body.left, 0f)
        if (body.right > bounds.right) body.offset(bounds.right - body.right, 0f)
        if (body.top < bounds.top) body.offset(0f, bounds.top - body.top)
        if (body.bottom > bounds.bottom) body.offset(0f, bounds.bottom - body.bottom)
        for (o in obstacles) if (RectF.intersects(body, o)) {
            if (dx > 0f) body.offset(o.left - body.right, 0f)
            if (dx < 0f) body.offset(o.right - body.left, 0f)
            if (dy > 0f) body.offset(0f, o.top - body.bottom)
            if (dy < 0f) body.offset(0f, o.bottom - body.top)
        }
    }
}
