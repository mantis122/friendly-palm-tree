package com.example.backyardrealms.game.theme

class ThemeTransition(private val halfDurationSeconds: Float = 0.42f) {
    var alpha: Float = 0f
        private set
    var isActive: Boolean = false
        private set

    private var elapsed = 0f
    private var midpointReached = false
    private var onMidpoint: (() -> Unit)? = null

    fun start(midpointAction: () -> Unit) {
        if (isActive) return
        isActive = true
        elapsed = 0f
        alpha = 0f
        midpointReached = false
        onMidpoint = midpointAction
    }

    fun update(dt: Float) {
        if (!isActive) return
        elapsed += dt
        if (elapsed < halfDurationSeconds) {
            alpha = (elapsed / halfDurationSeconds).coerceIn(0f, 1f)
            return
        }
        if (!midpointReached) {
            midpointReached = true
            onMidpoint?.invoke()
        }
        alpha = (1f - (elapsed - halfDurationSeconds) / halfDurationSeconds).coerceIn(0f, 1f)
        if (elapsed >= halfDurationSeconds * 2f) {
            alpha = 0f
            isActive = false
            onMidpoint = null
        }
    }
}
