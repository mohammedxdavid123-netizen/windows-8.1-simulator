package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Desktop Shortcut definition
data class DesktopShortcut(
    val name: String,
    val appType: AppType,
    val icon: ImageVector,
    val iconColor: Color,
    val initialPath: List<String>? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    val openWindows by viewModel.openWindows.collectAsState()
    val activeWindowId by viewModel.activeWindowId.collectAsState()
    val wallpaperType by viewModel.wallpaper.collectAsState()
    val hoverSnapState by viewModel.hoveredSnapGuide.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { (configuration.screenHeightDp - 48).dp.toPx() } // account for 48dp taskbar

    // Desktop icons list
    val shortcuts = listOf(
        DesktopShortcut("This PC", AppType.FILE_EXPLORER, Icons.Default.Computer, Color(0xFF0078D7), listOf("C:")),
        DesktopShortcut("File Explorer", AppType.FILE_EXPLORER, Icons.Default.FolderOpen, Color(0xFFFFC107)),
        DesktopShortcut("Recycle Bin", AppType.FILE_EXPLORER, Icons.Default.Delete, Color(0xFF90A4AE), listOf("C:")),
        DesktopShortcut("Internet Explorer", AppType.INTERNET_EXPLORER, Icons.Default.Language, Color(0xFF0288D1)),
        DesktopShortcut("Notepad", AppType.NOTEPAD, Icons.Default.Description, Color(0xFF607D8B)),
        DesktopShortcut("Paint", AppType.PAINT, Icons.Default.Brush, Color(0xFFE91E63)),
        DesktopShortcut("Minesweeper", AppType.MINESWEEPER, Icons.Default.Gamepad, Color(0xFF4CAF50)),
        DesktopShortcut("Settings", AppType.SETTINGS, Icons.Default.Settings, Color(0xFF455A64)),
        DesktopShortcut("Copilot", AppType.COPILOT, Icons.Default.Assistant, Color(0xFF6366F1))
    )

    var selectedShortcutName by remember { mutableStateOf<String?>(null) }

    // Desktop Procedural Wallpaper Brush selection
    val wallpaperBrush = when (wallpaperType) {
        WallpaperType.WINDOWS_DEFAULT -> Brush.linearGradient(
            colors = listOf(Color(0xFF1A5FB4), Color(0xFF0B4C95), Color(0xFF052957)),
            start = Offset(0f, 0f),
            end = Offset(screenWidthPx, screenHeightPx)
        )
        WallpaperType.CHARCOAL_DARK -> Brush.linearGradient(
            colors = listOf(Color(0xFF1F1F1F), Color(0xFF111111), Color(0xFF050505)),
            start = Offset(0f, 0f),
            end = Offset(screenWidthPx, screenHeightPx)
        )
        WallpaperType.OCEAN_GRADIENT -> Brush.radialGradient(
            colors = listOf(Color(0xFF00A2E8), Color(0xFF005080), Color(0xFF001A30)),
            center = Offset(screenWidthPx / 2f, screenHeightPx / 2f)
        )
        WallpaperType.SUNSET_METRO -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF4E50), Color(0xFFF9D423)),
            start = Offset(0f, 0f),
            end = Offset(screenWidthPx, screenHeightPx)
        )
        WallpaperType.EMERALD_GREEN -> Brush.linearGradient(
            colors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50), Color(0xFF009688)),
            start = Offset(0f, 0f),
            end = Offset(screenWidthPx, screenHeightPx)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(brush = wallpaperBrush) }
            .clickable { selectedShortcutName = null }
    ) {
        val widthDp = maxWidth
        val heightDp = maxHeight

        // 1. Desktop Shortcuts Grid (Left-Side columns layout)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            items(shortcuts) { shortcut ->
                val isSelected = selectedShortcutName == shortcut.name
                var lastTapTime by remember { mutableStateOf(0L) }

                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .pointerInput(shortcut.name) {
                            detectDragGestures(
                                onDrag = { _, _ -> },
                                onDragEnd = {}
                            )
                        }
                        .combinedClickable(
                            onClick = {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastTapTime < 300) {
                                    // Double Tap Launches App
                                    viewModel.openWindow(
                                        appType = shortcut.appType,
                                        title = if (shortcut.appType == AppType.NOTEPAD) "Untitled - Notepad" else shortcut.name,
                                        filePath = shortcut.initialPath
                                    )
                                } else {
                                    selectedShortcutName = shortcut.name
                                }
                                lastTapTime = currentTime
                            }
                        )
                        .padding(6.dp)
                        .testTag("desktop_shortcut_${shortcut.name}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = shortcut.icon,
                        contentDescription = shortcut.name,
                        tint = shortcut.iconColor,
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(2.dp), ambientColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shortcut.name,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.sp,
                        modifier = Modifier.shadow(1.dp)
                    )
                }
            }
        }

        // 2. WINDOWS LAYER (Draggable, Resizable Floating Windows)
        openWindows.forEach { window ->
            if (!window.isMinimized) {
                val isActive = activeWindowId == window.id

                // Calculate display coordinates & sizes based on Window State (maximize, snap, default)
                val windowX = when (window.snapState) {
                    SnapState.LEFT -> 0.dp
                    SnapState.RIGHT -> widthDp / 2f
                    SnapState.TOP -> 0.dp
                    SnapState.NONE -> if (window.isMaximized) 0.dp else window.x.dp
                }

                val windowY = when (window.snapState) {
                    SnapState.TOP -> 0.dp
                    else -> if (window.isMaximized) 0.dp else window.y.dp
                }

                val windowWidth = when (window.snapState) {
                    SnapState.LEFT, SnapState.RIGHT -> widthDp / 2f
                    SnapState.TOP -> widthDp
                    SnapState.NONE -> if (window.isMaximized) widthDp else window.width.dp
                }

                val windowHeight = when (window.snapState) {
                    SnapState.LEFT, SnapState.RIGHT, SnapState.TOP -> heightDp
                    SnapState.NONE -> if (window.isMaximized) heightDp else window.height.dp
                }

                // Window container
                Card(
                    modifier = Modifier
                        .offset(x = windowX, y = windowY)
                        .size(width = windowWidth, height = windowHeight)
                        .shadow(if (isActive) 18.dp else 4.dp, shape = RoundedCornerShape(16.dp))
                        .pointerInput(window.id) {
                            // Click focusing window inside container
                            detectDragGestures(
                                onDragStart = { viewModel.focusWindow(window.id) },
                                onDrag = { _, _ -> }
                            )
                        }
                        .testTag("window_${window.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
                    border = BorderStroke(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) viewModel.metroAccentColor.value else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // WINDOWS TITLE BAR Shell
                        var lastTitleTapTime by remember { mutableStateOf(0L) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(if (isActive) viewModel.metroAccentColor.value else Color(0xFFE0E0E0))
                                .pointerInput(window.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            viewModel.focusWindow(window.id)
                                            viewModel.startWindowDrag(window.id)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            viewModel.updateWindowPosition(window.id, dragAmount.x / density.density, dragAmount.y / density.density)
                                            
                                            // Handle snapping guide previews while dragging
                                            val globalTouchX = windowX.toPx() + change.position.x
                                            val globalTouchY = windowY.toPx() + change.position.y
                                            viewModel.handleWindowDrag(
                                                window.id,
                                                globalTouchX,
                                                globalTouchY,
                                                screenWidthPx,
                                                screenHeightPx
                                            )
                                        },
                                        onDragEnd = {
                                            viewModel.endWindowDrag(window.id)
                                        }
                                    )
                                }
                                .combinedClickable(
                                    onClick = {
                                        val curTime = System.currentTimeMillis()
                                        if (curTime - lastTitleTapTime < 300) {
                                            viewModel.toggleMaximizeWindow(window.id)
                                        } else {
                                            viewModel.focusWindow(window.id)
                                        }
                                        lastTitleTapTime = curTime
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Title & App Icon
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (window.appType) {
                                        AppType.FILE_EXPLORER -> Icons.Default.FolderOpen
                                        AppType.NOTEPAD -> Icons.Default.Description
                                        AppType.PAINT -> Icons.Default.Brush
                                        AppType.CALCULATOR -> Icons.Default.Calculate
                                        AppType.MINESWEEPER -> Icons.Default.Gamepad
                                        AppType.INTERNET_EXPLORER -> Icons.Default.Language
                                        AppType.SETTINGS -> Icons.Default.Settings
                                        AppType.SNAKE -> Icons.Default.Gamepad
                                        AppType.COPILOT -> Icons.Default.Assistant
                                    },
                                    contentDescription = null,
                                    tint = if (isActive) Color.White else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = window.title.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = if (isActive) Color.White else Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Window buttons: Min, Max, Close (Colored Circles MacOS/Win8 Hybrid Style)
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Minimize (Green Dot)
                                IconButton(
                                    onClick = { viewModel.minimizeWindow(window.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("window_minimize_${window.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color(0xFF27C93F) else Color.Gray.copy(alpha = 0.4f))
                                    )
                                }
                                // Maximize (Yellow Dot)
                                IconButton(
                                    onClick = { viewModel.toggleMaximizeWindow(window.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("window_maximize_${window.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color(0xFFFFBD2E) else Color.Gray.copy(alpha = 0.4f))
                                    )
                                }
                                // Close (Red Dot)
                                IconButton(
                                    onClick = { viewModel.closeWindow(window.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("window_close_${window.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color(0xFFFF5F56) else Color.Gray.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }

                        // App Content Area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(Color.White)
                        ) {
                            AppContent(
                                appType = window.appType,
                                windowId = window.id,
                                viewModel = viewModel
                            )

                            // Draggable Resize Handles at Bottom-Right corner
                            if (window.snapState == SnapState.NONE && !window.isMaximized) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(12.dp)
                                        .pointerInput(window.id) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                viewModel.updateWindowSize(
                                                    window.id,
                                                    dragAmount.x / density.density,
                                                    dragAmount.y / density.density,
                                                    screenWidthPx / density.density,
                                                    screenHeightPx / density.density
                                                )
                                            }
                                        }
                                        .drawBehind {
                                            // Draw typical small diagonal lines resizing grip
                                            drawLine(Color.Gray, Offset(this.size.width, 0f), Offset(0f, this.size.height), strokeWidth = 1.5f)
                                            drawLine(Color.Gray, Offset(this.size.width, this.size.height * 0.4f), Offset(this.size.width * 0.4f, this.size.height), strokeWidth = 1.5f)
                                            drawLine(Color.Gray, Offset(this.size.width, this.size.height * 0.8f), Offset(this.size.width * 0.8f, this.size.height), strokeWidth = 1.5f)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. WINDOW SNAPPING SEMI-TRANSPARENT BLUE PREVIEW GUIDES
        AnimatedVisibility(
            visible = hoverSnapState != SnapState.NONE,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            val previewX = when (hoverSnapState) {
                SnapState.LEFT -> 0.dp
                SnapState.RIGHT -> widthDp / 2f
                else -> 0.dp
            }
            val previewWidth = when (hoverSnapState) {
                SnapState.LEFT, SnapState.RIGHT -> widthDp / 2f
                else -> widthDp
            }
            Box(
                modifier = Modifier
                    .offset(x = previewX, y = 0.dp)
                    .size(width = previewWidth, height = heightDp)
                    .background(Color(0xFF3399FF).copy(alpha = 0.35f))
                    .border(BorderStroke(2.dp, Color(0xFF3399FF)))
            )
        }
    }
}
