package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.entity.Behavior
import com.example.backyardrealms.engine.entity.CharacterEntity
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.theme.ImaginationTheme
import kotlin.math.sin
import kotlin.math.sqrt

class FriendNpc(x: Float, y: Float) : CharacterEntity(), Interactable {
    override val id = "mia"
    override var active = true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(x, y, x + WIDTH, y + HEIGHT)
    private var idleTime = 0f
    private var blink = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    private var shieldCooldown = 0f
    private var shieldFlash = 0f
    private var retreatTimer = 0f

    private val idleBehavior: Behavior<FriendNpc> = object : Behavior<FriendNpc> {
        override fun update(owner: FriendNpc, dt: Float) {
            owner.idleTime += dt
            owner.blink = if ((owner.idleTime % 3.7f) > 3.52f) 1f else 0f
            owner.shieldCooldown = (owner.shieldCooldown - dt).coerceAtLeast(0f)
            owner.shieldFlash = (owner.shieldFlash - dt).coerceAtLeast(0f)
            owner.retreatTimer = (owner.retreatTimer - dt).coerceAtLeast(0f)
        }
    }

    var theme: ImaginationTheme = ImaginationTheme.REAL

    var debugPreferredX: Float = centerX
        private set
    var debugPreferredY: Float = centerY
        private set
    var debugKeepOutRadius: Float = NORMAL_KEEP_OUT
        private set
    var debugCombatMode: Boolean = false
        private set

    val centerX: Float get() = body.centerX()
    val centerY: Float get() = body.centerY()
    val collisionBounds: RectF get() = RectF(body)

    override val interactionBounds: RectF
        get() = RectF(body).apply { inset(-22f, -22f) }

    fun setPosition(x: Float, y: Float) {
        body.offsetTo(x, y)
        velocityX = 0f
        velocityY = 0f
    }

    fun updateCompanion(
        dt: Float,
        playerX: Float,
        playerY: Float,
        playerVelocityX: Float,
        playerVelocityY: Float,
        playerFacingX: Float,
        playerFacingY: Float,
        playerAttacking: Boolean,
        enemyNearby: Boolean,
        obstacles: List<RectF>,
        worldBounds: RectF
    ) {
        if (theme != ImaginationTheme.FANTASY) return

        val combatMode = enemyNearby || playerAttacking || retreatTimer > 0f
        debugCombatMode = combatMode
        val keepOut = if (combatMode) COMBAT_KEEP_OUT else NORMAL_KEEP_OUT
        val followDistance = if (combatMode) COMBAT_FOLLOW_DISTANCE else NORMAL_FOLLOW_DISTANCE
        debugKeepOutRadius = keepOut

        val movementLength = sqrt(playerVelocityX * playerVelocityX + playerVelocityY * playerVelocityY)
        var directionX = if (movementLength > 2f) playerVelocityX / movementLength else playerFacingX
        var directionY = if (movementLength > 2f) playerVelocityY / movementLength else playerFacingY
        val directionLength = sqrt(directionX * directionX + directionY * directionY).coerceAtLeast(0.001f)
        directionX /= directionLength
        directionY /= directionLength

        var targetX = playerX - directionX * followDistance
        var targetY = playerY - directionY * followDistance

        if (combatMode) {
            val sideSign = if ((centerX - playerX) * -directionY + (centerY - playerY) * directionX >= 0f) 1f else -1f
            targetX += -directionY * COMBAT_SIDE_OFFSET * sideSign
            targetY += directionX * COMBAT_SIDE_OFFSET * sideSign
        } else {
            targetY += 10f
        }

        val fromPlayerX = centerX - playerX
        val fromPlayerY = centerY - playerY
        val playerDistance = sqrt(fromPlayerX * fromPlayerX + fromPlayerY * fromPlayerY)
        if (playerDistance < keepOut) {
            val safe = playerDistance.coerceAtLeast(0.001f)
            targetX = playerX + fromPlayerX / safe * (keepOut + 12f)
            targetY = playerY + fromPlayerY / safe * (keepOut + 12f)
        }

        if (playerAttacking && playerDistance < ATTACK_AVOID_DISTANCE) {
            val safe = playerDistance.coerceAtLeast(0.001f)
            val dot = fromPlayerX / safe * playerFacingX + fromPlayerY / safe * playerFacingY
            if (dot > ATTACK_ARC_DOT) {
                val sideSign = if (fromPlayerX * -playerFacingY + fromPlayerY * playerFacingX >= 0f) 1f else -1f
                targetX = playerX - playerFacingX * COMBAT_FOLLOW_DISTANCE - playerFacingY * COMBAT_SIDE_OFFSET * sideSign
                targetY = playerY - playerFacingY * COMBAT_FOLLOW_DISTANCE + playerFacingX * COMBAT_SIDE_OFFSET * sideSign
            }
        }

        debugPreferredX = targetX
        debugPreferredY = targetY

        val dx = targetX - centerX
        val dy = targetY - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > RECOVERY_DISTANCE) {
            setPosition(
                (targetX - WIDTH / 2f).coerceIn(worldBounds.left, worldBounds.right - WIDTH),
                (targetY - HEIGHT / 2f).coerceIn(worldBounds.top, worldBounds.bottom - HEIGHT)
            )
            return
        }

        if (distance < FOLLOW_DEAD_ZONE) {
            velocityX = approach(velocityX, 0f, DECELERATION * dt)
            velocityY = approach(velocityY, 0f, DECELERATION * dt)
        } else {
            val safe = distance.coerceAtLeast(0.001f)
            val desiredSpeed = if (distance > 90f) CATCH_UP_SPEED else FOLLOW_SPEED
            velocityX = approach(velocityX, dx / safe * desiredSpeed, ACCELERATION * dt)
            velocityY = approach(velocityY, dy / safe * desiredSpeed, ACCELERATION * dt)
        }

        moveAxis(velocityX * dt, 0f, obstacles, worldBounds)
        moveAxis(0f, velocityY * dt, obstacles, worldBounds)
    }


    fun updateScriptedPosition(
        dt: Float,
        targetX: Float,
        targetY: Float,
        obstacles: List<RectF>,
        worldBounds: RectF
    ) {
        val dx = targetX - centerX
        val dy = targetY - centerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        if (distance < 2f) {
            velocityX = 0f
            velocityY = 0f
            return
        }
        val safe = distance.coerceAtLeast(0.001f)
        velocityX = dx / safe * FOLLOW_SPEED
        velocityY = dy / safe * FOLLOW_SPEED
        moveAxis(velocityX * dt, 0f, obstacles, worldBounds)
        moveAxis(0f, velocityY * dt, obstacles, worldBounds)
    }

    fun tryShieldBash(targetX: Float, targetY: Float): Boolean {
        if (theme != ImaginationTheme.FANTASY || shieldCooldown > 0f || retreatTimer > 0f) return false
        val dx = targetX - centerX
        val dy = targetY - centerY
        if (dx * dx + dy * dy > SHIELD_RANGE * SHIELD_RANGE) return false
        shieldCooldown = SHIELD_COOLDOWN
        shieldFlash = 0.22f
        retreatTimer = RETREAT_SECONDS
        return true
    }

    override fun interactionText(): String = if (theme == ImaginationTheme.REAL) {
        "Mia grins. “Want to play?”"
    } else {
        "Sir Mia raises her cardboard shield. “Right beside you, Guardian.”"
    }

    override fun update(dt: Float) = idleBehavior.update(this, dt)

    override fun draw(canvas: Canvas) {
        val bob = sin(idleTime * 2f) * 0.6f
        paint.color = if (theme == ImaginationTheme.REAL) {
            0xFF9A4770.toInt()
        } else if (shieldFlash > 0f) {
            0xFF7FA8FF.toInt()
        } else {
            0xFF3857A8.toInt()
        }
        canvas.drawRect(body.left, body.top + bob, body.right, body.bottom + bob, paint)

        paint.color = 0xFFF1C89A.toInt()
        canvas.drawCircle(body.centerX(), body.top + 3f + bob, 5f, paint)

        if (blink < 0.5f) {
            paint.color = 0xFF2B211B.toInt()
            canvas.drawCircle(body.centerX() - 1.5f, body.top + 2.5f + bob, .55f, paint)
            canvas.drawCircle(body.centerX() + 1.5f, body.top + 2.5f + bob, .55f, paint)
        }

        if (theme == ImaginationTheme.FANTASY) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = if (shieldFlash > 0f) 0xFFFFFFFF.toInt() else 0xFFD7E2FF.toInt()
            canvas.drawCircle(body.right + 4f, body.centerY() + bob, 5f, paint)
            paint.style = Paint.Style.FILL
        }

        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8f
        canvas.drawText(if (theme == ImaginationTheme.REAL) "MIA" else "SIR MIA", body.centerX(), body.bottom + 10f, paint)
    }

    private fun moveAxis(dx: Float, dy: Float, obstacles: List<RectF>, bounds: RectF) {
        if (dx == 0f && dy == 0f) return
        body.offset(dx, dy)

        if (body.left < bounds.left) body.offset(bounds.left - body.left, 0f)
        if (body.right > bounds.right) body.offset(bounds.right - body.right, 0f)
        if (body.top < bounds.top) body.offset(0f, bounds.top - body.top)
        if (body.bottom > bounds.bottom) body.offset(0f, bounds.bottom - body.bottom)

        for (obstacle in obstacles) {
            if (!RectF.intersects(body, obstacle)) continue
            if (dx > 0f) body.offset(obstacle.left - body.right, 0f)
            if (dx < 0f) body.offset(obstacle.right - body.left, 0f)
            if (dy > 0f) body.offset(0f, obstacle.top - body.bottom)
            if (dy < 0f) body.offset(0f, obstacle.bottom - body.top)
        }
    }

    private fun approach(current: Float, target: Float, amount: Float): Float = when {
        current < target -> (current + amount).coerceAtMost(target)
        current > target -> (current - amount).coerceAtLeast(target)
        else -> target
    }

    companion object {
        private const val WIDTH = 14f
        private const val HEIGHT = 18f
        private const val NORMAL_FOLLOW_DISTANCE = 42f
        private const val COMBAT_FOLLOW_DISTANCE = 68f
        private const val NORMAL_KEEP_OUT = 24f
        private const val COMBAT_KEEP_OUT = 38f
        private const val COMBAT_SIDE_OFFSET = 24f
        private const val ATTACK_AVOID_DISTANCE = 78f
        private const val ATTACK_ARC_DOT = 0.18f
        private const val FOLLOW_DEAD_ZONE = 16f
        private const val RECOVERY_DISTANCE = 190f
        private const val FOLLOW_SPEED = 62f
        private const val CATCH_UP_SPEED = 92f
        private const val ACCELERATION = 300f
        private const val DECELERATION = 340f
        private const val SHIELD_RANGE = 45f
        private const val SHIELD_COOLDOWN = 2.8f
        private const val RETREAT_SECONDS = 0.9f
    }
}
