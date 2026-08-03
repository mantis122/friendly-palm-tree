package com.example.backyardrealms.game.theme

enum class ImaginationTheme {
    REAL,
    FANTASY;

    fun toggled(): ImaginationTheme = if (this == REAL) FANTASY else REAL
}
