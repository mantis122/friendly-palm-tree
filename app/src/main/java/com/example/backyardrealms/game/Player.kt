package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.InputSnapshot
import kotlin.math.abs

class Player(startX: Float, startY: Float) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = RectF(startX, startY, startX + WIDTH, startY + HEIGHT)
    private var facingX = 0f
    private var facingY = 1f
    private var actionTimer = 0f

    fun update(dt: Float, input: InputSnapshot, obstacles: List<RectF>, bounds: RectF) {
        val dx = input.moveX * SPEED * dt
        val dy = input.moveY * SPEED * dt

        if (abs(input.moveX) + abs(input.moveY) > 0.15f) {
            facingX = input.moveX
            facingY = input.moveY
        }

        moveAxis(dx, 0f, obstacles, bounds)
        moveAxis(0f, dy, obstacles, bounds)

        if (input.actionPressed) actionTimer = ACTION_SECONDS
        if (actionTimer > 0f) actionTimer = (actionTimer - dt).coerceAtLeast(0f)
    }

    fun draw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFF315A9A.toInt()
        canvas.drawRect(body, paint)

        paint.color = 0xFFF1C89A.toInt()
        canvas.drawCircle(body.centerX(), body.top + 3f, 5f, paint)

        if (actionTimer > 0f) {
            val reach = 19f
            val cx = body.centerX() + facingX * reach
            val cy = body.centerY() + facingY * reach
            paint.color = 0xFFE8D28A.toInt()
            canvas.save()
            canvas.rotate(
                Math.toDegrees(kotlin.math.atan2(facingY.toDouble(), facingX.toDouble())).toFloat(),
                cx,
                cy
            )
            canvas.drawRect(cx - 2f, cy - 13f, cx + 2f, cy + 13f, paint)
            canvas.restore()
        }
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
            if (dx > 0f) body.offset(obstacle.left - body.right, 0f)
            if (dx < 0f) body.offset(obstacle.right - body.left, 0f)
            if (dy > 0f) body.offset(0f, obstacle.top - body.bottom)
            if (dy < 0f) body.offset(0f, obstacle.bottom - body.top)
        }
    }

    companion object {
        private const val WIDTH = 14f
        private const val HEIGHT = 18f
        private const val SPEED = 82f
        private const val ACTION_SECONDS = 0.18f
    }
}
