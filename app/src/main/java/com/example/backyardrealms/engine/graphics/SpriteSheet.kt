package com.example.backyardrealms.engine.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

class SpriteSheet(
    private val bitmap: Bitmap,
    private val frameWidth: Int,
    private val frameHeight: Int
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
    private val columns = (bitmap.width / frameWidth).coerceAtLeast(1)

    fun drawFrame(
        canvas: Canvas,
        frameIndex: Int,
        destination: RectF,
        flipX: Boolean = false
    ) {
        val safeFrame = frameIndex.coerceAtLeast(0)
        val column = safeFrame % columns
        val row = safeFrame / columns
        val source = Rect(
            column * frameWidth,
            row * frameHeight,
            column * frameWidth + frameWidth,
            row * frameHeight + frameHeight
        )

        if (!flipX) {
            canvas.drawBitmap(bitmap, source, destination, paint)
            return
        }

        canvas.save()
        canvas.scale(-1f, 1f, destination.centerX(), destination.centerY())
        canvas.drawBitmap(bitmap, source, destination, paint)
        canvas.restore()
    }
}
