package com.example.backyardrealms.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.engine.GameConfig
import com.example.backyardrealms.engine.InputSnapshot

class BackyardGame {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val worldBounds = RectF(12f, 12f, GameConfig.LOGICAL_WIDTH - 12f, GameConfig.LOGICAL_HEIGHT - 12f)
    private val player = Player(224f, 176f)

    private val fort = RectF(194f, 34f, 286f, 102f)
    private val tree = RectF(76f, 68f, 118f, 117f)
    private val shed = RectF(354f, 54f, 430f, 111f)
    private val garden = RectF(322f, 166f, 420f, 222f)
    private val sandbox = RectF(126f, 176f, 190f, 226f)
    private val obstacles = listOf(fort, tree, shed, garden, sandbox)

    fun update(dt: Float, input: InputSnapshot) {
        player.update(dt, input, obstacles, worldBounds)
    }

    fun draw(canvas: Canvas) {
        drawGround(canvas)
        drawLandmark(canvas, tree, 0xFF486B2A.toInt(), "TREE")
        drawLandmark(canvas, fort, 0xFF80552E.toInt(), "FORT")
        drawLandmark(canvas, shed, 0xFF6B645C.toInt(), "SHED")
        drawLandmark(canvas, garden, 0xFF526C30.toInt(), "GARDEN")
        drawLandmark(canvas, sandbox, 0xFFCDAE6B.toInt(), "SANDBOX")
        player.draw(canvas)
        drawDebug(canvas)
    }

    private fun drawGround(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFF79A84D.toInt()
        canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)

        paint.color = 0xFF4C7435.toInt()
        canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, 12f, paint)
        canvas.drawRect(0f, GameConfig.LOGICAL_HEIGHT - 12f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)
        canvas.drawRect(0f, 0f, 12f, GameConfig.LOGICAL_HEIGHT, paint)
        canvas.drawRect(GameConfig.LOGICAL_WIDTH - 12f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)

        paint.color = 0xFFC6A16B.toInt()
        canvas.drawRect(220f, 102f, 260f, GameConfig.LOGICAL_HEIGHT - 12f, paint)
    }

    private fun drawLandmark(canvas: Canvas, rect: RectF, color: Int, label: String) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRect(rect, paint)
        paint.color = 0xDDFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        canvas.drawText(label, rect.centerX(), rect.centerY() + 3f, paint)
    }

    private fun drawDebug(canvas: Canvas) {
        paint.color = 0xCC000000.toInt()
        canvas.drawRect(6f, 6f, 176f, 34f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        canvas.drawText("Backyard Engine 0.1", 12f, 18f, paint)
        canvas.drawText(player.positionText(), 12f, 30f, paint)
    }
}
