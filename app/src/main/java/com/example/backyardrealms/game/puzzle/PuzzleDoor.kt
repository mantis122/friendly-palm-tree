package com.example.backyardrealms.game.puzzle

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class PuzzleDoor(val id: String, val bounds: RectF) {
    var open: Boolean = false

    fun collisionBounds(): List<RectF> = if (open) emptyList() else listOf(RectF(bounds))

    fun draw(canvas: Canvas, paint: Paint) {
        if (open) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = 0x667ED6FF.toInt()
            canvas.drawRect(bounds, paint)
            paint.style = Paint.Style.FILL
            return
        }
        paint.color = 0xFF584437.toInt()
        canvas.drawRect(bounds, paint)
        paint.color = 0xFFB89458.toInt()
        for (y in listOf(bounds.top + 10f, bounds.centerY(), bounds.bottom - 10f)) {
            canvas.drawRect(bounds.left, y - 2f, bounds.right, y + 2f, paint)
        }
    }
}
