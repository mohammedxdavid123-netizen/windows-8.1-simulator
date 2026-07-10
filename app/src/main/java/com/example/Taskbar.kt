package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Taskbar(
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    val openWindows by viewModel.openWindows.collectAsState()
    val activeWindowId by viewModel.activeWindowId.collectAsState()
    val isStartOpen by viewModel.isStartScreenOpen.collectAsState()
    val accentColor by viewModel.metroAccentColor.collectAsState()

    // Clock state
    var clockString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }

    // Tray details popups state
    var showCalendarPopup by remember { mutableStateOf(false) }
    var showVolumePopup by remember { mutableStateOf(false) }
    var showWifiPopup by remember { mutableStateOf(false) }

    var soundLevel by remember { mutableStateOf(80f) }
    var isWifiOn by remember { mutableStateOf(true) }

    // Taskbar Preview state
    var previewWindowId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            clockString = timeFormat.format(now)
            dateString = dateFormat.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(0.dp))
            .clickable(enabled = false) {} // prevent click passing to desktop
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: START BUTTON & RUNNING APPS LIST
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Classic Windows 8.1 Angled Start Button inside a modern MD3 circular base
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isStartOpen) accentColor else Color.White.copy(alpha = 0.15f))
                        .clickable { viewModel.toggleStartScreen() }
                        .testTag("taskbar_start_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val w = size.width
                        val h = size.height
                        val midX = w / 2f
                        val midY = h / 2f
                        val gap = 1.2.dp.toPx()
                        val skew = 1.8.dp.toPx()

                        // Top-left pane: skewed left
                        drawPath(
                            path = Path().apply {
                                moveTo(0f, skew)
                                lineTo(midX - gap, 0f)
                                lineTo(midX - gap, midY - gap)
                                lineTo(0f, midY - gap)
                                close()
                            },
                            color = Color.White
                        )
                        // Bottom-left pane: skewed left
                        drawPath(
                            path = Path().apply {
                                moveTo(0f, midY + gap)
                                lineTo(midX - gap, midY + gap)
                                lineTo(midX - gap, h)
                                lineTo(0f, h - skew)
                                close()
                            },
                            color = Color.White
                        )
                        // Top-right pane: skewed right
                        drawPath(
                            path = Path().apply {
                                moveTo(midX + gap, 0f)
                                lineTo(w, skew)
                                lineTo(w, midY - gap)
                                lineTo(midX + gap, midY - gap)
                                close()
                            },
                            color = Color.White
                        )
                        // Bottom-right pane: skewed right
                        drawPath(
                            path = Path().apply {
                                moveTo(midX + gap, midY + gap)
                                lineTo(w, midY + gap)
                                lineTo(w, h - skew)
                                lineTo(midX + gap, h)
                                close()
                            },
                            color = Color.White
                        )
                    }
                }

                // 2. Running App Icons inside high-contrast Material 3 modern pills
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    openWindows.forEach { window ->
                        val isActive = activeWindowId == window.id
                        val isMinimized = window.isMinimized

                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isActive) accentColor.copy(alpha = 0.25f)
                                    else if (previewWindowId == window.id) Color.White.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) accentColor.copy(alpha = 0.5f)
                                            else if (previewWindowId == window.id) Color.White.copy(alpha = 0.3f)
                                            else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    if (isActive) {
                                        if (previewWindowId == window.id) {
                                            viewModel.minimizeWindow(window.id)
                                            previewWindowId = null
                                        } else {
                                            previewWindowId = window.id
                                        }
                                    } else {
                                        viewModel.restoreMinimizedWindow(window.id)
                                        previewWindowId = window.id
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("taskbar_app_${window.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                contentDescription = window.title,
                                tint = if (isMinimized) Color.Gray else Color.White,
                                modifier = Modifier.size(20.dp)
                            )

                            // Small indicator dot
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (isActive) accentColor else if (!isMinimized) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // RIGHT: SYSTEM TRAY & STATUS PILLS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Pill
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable {
                            // Focus Settings / Search
                            viewModel.toggleStartScreen()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Wifi / Volume / Battery small quick status triggers
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isWifiOn) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Wi-Fi Status",
                        tint = if (isWifiOn) Color.White else Color.Red,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable {
                                showWifiPopup = !showWifiPopup
                                showVolumePopup = false
                                showCalendarPopup = false
                            }
                    )

                    Icon(
                        imageVector = if (soundLevel == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Volume Status",
                        tint = Color.White,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable {
                                showVolumePopup = !showVolumePopup
                                showWifiPopup = false
                                showCalendarPopup = false
                            }
                    )

                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery Status",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // ONLINE pulsing status pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                            .background(Color(0xFF4ADE80), CircleShape)
                    )
                    Text(
                        text = "ONLINE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Date & Time Column
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                        .clickable {
                            showCalendarPopup = !showCalendarPopup
                            showVolumePopup = false
                            showWifiPopup = false
                        },
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        clockString,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        dateString,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // --- TASKBAR FLOATING APP WINDOW PREVIEW CARD ---
        openWindows.forEach { window ->
            if (previewWindowId == window.id) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 64.dp, bottom = 72.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(120.dp)
                            .shadow(12.dp, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Preview Title Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = window.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.closeWindow(window.id)
                                        previewWindowId = null
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFFFF5F56),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Miniature Mock Screen Preview Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        viewModel.restoreMinimizedWindow(window.id)
                                        previewWindowId = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (window.appType) {
                                        AppType.PAINT -> Icons.Default.Brush
                                        AppType.CALCULATOR -> Icons.Default.Calculate
                                        AppType.MINESWEEPER -> Icons.Default.Gamepad
                                        AppType.FILE_EXPLORER -> Icons.Default.FolderOpen
                                        AppType.INTERNET_EXPLORER -> Icons.Default.Language
                                        AppType.SNAKE -> Icons.Default.Gamepad
                                        AppType.COPILOT -> Icons.Default.Assistant
                                        else -> Icons.Default.WebAsset
                                    },
                                    contentDescription = null,
                                    tint = accentColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- POPUPS TRAY WINDOWS (UPGRADED WITH MODERN DESIGN) ---
        // Wi-Fi Status Tray Pop-Up
        if (showWifiPopup) {
            TrayPopupContainer(
                title = "Wireless Connection",
                onClose = { showWifiPopup = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 56.dp, bottom = 72.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wi-Fi Adapter", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Switch(
                            checked = isWifiOn,
                            onCheckedChange = { isWifiOn = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isWifiOn) "Connected to:\nMobile_Simulator_Net" else "Disconnected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWifiOn) Color(0xFF4ADE80) else Color.Gray
                    )
                }
            }
        }

        // Volume Level Slider Pop-Up
        if (showVolumePopup) {
            TrayPopupContainer(
                title = "System Volume",
                onClose = { showVolumePopup = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 32.dp, bottom = 72.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Volume: ${soundLevel.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = soundLevel,
                        onValueChange = { soundLevel = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }

        // System Calendar Pop-Up
        if (showCalendarPopup) {
            TrayPopupContainer(
                title = "Date & Calendar",
                onClose = { showCalendarPopup = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 72.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(clockString, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(dateString, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("July 2026", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple calendar grid (July 2026)
                    val days = (1..31).toList()
                    val weeks = days.chunked(7)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        weeks.forEach { week ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { day ->
                                    val isToday = (day == 10)
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isToday) accentColor else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            day.toString(),
                                            fontSize = 11.sp,
                                            color = if (isToday) Color.White else Color.LightGray,
                                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrayPopupContainer(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111112))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                IconButton(onClick = onClose, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(11.dp))
                }
            }
            Divider(color = Color.White.copy(alpha = 0.1f))
            content()
        }
    }
}
