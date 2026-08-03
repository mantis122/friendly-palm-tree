package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.InputSnapshot
import com.example.backyardrealms.engine.graphics.AnimatedSprite
import com.example.backyardrealms.engine.graphics.SpriteSheet
import kotlin.math.abs
import kotlin.math.sqrt

class Player(
    startX: Float,
    startY: Float,
    private val spriteSheet: SpriteSheet
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(startX, startY, startX + WIDTH, startY + HEIGHT)
    private val idleAnimation = AnimatedSprite(intArrayOf(0), 1f)
    private val walkAnimation = AnimatedSprite(intArrayOf(0, 1, 2, 1), 0.12f)

    private var velocityX = 0f
    private var velocityY = 0f
    private var facingX = 0f
    private var facingY = 1f
    private var actionTimer = 0f
    private var moving = false

    val centerX: Float get() = body.centerX()
    val centerY: Float get() = body.centerY()
    val x: Float get() = body.left
    val y: Float get() = body.top
    fun setPosition(x: Float, y: Float) { body.offsetTo(x, y); velocityX = 0f; velocityY = 0f }
    val collisionBounds: RectF get() = RectF(body)
    val isAttacking: Boolean get() = actionTimer > 0f

    fun update(dt: Float, input: InputSnapshot, obstacles: List<RectF>, bounds: RectF) {
        var desiredX = input.moveX
        var desiredY = input.moveY
        val desiredLength = sqrt(desiredX * desiredX + desiredY * desiredY)
        if (desiredLength > 1f) {
            desiredX /= desiredLength
            desiredY /= desiredLength
        }

        val hasInput = abs(desiredX) + abs(desiredY) > 0.12f
        val response = if (hasInput) ACCELERATION else DECELERATION
        velocityX = approach(velocityX, desiredX * MAX_SPEED, response * dt)
        velocityY = approach(velocityY, desiredY * MAX_SPEED, response * dt)
        moving = abs(velocityX) + abs(velocityY) > 3f

        if (hasInput) {
            facingX = desiredX
            facingY = desiredY
        }

        moveAxis(velocityX * dt, 0f, obstacles, bounds)
        moveAxis(0f, velocityY * dt, obstacles, bounds)

        if (input.actionPressed && actionTimer <= 0f) actionTimer = ACTION_SECONDS
        if (actionTimer > 0f) actionTimer = (actionTimer - dt).coerceAtLeast(0f)

        if (moving) walkAnimation.update(dt) else idleAnimation.update(dt)
    }

    fun draw(canvas: Canvas) {
        val frame = if (moving) walkAnimation.currentFrame else idleAnimation.currentFrame
        val destination = RectF(body.left - 5f, body.top - 11f, body.right + 5f, body.bottom + 3f)
        spriteSheet.drawFrame(canvas, frame, destination, flipX = facingX < -0.1f)

        if (actionTimer > 0f) {
            val progress = 1f - actionTimer / ACTION_SECONDS
            val baseAngle = Math.toDegrees(kotlin.math.atan2(facingY.toDouble(), facingX.toDouble())).toFloat()
            val swingAngle = baseAngle - 55f + progress * 110f
            val reach = 20f
            val radians = Math.toRadians(swingAngle.toDouble())
            val cx = body.centerX() + kotlin.math.cos(radians).toFloat() * reach
            val cy = body.centerY() + kotlin.math.sin(radians).toFloat() * reach
            paint.color = 0xFFE8D28A.toInt()
            canvas.save()
            canvas.rotate(swingAngle, cx, cy)
            canvas.drawRect(cx - 2f, cy - 13f, cx + 2f, cy + 13f, paint)
            canvas.restore()
        }
    }

    fun attackBounds(): RectF {
        val reach = 22f
        val cx = body.centerX() + facingX * reach
        val cy = body.centerY() + facingY * reach
        return RectF(cx - 10f, cy - 10f, cx + 10f, cy + 10f)
    }

    fun positionText(): String = "x=${body.left.toInt()} y=${body.top.toInt()}"

    private fun moveAxis(dx: Float, dy: Float, obstacles: List<RectF>, bounds: RectF) {
        if (dx == 0f && dy == 0f) return
        body.offset(dx, dy)

        if (body.left < bounds.left) body.offset(bounds.left - body.left, 0f)
        if (body.right > bounds.right) body.offset(bounds.right - body.right, 0f)
        if (body.top < bounds.top) body.offset(0f, bounds.top - body.top)
        if (body.bottom > bounds.bottom) body.offset(0f, bounds.bottom - body.bottom)

        for (obstacle in obstacles) {
            if (!RectF.intersects(body, obstacle)) continue
            if (dx > 0f) {
                body.offset(obstacle.left - body.right, 0f)
                velocityX = 0f
            }
            if (dx < 0f) {
                body.offset(obstacle.right - body.left, 0f)
                velocityX = 0f
            }
            if (dy > 0f) {
                body.offset(0f, obstacle.top - body.bottom)
                velocityY = 0f
            }
            if (dy < 0f) {
                body.offset(0f, obstacle.bottom - body.top)
                velocityY = 0f
            }
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
        private const val MAX_SPEED = 92f
        private const val ACCELERATION = 620f
        private const val DECELERATION = 760f
        private const val ACTION_SECONDS = 0.24f
    }
}
