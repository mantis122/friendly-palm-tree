package com.example.backyardrealms.game.enemy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.combat.DamageHit
import com.example.backyardrealms.engine.combat.Damageable
import com.example.backyardrealms.engine.combat.HealthComponent
import com.example.backyardrealms.engine.entity.CharacterEntity
import kotlin.math.sin
import kotlin.math.sqrt

enum class EnemyState { WANDER, CHASE, HURT, STUNNED, DEFEATED }

class YardBlob(
    override val id: String,
    private val spawnX: Float,
    private val spawnY: Float
) : CharacterEntity(), Damageable {
    override var active = true
    override val health = HealthComponent(3)
    override val isInvulnerable: Boolean get() = hurtTimer > 0f
    override val hurtBounds: RectF get() = RectF(body)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(spawnX, spawnY, spawnX + 20f, spawnY + 16f)
    private var velocityX = 0f
    private var velocityY = 0f
    private var age = 0f
    private var wanderTimer = 0f
    private var hurtTimer = 0f
    private var defeatedTimer = 0f
    private var stunTimer = 0f
    private var lastAttackId = -1
    var state: EnemyState = EnemyState.WANDER
        private set

    val centerX: Float get() = body.centerX()
    val centerY: Float get() = body.centerY()
    val contactBounds: RectF get() = RectF(body).apply { inset(2f, 2f) }

    fun update(dt: Float, playerX: Float, playerY: Float, obstacles: List<RectF>, worldBounds: RectF) {
        age += dt
        if (state == EnemyState.DEFEATED) {
            defeatedTimer -= dt
            if (defeatedTimer <= 0f) respawn()
            return
        }
        if (stunTimer > 0f) {
            stunTimer = (stunTimer - dt).coerceAtLeast(0f)
            state = EnemyState.STUNNED
            velocityX *= 0.78f
            velocityY *= 0.78f
            if (stunTimer <= 0f) state = EnemyState.WANDER
            return
        }
        if (hurtTimer > 0f) {
            hurtTimer -= dt
            state = EnemyState.HURT
            moveAxis(velocityX * dt, 0f, obstacles, worldBounds)
            moveAxis(0f, velocityY * dt, obstacles, worldBounds)
            velocityX *= 0.86f
            velocityY *= 0.86f
            if (hurtTimer <= 0f) state = EnemyState.WANDER
            return
        }

        val dx = playerX - centerX
        val dy = playerY - centerY
        val distance = sqrt(dx * dx + dy * dy)
        if (distance < NOTICE_DISTANCE) {
            state = EnemyState.CHASE
            val safe = distance.coerceAtLeast(0.001f)
            velocityX = dx / safe * CHASE_SPEED
            velocityY = dy / safe * CHASE_SPEED
        } else {
            state = EnemyState.WANDER
            wanderTimer -= dt
            if (wanderTimer <= 0f) {
                wanderTimer = 1.3f + ((age * 1.7f) % 1.4f)
                val angle = age * 2.31f + id.hashCode() * 0.01f
                velocityX = kotlin.math.cos(angle) * WANDER_SPEED
                velocityY = kotlin.math.sin(angle) * WANDER_SPEED
            }
        }
        moveAxis(velocityX * dt, 0f, obstacles, worldBounds)
        moveAxis(0f, velocityY * dt, obstacles, worldBounds)
    }

    override fun update(dt: Float) = Unit

    override fun receiveHit(hit: DamageHit): Boolean {
        if (!active || state == EnemyState.DEFEATED || isInvulnerable || hit.attackId == lastAttackId) return false
        lastAttackId = hit.attackId
        if (health.damage(hit.amount) <= 0) return false
        velocityX = hit.knockbackX
        velocityY = hit.knockbackY
        hurtTimer = 0.24f
        if (!health.isAlive) {
            state = EnemyState.DEFEATED
            active = false
            defeatedTimer = RESPAWN_SECONDS
        }
        return true
    }

    fun forceRespawn() = respawn()

    /** Companion support action: stun without dealing damage. */
    fun stun(seconds: Float = 0.75f): Boolean {
        if (!active || state == EnemyState.DEFEATED || state == EnemyState.HURT) return false
        stunTimer = maxOf(stunTimer, seconds)
        velocityX *= -0.25f
        velocityY *= -0.25f
        state = EnemyState.STUNNED
        return true
    }

    private fun respawn() {
        body.offsetTo(spawnX, spawnY)
        health.restore()
        active = true
        state = EnemyState.WANDER
        hurtTimer = 0f
        defeatedTimer = 0f
        stunTimer = 0f
        velocityX = 0f
        velocityY = 0f
        lastAttackId = -1
    }

    override fun draw(canvas: Canvas) {
        if (!active || state == EnemyState.DEFEATED) return
        val squash = sin(age * 7f) * 1.4f
        paint.color = when (state) {
            EnemyState.HURT -> 0xFFFFE0E0.toInt()
            EnemyState.STUNNED -> 0xFF8ED8FF.toInt()
            else -> 0xFF9D63D6.toInt()
        }
        canvas.drawOval(body.left, body.top + squash, body.right, body.bottom - squash, paint)
        paint.color = 0xFFE7C9FF.toInt()
        canvas.drawCircle(body.centerX() - 4f, body.centerY() - 2f, 2f, paint)
        canvas.drawCircle(body.centerX() + 4f, body.centerY() - 2f, 2f, paint)
        paint.color = 0xFF24172E.toInt()
        canvas.drawCircle(body.centerX() - 4f, body.centerY() - 2f, .8f, paint)
        canvas.drawCircle(body.centerX() + 4f, body.centerY() - 2f, .8f, paint)

        paint.color = 0xCC000000.toInt()
        canvas.drawRect(body.left, body.top - 6f, body.right, body.top - 3f, paint)
        paint.color = 0xFF71D46C.toInt()
        val ratio = health.current.toFloat() / health.maximum
        canvas.drawRect(body.left, body.top - 6f, body.left + body.width() * ratio, body.top - 3f, paint)
    }

    private fun moveAxis(dx: Float, dy: Float, obstacles: List<RectF>, bounds: RectF) {
        if (dx == 0f && dy == 0f) return
        body.offset(dx, dy)
        if (body.left < bounds.left) { body.offset(bounds.left - body.left, 0f); velocityX *= -1f }
        if (body.right > bounds.right) { body.offset(bounds.right - body.right, 0f); velocityX *= -1f }
        if (body.top < bounds.top) { body.offset(0f, bounds.top - body.top); velocityY *= -1f }
        if (body.bottom > bounds.bottom) { body.offset(0f, bounds.bottom - body.bottom); velocityY *= -1f }
        for (obstacle in obstacles) {
            if (!RectF.intersects(body, obstacle)) continue
            if (dx > 0f) { body.offset(obstacle.left - body.right, 0f); velocityX *= -0.5f }
            if (dx < 0f) { body.offset(obstacle.right - body.left, 0f); velocityX *= -0.5f }
            if (dy > 0f) { body.offset(0f, obstacle.top - body.bottom); velocityY *= -0.5f }
            if (dy < 0f) { body.offset(0f, obstacle.bottom - body.top); velocityY *= -0.5f }
        }
    }

    companion object {
        private const val NOTICE_DISTANCE = 120f
        private const val WANDER_SPEED = 24f
        private const val CHASE_SPEED = 46f
        private const val RESPAWN_SECONDS = 4f
    }
}
