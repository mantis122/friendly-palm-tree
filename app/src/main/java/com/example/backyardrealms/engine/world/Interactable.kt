package com.example.backyardrealms.engine.world

import android.graphics.RectF

interface Interactable {
    val interactionBounds: RectF
    fun interactionText(): String
}
