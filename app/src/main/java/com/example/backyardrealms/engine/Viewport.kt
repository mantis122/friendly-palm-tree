package com.example.backyardrealms.engine

import android.graphics.Canvas
import kotlin.math.min

class Viewport {
    var scale = 1f
        private set
    var offsetX = 0f
        private set
    var offsetY = 0f
        private set

    fun update(viewWidth: Int, viewHeight: Int) {
        if (viewWidth <= 0 || viewHeight <= 0) return
        scale = min(
            viewWidth / GameConfig.LOGICAL_WIDTH,
            viewHeight / GameConfig.LOGICAL_HEIGHT
        )
        offsetX = (viewWidth - GameConfig.LOGICAL_WIDTH * scale) * 0.5f
        offsetY = (viewHeight - GameConfig.LOGICAL_HEIGHT * scale) * 0.5f
    }

    fun begin(canvas: Canvas) {
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
    }

    fun end(canvas: Canvas) {
        canvas.restore()
    }

    fun toLogicalX(screenX: Float): Float = (screenX - offsetX) / scale
    fun toLogicalY(screenY: Float): Float = (screenY - offsetY) / scale
}
