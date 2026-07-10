package com.example

import androidx.compose.runtime.Stable

enum class AppType {
    FILE_EXPLORER,
    NOTEPAD,
    PAINT,
    CALCULATOR,
    MINESWEEPER,
    INTERNET_EXPLORER,
    SETTINGS,
    SNAKE,
    COPILOT
}

enum class SnapState {
    NONE,
    LEFT,
    RIGHT,
    TOP
}

@Stable
data class WindowData(
    val id: Int,
    val title: String,
    val appType: AppType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isMaximized: Boolean = false,
    val isMinimized: Boolean = false,
    val zIndex: Int = 0,
    val snapState: SnapState = SnapState.NONE,
    // Store original bounds before maximize or snap, so we can restore
    val prevX: Float = x,
    val prevY: Float = y,
    val prevWidth: Float = width,
    val prevHeight: Float = height
)

data class MockFile(
    val name: String,
    val isDirectory: Boolean,
    var content: String = "",
    val children: MutableList<MockFile> = mutableListOf()
)

// Minesweeper Cells
data class MinesweeperCell(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMinesCount: Int = 0
)

enum class MinesweeperStatus {
    PLAYING,
    WON,
    LOST
}

// Paint Strokes
data class PaintStroke(
    val points: List<androidx.compose.ui.geometry.Offset>,
    val color: androidx.compose.ui.graphics.Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

enum class WallpaperType {
    WINDOWS_DEFAULT,
    CHARCOAL_DARK,
    OCEAN_GRADIENT,
    SUNSET_METRO,
    EMERALD_GREEN
}
