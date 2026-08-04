package com.example.backyardrealms.game.puzzle

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class FloorSwitch(val id: String, val bounds: RectF) {
    var pressed: Boolean = false

    fun update(occupant: RectF) {
        pressed = RectF.intersects(bounds, occupant)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = if (pressed) 0xFFFFD75A.toInt() else 0xFF77614A.toInt()
        canvas.drawOval(bounds, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFF2A2018.toInt()
        canvas.drawOval(bounds, paint)
        paint.style = Paint.Style.FILL
    }
}
