package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Model for Metro Tiles
data class MetroTile(
    val name: String,
    val appType: AppType,
    val icon: ImageVector,
    val tileColor: Color,
    val isWide: Boolean = false,
    val liveContent: (@Composable () -> Unit)? = null
)

@Composable
fun StartScreen(
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    val accentColor by viewModel.metroAccentColor.collectAsState()
    var isAllAppsViewOpen by remember { mutableStateOf(false) }

    // Dynamic Live State for Clock Live Tile
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }

    // Dynamic photos rotation state
    var photoIndex by remember { mutableStateOf(0) }
    val photoGradients = listOf(
        Brush.linearGradient(colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))),
        Brush.linearGradient(colors = listOf(Color(0xFF2196F3), Color(0xFF00BCD4))),
        Brush.linearGradient(colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))),
        Brush.linearGradient(colors = listOf(Color(0xFF9C27B0), Color(0xFFE040FB)))
    )

    LaunchedEffect(Unit) {
        val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            timeString = clockFormat.format(now)
            dateString = dateFormat.format(now)
            // Rotate photo tile background every 5 seconds
            if (System.currentTimeMillis() % 5000 < 1000) {
                photoIndex = (photoIndex + 1) % photoGradients.size
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    val tilesGroup1 = listOf(
        MetroTile("Desktop", AppType.FILE_EXPLORER, Icons.Default.Computer, Color(0xFF3B5998), isWide = true) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Procedural grid pattern matching default Win8 Desktop theme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF005A9E), Color(0xFF0078D7))
                            )
                        )
                )
                Text(
                    "Desktop",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                )
            }
        },
        MetroTile("Internet Explorer", AppType.INTERNET_EXPLORER, Icons.Default.Language, Color(0xFF00A2E8)),
        MetroTile("Notepad", AppType.NOTEPAD, Icons.Default.Description, Color(0xFF00B294)),
        MetroTile("PC Settings", AppType.SETTINGS, Icons.Default.Settings, Color(0xFF455A64), isWide = true) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Change Wallpaper", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Personalize Start Screen Color", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }
    )

    val tilesGroup2 = listOf(
        MetroTile("Calendar & Time", AppType.NOTEPAD, Icons.Default.CalendarToday, Color(0xFFD13438), isWide = true) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(timeString, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                Text(dateString, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Normal)
                Text("Today", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        },
        MetroTile("Calculator", AppType.CALCULATOR, Icons.Default.Calculate, Color(0xFF7A7A7A)),
        MetroTile("Paint", AppType.PAINT, Icons.Default.Brush, Color(0xFFC30052)),
        MetroTile("Minesweeper", AppType.MINESWEEPER, Icons.Default.Gamepad, Color(0xFF107C41), isWide = true) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Gamepad, contentDescription = "Mines", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Minesweeper Game", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Classic grid. Clear bombs to win!", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        },
        MetroTile("Snake", AppType.SNAKE, Icons.Default.Gamepad, Color(0xFF00529C), isWide = true) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Gamepad, contentDescription = "Snake", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Snake Game", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Retro fun. Guide the blue snake!", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        },
        MetroTile("Copilot", AppType.COPILOT, Icons.Default.Assistant, Color(0xFF6366F1), isWide = true) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Assistant, contentDescription = "Copilot", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Copilot AI", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("AI-powered Gemini smart assistant", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
    )

    val tilesGroup3 = listOf(
        MetroTile("Live Photos", AppType.PAINT, Icons.Default.PhotoLibrary, Color(0xFF555555), isWide = true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(photoGradients[photoIndex])
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photos Live", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        MetroTile("Weather", AppType.INTERNET_EXPLORER, Icons.Default.WbSunny, Color(0xFF2196F3), isWide = false) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = "Sunny", tint = Color.Yellow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Sunny 72°", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Seattle", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
            }
        },
        MetroTile("Help Guide", AppType.FILE_EXPLORER, Icons.Default.Help, Color(0xFF0078D7))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(accentColor) // custom Metro Background Color chosen by settings!
            .padding(top = 40.dp, bottom = 12.dp)
    ) {
        if (!isAllAppsViewOpen) {
            // MAIN HORIZONTAL TILES SCROLL VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // START HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "START",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp,
                        color = Color.White
                    )

                    // User Profile on right
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MOHAMMED DAVID",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "User Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Power button
                        IconButton(
                            onClick = { viewModel.setStartScreenOpen(false) },
                            modifier = Modifier.size(28.dp).testTag("start_power_off")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Shutdown View", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tiles horizontal scroll groups
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // Group 1 column
                    TileGroupColumn("Essentials", tilesGroup1, viewModel)

                    Spacer(modifier = Modifier.width(28.dp))

                    // Group 2 column
                    TileGroupColumn("Tools & Games", tilesGroup2, viewModel)

                    Spacer(modifier = Modifier.width(28.dp))

                    // Group 3 column
                    TileGroupColumn("Media & Weather", tilesGroup3, viewModel)
                }

                // Arrow pointing down to toggle All Apps
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isAllAppsViewOpen = true },
                        modifier = Modifier.testTag("start_show_all_apps")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Show All Apps",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {
            // ALL APPS GRID LIST (VERTICAL VIEW)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apps by name",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { isAllAppsViewOpen = false },
                        modifier = Modifier.testTag("start_back_to_tiles")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val allAppsList = listOf(
                    "Calculator" to (AppType.CALCULATOR to Icons.Default.Calculate),
                    "File Explorer" to (AppType.FILE_EXPLORER to Icons.Default.FolderOpen),
                    "Internet Explorer" to (AppType.INTERNET_EXPLORER to Icons.Default.Language),
                    "Minesweeper" to (AppType.MINESWEEPER to Icons.Default.Gamepad),
                    "Snake" to (AppType.SNAKE to Icons.Default.Gamepad),
                    "Copilot" to (AppType.COPILOT to Icons.Default.Assistant),
                    "Notepad" to (AppType.NOTEPAD to Icons.Default.Description),
                    "Paint" to (AppType.PAINT to Icons.Default.Brush),
                    "PC Settings" to (AppType.SETTINGS to Icons.Default.Settings)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allAppsList) { (name, pair) ->
                        val (appType, icon) = pair
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isAllAppsViewOpen = false
                                    viewModel.openWindow(appType, name)
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(name, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TileGroupColumn(
    groupTitle: String,
    tiles: List<MetroTile>,
    viewModel: SystemViewModel
) {
    Column(modifier = Modifier.width(260.dp)) {
        Text(
            text = groupTitle.uppercase(),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tiles flow helper: Windows 8.1 has side by side layout.
            // We can layout tiles by grouping them manually or using Rows.
            // To make it look gorgeous: we can iterate and render wide or narrow tiles correctly.
            var i = 0
            while (i < tiles.size) {
                val tile = tiles[i]
                if (tile.isWide) {
                    // Wide tile spans entire row width
                    TileView(tile = tile, viewModel = viewModel, modifier = Modifier.fillMaxWidth().height(110.dp))
                    i++
                } else {
                    // Check if there is a second square tile to pair with
                    if (i + 1 < tiles.size && !tiles[i + 1].isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TileView(tile = tile, viewModel = viewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                            TileView(tile = tiles[i + 1], viewModel = viewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                        i += 2
                    } else {
                        // Single square tile on row
                        Row(
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TileView(tile = tile, viewModel = viewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        i++
                    }
                }
            }
        }
    }
}

@Composable
fun TileView(
    tile: MetroTile,
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, shape = RoundedCornerShape(12.dp))
            .clickable {
                viewModel.openWindow(
                    appType = tile.appType,
                    title = if (tile.appType == AppType.NOTEPAD) "Untitled - Notepad" else tile.name,
                    filePath = if (tile.name == "Desktop") listOf("C:", "Users", "User", "Desktop") else null
                )
            }
            .testTag("metro_tile_${tile.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tile.tileColor),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        if (tile.liveContent != null) {
            tile.liveContent.invoke()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.name,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )

                Text(
                    text = tile.name.uppercase(),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}
