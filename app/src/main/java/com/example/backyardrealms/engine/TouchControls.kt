package com.example.backyardrealms.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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

                if (x < GameConfig.LOGICAL_WIDTH * 0.5f && movePointerId == MotionEvent.INVALID_POINTER_ID) {
                    movePointerId = id
                    updateJoystick(x, y)
                } else if (insideAction(x, y) && actionPointerId == MotionEvent.INVALID_POINTER_ID) {
                    actionPointerId = id
                    actionHeld = true
                    input.queueAction()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val x = viewport.toLogicalX(event.getX(index))
                    val y = viewport.toLogicalY(event.getY(index))
                    if (id == movePointerId) updateJoystick(x, y)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                releasePointer(event.getPointerId(event.actionIndex))
            }

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
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 13f
        canvas.drawText("ACTION", actionCenterX, actionCenterY + 4f, paint)
    }

    private fun updateJoystick(x: Float, y: Float) {
        val dx = x - joystickCenterX
        val dy = y - joystickCenterY
        val distance = sqrt(dx * dx + dy * dy)
        val amount = if (distance > joystickRadius && distance > 0f) joystickRadius / distance else 1f
        val limitedX = dx * amount
        val limitedY = dy * amount
        knobX = joystickCenterX + limitedX
        knobY = joystickCenterY + limitedY
        input.setMovement(limitedX / joystickRadius, limitedY / joystickRadius)
    }

    private fun releasePointer(id: Int) {
        if (id == movePointerId) {
            movePointerId = MotionEvent.INVALID_POINTER_ID
            knobX = joystickCenterX
            knobY = joystickCenterY
            input.setMovement(0f, 0f)
        }
        if (id == actionPointerId) {
            actionPointerId = MotionEvent.INVALID_POINTER_ID
            actionHeld = false
        }
    }

    private fun reset() {
        movePointerId = MotionEvent.INVALID_POINTER_ID
        actionPointerId = MotionEvent.INVALID_POINTER_ID
        knobX = joystickCenterX
        knobY = joystickCenterY
        actionHeld = false
        input.clear()
    }

    private fun insideAction(x: Float, y: Float): Boolean {
        val dx = x - actionCenterX
        val dy = y - actionCenterY
        return dx * dx + dy * dy <= actionRadius * actionRadius
    }
}
