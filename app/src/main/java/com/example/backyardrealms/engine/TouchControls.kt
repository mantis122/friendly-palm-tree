package com.example.backyardrealms.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.sqrt

class TouchControls(private val input: InputState) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val joystickCenterX = 72f
    private val joystickCenterY = GameConfig.LOGICAL_HEIGHT - 66f
    private val joystickRadius = 44f
    private val knobRadius = 19f
    private val actionCenterX = GameConfig.LOGICAL_WIDTH - 66f
    private val actionCenterY = GameConfig.LOGICAL_HEIGHT - 62f
    private val actionRadius = 34f
    private val developerCenterX = GameConfig.LOGICAL_WIDTH - 30f
    private val developerCenterY = 22f
    private val developerRadius = 18f
    private val inventoryCenterX = GameConfig.LOGICAL_WIDTH - 76f
    private val inventoryCenterY = 22f
    private val inventoryRadius = 18f

    private var movePointerId = MotionEvent.INVALID_POINTER_ID
    private var actionPointerId = MotionEvent.INVALID_POINTER_ID
    private var knobX = joystickCenterX
    private var knobY = joystickCenterY
    private var actionHeld = false

    fun onTouch(event: MotionEvent, viewport: Viewport): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = viewport.toLogicalX(event.getX(index))
                val y = viewport.toLogicalY(event.getY(index))
                when {
                    insideCircle(x, y, developerCenterX, developerCenterY, developerRadius) -> input.queueDeveloper()
                    insideCircle(x, y, inventoryCenterX, inventoryCenterY, inventoryRadius) -> input.queueInventory()
                    x < GameConfig.LOGICAL_WIDTH * 0.5f && movePointerId == MotionEvent.INVALID_POINTER_ID -> {
                        movePointerId = id
                        updateJoystick(x, y)
                    }
                    insideCircle(x, y, actionCenterX, actionCenterY, actionRadius) && actionPointerId == MotionEvent.INVALID_POINTER_ID -> {
                        actionPointerId = id
                        actionHeld = true
                        input.queueAction()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    if (id == movePointerId) updateJoystick(viewport.toLogicalX(event.getX(index)), viewport.toLogicalY(event.getY(index)))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> releasePointer(event.getPointerId(event.actionIndex))
            MotionEvent.ACTION_CANCEL -> reset()
        }
        return true
    }

    fun draw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = 0x55333333
        canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, paint)
        paint.color = 0x99FFFFFF.toInt()
        canvas.drawCircle(knobX, knobY, knobRadius, paint)
        paint.color = if (actionHeld) 0xCCF0B040.toInt() else 0x99D08030.toInt()
        canvas.drawCircle(actionCenterX, actionCenterY, actionRadius, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 13f
        canvas.drawText("ACTION", actionCenterX, actionCenterY + 4f, paint)
        drawSmallButton(canvas, inventoryCenterX, inventoryCenterY, "BAG")
        drawSmallButton(canvas, developerCenterX, developerCenterY, "DEV")
    }

    private fun drawSmallButton(canvas: Canvas, x: Float, y: Float, label: String) {
        paint.color = 0xAA222222.toInt(); canvas.drawCircle(x, y, 18f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textSize = 9f; canvas.drawText(label, x, y + 3f, paint)
    }

    private fun updateJoystick(x: Float, y: Float) {
        val dx = x - joystickCenterX; val dy = y - joystickCenterY
        val distance = sqrt(dx * dx + dy * dy)
        val amount = if (distance > joystickRadius && distance > 0f) joystickRadius / distance else 1f
        val limitedX = dx * amount; val limitedY = dy * amount
        knobX = joystickCenterX + limitedX; knobY = joystickCenterY + limitedY
        input.setMovement(limitedX / joystickRadius, limitedY / joystickRadius)
    }

    private fun releasePointer(id: Int) {
        if (id == movePointerId) { movePointerId = MotionEvent.INVALID_POINTER_ID; knobX = joystickCenterX; knobY = joystickCenterY; input.setMovement(0f, 0f) }
        if (id == actionPointerId) { actionPointerId = MotionEvent.INVALID_POINTER_ID; actionHeld = false }
    }

    private fun reset() {
        movePointerId = MotionEvent.INVALID_POINTER_ID; actionPointerId = MotionEvent.INVALID_POINTER_ID
        knobX = joystickCenterX; knobY = joystickCenterY; actionHeld = false; input.clear()
    }

    private fun insideCircle(x: Float, y: Float, cx: Float, cy: Float, radius: Float): Boolean {
        val dx = x - cx; val dy = y - cy
        return dx * dx + dy * dy <= radius * radius
    }
}
