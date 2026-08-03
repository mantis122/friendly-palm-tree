package com.example.backyardrealms.engine

import android.graphics.Canvas
import android.graphics.RectF
import kotlin.math.abs

class Camera2D(
    private val viewportWidth: Float,
    private val viewportHeight: Float
) {
    var x = 0f
        private set
    var y = 0f
        private set

    private var initialized = false

    fun follow(targetX: Float, targetY: Float, worldBounds: RectF, dt: Float) {
        val desiredX = (targetX - viewportWidth * 0.5f)
            .coerceIn(worldBounds.left, (worldBounds.right - viewportWidth).coerceAtLeast(worldBounds.left))
        val desiredY = (targetY - viewportHeight * 0.5f)
            .coerceIn(worldBounds.top, (worldBounds.bottom - viewportHeight).coerceAtLeast(worldBounds.top))

        if (!initialized) {
            x = desiredX
            y = desiredY
            initialized = true
            return
        }

        val deadZoneX = 28f
        val deadZoneY = 18f
        val screenX = targetX - x
        val screenY = targetY - y
        var adjustedX = desiredX
        var adjustedY = desiredY

        if (abs(screenX - viewportWidth * 0.5f) < deadZoneX) adjustedX = x
        if (abs(screenY - viewportHeight * 0.5f) < deadZoneY) adjustedY = y

        val smoothing = (dt * 7.5f).coerceIn(0f, 1f)
        x += (adjustedX - x) * smoothing
        y += (adjustedY - y) * smoothing
    }

    fun begin(canvas: Canvas) {
        canvas.save()
        canvas.translate(-x, -y)
    }

    fun end(canvas: Canvas) {
        canvas.restore()
    }

    fun visibleBounds(): RectF = RectF(x, y, x + viewportWidth, y + viewportHeight)
}
