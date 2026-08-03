package com.example.backyardrealms.engine.audio

import com.example.backyardrealms.game.theme.ImaginationTheme

/** Asset-independent audio routing. Real sound assets can be assigned without changing game systems. */
class GameAudio {
    var ambienceName: String = "neighborhood afternoon"
        private set
    var lastCue: String = "none"
        private set

    fun setTheme(theme: ImaginationTheme) {
        ambienceName = if (theme == ImaginationTheme.REAL) "neighborhood afternoon" else "enchanted wind"
        lastCue = "ambience:$ambienceName"
    }
    fun footstep(surface: String) { lastCue = "footstep:$surface" }
    fun interaction(id: String) { lastCue = "interact:$id" }
    fun attack() { lastCue = "stick swing" }
}
