package com.example.backyardrealms.engine

import kotlin.math.sqrt

data class InputSnapshot(
    val moveX: Float,
    val moveY: Float,
    val actionPressed: Boolean
)

class InputState {
    private var moveX = 0f
    private var moveY = 0f
    private var actionQueued = false

    @Synchronized
    fun setMovement(x: Float, y: Float) {
        val length = sqrt(x * x + y * y)
        if (length > 1f) {
            moveX = x / length
            moveY = y / length
        } else {
            moveX = x
            moveY = y
        }
    }

    @Synchronized
    fun queueAction() {
        actionQueued = true
    }

    @Synchronized
    fun snapshot(): InputSnapshot {
        val result = InputSnapshot(moveX, moveY, actionQueued)
        actionQueued = false
        return result
    }

    @Synchronized
    fun clear() {
        moveX = 0f
        moveY = 0f
        actionQueued = false
    }
}
