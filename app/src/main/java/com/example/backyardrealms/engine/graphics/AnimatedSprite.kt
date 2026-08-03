package com.example.backyardrealms.engine.graphics

class AnimatedSprite(
    private val frames: IntArray,
    private val secondsPerFrame: Float,
    private val loop: Boolean = true
) {
    private var elapsed = 0f
    private var framePosition = 0

    val currentFrame: Int
        get() = frames[framePosition]

    fun update(dt: Float) {
        if (frames.size <= 1 || secondsPerFrame <= 0f) return
        elapsed += dt
        while (elapsed >= secondsPerFrame) {
            elapsed -= secondsPerFrame
            if (framePosition < frames.lastIndex) {
                framePosition++
            } else if (loop) {
                framePosition = 0
            }
        }
    }

    fun reset() {
        elapsed = 0f
        framePosition = 0
    }
}
