package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.gestures.detectTapGestures
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN ROUTER COMPOSE ---
@Composable
fun AppContent(
    appType: AppType,
    windowId: Int,
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        when (appType) {
            AppType.FILE_EXPLORER -> FileExplorerApp(windowId = windowId, viewModel = viewModel)
            AppType.NOTEPAD -> NotepadApp(windowId = windowId, viewModel = viewModel)
            AppType.PAINT -> PaintApp(windowId = windowId, viewModel = viewModel)
            AppType.CALCULATOR -> CalculatorApp(windowId = windowId, viewModel = viewModel)
            AppType.MINESWEEPER -> MinesweeperApp(windowId = windowId, viewModel = viewModel)
            AppType.INTERNET_EXPLORER -> InternetExplorerApp(windowId = windowId, viewModel = viewModel)
            AppType.SETTINGS -> SettingsApp(viewModel = viewModel)
            AppType.SNAKE -> SnakeApp(windowId = windowId, viewModel = viewModel)
            AppType.COPILOT -> CopilotApp(windowId = windowId, viewModel = viewModel)
        }
    }
}

// --- FILE EXPLORER APP ---
@Composable
fun FileExplorerApp(windowId: Int, viewModel: SystemViewModel) {
    val currentPath by viewModel.explorerPaths.collectAsState()
    val path = currentPath[windowId] ?: listOf("C:", "Users", "User", "Desktop")
    val selectedFile by viewModel.selectedFileNames.collectAsState()
    val activeSelection = selectedFile[windowId]

    val dir = viewModel.getDirectoryAtPath(path)
    val files = dir?.children ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar / Address Bar Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F3F3))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBackExplorer(windowId) },
                enabled = path.size > 1,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp),
                    tint = if (path.size > 1) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Address Breadcrumb
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFDCDCDC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Folder",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = path.joinToString(" > "),
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Control Ribbon
            IconButton(
                onClick = { viewModel.createFolderInExplorer(windowId) },
                modifier = Modifier.size(30.dp).testTag("explorer_new_folder")
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "New Folder",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { viewModel.createFileInExplorer(windowId) },
                modifier = Modifier.size(30.dp).testTag("explorer_new_file")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                    contentDescription = "New Text Document",
                    tint = Color(0xFF555555),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { viewModel.deleteSelectedFileInExplorer(windowId) },
                enabled = activeSelection != null,
                modifier = Modifier.size(30.dp).testTag("explorer_delete")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Selected",
                    tint = if (activeSelection != null) Color.Red else Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)

        Row(modifier = Modifier.weight(1f)) {
            // Sidebar Navigation
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFAFAFA))
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = "Favorites",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                val sidebarItems = listOf(
                    "Desktop" to listOf("C:", "Users", "User", "Desktop"),
                    "Documents" to listOf("C:", "Users", "User", "Documents"),
                    "Downloads" to listOf("C:", "Users", "User", "Downloads"),
                    "Pictures" to listOf("C:", "Users", "User", "Pictures"),
                    "This PC (C:)" to listOf("C:")
                )
                sidebarItems.forEach { (name, itemPath) ->
                    val isSelected = path == itemPath
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) Color(0xFFE5F3FF) else Color.Transparent)
                            .clickable {
                                viewModel.navigateExplorer(windowId, "") // trigger path update
                                viewModel.selectFileInExplorer(windowId, null)
                                // direct override
                                val curPaths = viewModel.explorerPaths.value.toMutableMap()
                                curPaths[windowId] = itemPath
                                // update
                                val field = viewModel.javaClass.getDeclaredField("_explorerPaths")
                                field.isAccessible = true
                                (field.get(viewModel) as MutableStateFlow<Map<Int, List<String>>>).value = curPaths
                            }
                            .padding(vertical = 5.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (name.startsWith("This PC")) Icons.Default.Computer else Icons.Default.Folder,
                            contentDescription = name,
                            tint = if (name.startsWith("This PC")) Color(0xFF0078D7) else Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFE5E5E5))

            // Main Contents Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White)
                    .clickable { viewModel.selectFileInExplorer(windowId, null) }
            ) {
                if (files.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Empty Folder",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This folder is empty.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 75.dp),
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    ) {
                        items(files) { file ->
                            val isFileSelected = activeSelection == file.name
                            var lastTapTime by remember { mutableStateOf(0L) }

                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(width = 75.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isFileSelected) Color(0xFFCCE8FF)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isFileSelected) Color(0xFF99D1FF) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .pointerInput(file.name) {
                                        detectTapGestures(
                                            onTap = {
                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastTapTime < 300) {
                                                    // Double tap detected
                                                    if (file.isDirectory) {
                                                        viewModel.navigateExplorer(windowId, file.name)
                                                    } else {
                                                        // Open file in Notepad!
                                                        viewModel.openWindow(
                                                            AppType.NOTEPAD,
                                                            "${file.name} - Notepad",
                                                            path + file.name
                                                        )
                                                    }
                                                } else {
                                                    // Single tap
                                                    viewModel.selectFileInExplorer(windowId, file.name)
                                                }
                                                lastTapTime = currentTime
                                            },
                                            onLongPress = {
                                                viewModel.selectFileInExplorer(windowId, file.name)
                                            }
                                        )
                                    }
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                                    contentDescription = file.name,
                                    tint = if (file.isDirectory) Color(0xFFFFC107) else Color(0xFF0078D7),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = file.name,
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- NOTEPAD APP ---
@Composable
fun NotepadApp(windowId: Int, viewModel: SystemViewModel) {
    val notepadStates by viewModel.notepadFiles.collectAsState()
    val noteState = notepadStates[windowId] ?: Pair(null, "")
    val text = noteState.second

    Column(modifier = Modifier.fillMaxSize()) {
        // Notepad Top Bar File Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F3F3))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.saveNotepadFile(windowId) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("notepad_save")
            ) {
                Text("File: Save", color = Color.Black, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (noteState.first != null) noteState.first!!.last() else "Untitled document*",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
        Divider(color = Color(0xFFDFDFDF), thickness = 1.dp)

        // Monospace text field
        BasicTextField(
            value = text,
            onValueChange = { viewModel.updateNotepadText(windowId, it) },
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp)
                .testTag("notepad_text_field")
        )
    }
}

// --- PAINT APP ---
@Composable
fun PaintApp(windowId: Int, viewModel: SystemViewModel) {
    val paintStrokesMap by viewModel.paintStrokes.collectAsState()
    val strokes = paintStrokesMap[windowId] ?: emptyList()

    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentWidth by remember { mutableStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    val colorsList = listOf(
        Color.Black, Color.DarkGray, Color.Red, Color.Magenta, Color.Blue,
        Color(0xFF0078D7), Color(0xFF00A2E8), Color(0xFF22B14C), Color(0xFF81C784),
        Color(0xFFFFC90E), Color(0xFFFF7F27), Color(0xFF880015)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Draw Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tool selector
            IconButton(
                onClick = { isEraser = false },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (!isEraser) Color(0xFFD0D0D0) else Color.Transparent
                ),
                modifier = Modifier.size(32.dp).testTag("paint_brush")
            ) {
                Icon(Icons.Default.Brush, contentDescription = "Brush", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { isEraser = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isEraser) Color(0xFFD0D0D0) else Color.Transparent
                ),
                modifier = Modifier.size(32.dp).testTag("paint_eraser")
            ) {
                Icon(Icons.Default.BorderColor, contentDescription = "Eraser", modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stroke slider
            Column(modifier = Modifier.width(80.dp)) {
                Text("Width: ${currentWidth.toInt()}px", fontSize = 9.sp, color = Color.Black)
                Slider(
                    value = currentWidth,
                    onValueChange = { currentWidth = it },
                    valueRange = 2f..24f,
                    modifier = Modifier.height(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Save & Clear
            IconButton(
                onClick = { viewModel.savePaintImage(windowId) },
                modifier = Modifier.size(32.dp).testTag("paint_save")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Drawing", modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { viewModel.clearPaint(windowId) },
                modifier = Modifier.size(32.dp).testTag("paint_clear")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Clear Canvas", modifier = Modifier.size(16.dp), tint = Color.Red)
            }
        }

        // Color Palette
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8E8E8))
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorsList.forEach { col ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(20.dp)
                        .background(col, CircleShape)
                        .border(
                            width = if (currentColor == col && !isEraser) 2.dp else 1.dp,
                            color = if (currentColor == col && !isEraser) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable {
                            currentColor = col
                            isEraser = false
                        }
                )
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Canvas Area
        var activePoints = remember { mutableStateListOf<Offset>() }

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .pointerInput(windowId) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activePoints.clear()
                            activePoints.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPoint = activePoints.lastOrNull()?.let { it + dragAmount } ?: change.position
                            activePoints.add(newPoint)
                        },
                        onDragEnd = {
                            if (activePoints.isNotEmpty()) {
                                viewModel.addPaintStroke(
                                    windowId,
                                    PaintStroke(
                                        points = activePoints.toList(),
                                        color = currentColor,
                                        strokeWidth = currentWidth,
                                        isEraser = isEraser
                                    )
                                )
                                activePoints.clear()
                            }
                        }
                    )
                }
        ) {
            // Draw completed strokes
            strokes.forEach { stroke ->
                val points = stroke.points
                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = if (stroke.isEraser) Color.White else stroke.color,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = stroke.strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw current active stroke
            if (activePoints.size > 1) {
                for (i in 0 until activePoints.size - 1) {
                    drawLine(
                        color = if (isEraser) Color.White else currentColor,
                        start = activePoints[i],
                        end = activePoints[i + 1],
                        strokeWidth = currentWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// --- CALCULATOR APP ---
@Composable
fun CalculatorApp(windowId: Int, viewModel: SystemViewModel) {
    val displays by viewModel.calculatorDisplays.collectAsState()
    val display = displays[windowId] ?: "0"

    val buttons = listOf(
        listOf("C", "CE", "←", "±"),
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
            .padding(12.dp)
    ) {
        // Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFCCCCCC))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = display,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Button Matrix
        Column(modifier = Modifier.weight(4f)) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { btn ->
                        val isOperator = btn in listOf("/", "*", "-", "+", "=")
                        val isClear = btn in listOf("C", "CE", "←", "±")
                        
                        Button(
                            onClick = { viewModel.onCalculatorKey(windowId, btn) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 2.dp)
                                .testTag("calc_key_$btn"),
                            shape = RoundedCornerShape(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    btn == "=" -> Color(0xFF0078D7)
                                    isOperator -> Color(0xFFDCDCDC)
                                    isClear -> Color(0xFFE5E5E5)
                                    else -> Color(0xFFF5F5F5)
                                },
                                contentColor = if (btn == "=") Color.White else Color.Black
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = btn,
                                fontSize = 16.sp,
                                fontWeight = if (isOperator) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- MINESWEEPER APP ---
@Composable
fun MinesweeperApp(windowId: Int, viewModel: SystemViewModel) {
    val gridMap by viewModel.minesweeperGrids.collectAsState()
    val statusMap by viewModel.minesweeperStatus.collectAsState()
    val mineCountMap by viewModel.minesweeperMinesCount.collectAsState()

    val grid = gridMap[windowId] ?: emptyList()
    val status = statusMap[windowId] ?: MinesweeperStatus.PLAYING
    val minesCount = mineCountMap[windowId] ?: 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBDBDBD))
            .border(3.dp, Color.White)
            .padding(10.dp)
    ) {
        // Status indicator dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFBDBDBD))
                .border(2.dp, Color.Gray)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mines remaining count counter (retro red LCD text look!)
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format("%03d", minesCount),
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Smiley reset button
            Button(
                onClick = { viewModel.initMinesweeper(windowId) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCCCCC)),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.size(36.dp).testTag("minesweeper_reset")
            ) {
                Text(
                    text = when (status) {
                        MinesweeperStatus.PLAYING -> "🙂"
                        MinesweeperStatus.WON -> "😎"
                        MinesweeperStatus.LOST -> "😵"
                    },
                    fontSize = 20.sp
                )
            }

            // Status message
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = when (status) {
                        MinesweeperStatus.PLAYING -> "PLAY"
                        MinesweeperStatus.WON -> "WIN!"
                        MinesweeperStatus.LOST -> "BOOM"
                    },
                    color = when (status) {
                        MinesweeperStatus.PLAYING -> Color.Green
                        MinesweeperStatus.WON -> Color.Yellow
                        MinesweeperStatus.LOST -> Color.Red
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mines grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, Color.Gray)
                .background(Color(0xFF7B7B7B))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                modifier = Modifier.fillMaxSize()
            ) {
                items(grid) { cell ->
                    val isRevealed = cell.isRevealed
                    val isFlagged = cell.isFlagged

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(1.dp, Color.Gray)
                            .background(
                                if (isRevealed) {
                                    if (cell.isMine) Color.Red else Color(0xFFCCCCCC)
                                } else {
                                    Color(0xFFCECECE)
                                }
                            )
                            .pointerInput(cell.row, cell.col) {
                                detectTapGestures(
                                    onTap = {
                                        viewModel.revealMinesweeperCell(windowId, cell.row, cell.col)
                                    },
                                    onLongPress = {
                                        viewModel.flagMinesweeperCell(windowId, cell.row, cell.col)
                                    }
                                )
                            }
                            .testTag("mine_${cell.row}_${cell.col}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFlagged) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Flag",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (isRevealed) {
                            if (cell.isMine) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessLow,
                                    contentDescription = "Mine",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (cell.adjacentMinesCount > 0) {
                                val textCol = when (cell.adjacentMinesCount) {
                                    1 -> Color.Blue
                                    2 -> Color(0xFF2E7D32) // green
                                    3 -> Color.Red
                                    4 -> Color(0xFF6A1B9A) // purple
                                    5 -> Color(0xFF800000) // maroon
                                    else -> Color.DarkGray
                                }
                                Text(
                                    text = cell.adjacentMinesCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- INTERNET EXPLORER APP ---
@Composable
fun InternetExplorerApp(windowId: Int, viewModel: SystemViewModel) {
    val urls by viewModel.browserUrls.collectAsState()
    val currentUrl = urls[windowId] ?: "http://www.google.com"
    var urlInput by remember { mutableStateOf(currentUrl) }

    val searchQueries by viewModel.browserSearchQuery.collectAsState()
    val query = searchQueries[windowId] ?: ""

    Column(modifier = Modifier.fillMaxSize()) {
        // Address Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5ECF4))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setBrowserUrl(windowId, "http://www.google.com") }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))

            // Address bar card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFB0C4DE))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = "Web", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        modifier = Modifier.weight(1f).testTag("browser_address_bar")
                    )

                    IconButton(
                        onClick = { viewModel.setBrowserUrl(windowId, urlInput) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Go", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Divider(color = Color(0xFFB0C4DE), thickness = 1.dp)

        // Web Renderer Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2))
                .verticalScroll(rememberScrollState())
        ) {
            if (currentUrl.contains("google.com") || currentUrl.contains("search.com")) {
                // RENDER GOOGLE MOCK
                GoogleMockPage(
                    windowId = windowId,
                    viewModel = viewModel,
                    query = query,
                    onNavigate = { urlInput = it }
                )
            } else if (currentUrl.contains("wikipedia.org")) {
                // RENDER WIKIPEDIA MOCK
                WikipediaMockPage()
            } else {
                // RENDER HOMEPAGE / MSN PORTAL MOCK
                PortalMockPage(
                    onGoGoogle = {
                        urlInput = "http://www.google.com"
                        viewModel.setBrowserUrl(windowId, "http://www.google.com")
                    },
                    onGoWikipedia = {
                        urlInput = "https://en.wikipedia.org"
                        viewModel.setBrowserUrl(windowId, "https://en.wikipedia.org")
                    }
                )
            }
        }
    }
}

@Composable
fun GoogleMockPage(
    windowId: Int,
    viewModel: SystemViewModel,
    query: String,
    onNavigate: (String) -> Unit
) {
    var searchField by remember { mutableStateOf(query) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Text(
            text = "Google",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Blue, Color.Red, Color.Yellow, Color.Blue, Color.Green, Color.Red)
                )
            ),
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        // Search Input
        OutlinedTextField(
            value = searchField,
            onValueChange = { searchField = it },
            placeholder = { Text("Search the web or type a URL...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("google_search_field"),
            trailingIcon = {
                IconButton(onClick = { viewModel.searchBrowser(windowId, searchField) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(
                onClick = { viewModel.searchBrowser(windowId, searchField) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2), contentColor = Color.Black),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Google Search", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onNavigate("https://en.wikipedia.org")
                    viewModel.setBrowserUrl(windowId, "https://en.wikipedia.org")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2), contentColor = Color.Black),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("I'm Feeling Lucky", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Results
        if (query.isNotEmpty()) {
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Search results for '$query':",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            val results = when {
                query.contains("win", ignoreCase = true) -> listOf(
                    "Windows 8.1 - Microsoft Wiki" to "Windows 8.1 is an operating system that was produced by Microsoft. It was released to manufacturing on August 27, 2013.",
                    "Download Windows 8.1 Pro Simulator" to "A beautiful, fully-functional Compose-based Windows 8.1 simulator for Android. Contains File Explorer, Paint, Minesweeper, and Notepad!"
                )
                query.contains("compose", ignoreCase = true) -> listOf(
                    "Jetpack Compose - Android Developers" to "Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android.",
                    "Compose Custom Window Managers" to "Learn how to build dragging, snapping and multi-window managers entirely in Jetpack Compose using pointer input offsets."
                )
                else -> listOf(
                    "Simulated Result: $query" to "This is a working search simulator in our Windows 8.1 environment. Try searching 'Windows 8.1' or 'Compose' to see specialized responses!",
                    "Did you know?" to "Windows 8.1 re-introduced the Start button on the taskbar, which opens the modern horizontal-scrolling tile Start Screen!"
                )
            }

            results.forEach { (title, snippet) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF1A0DAB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            if (title.contains("Wiki")) {
                                onNavigate("https://en.wikipedia.org")
                                viewModel.setBrowserUrl(windowId, "https://en.wikipedia.org")
                            }
                        }
                    )
                    Text(text = "https://www.google.com/search?q=$query", color = Color(0xFF006621), fontSize = 10.sp)
                    Text(text = snippet, color = Color(0xFF545454), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WikipediaMockPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = "Wiki", tint = Color.Gray, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("WIKIPEDIA", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, letterSpacing = 2.sp)
                Text("The Free Encyclopedia", fontSize = 10.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        Text("Windows 8.1", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Text("From Wikipedia, the free encyclopedia", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Windows 8.1 is an upgrade for Windows 8, a version of the Windows NT operating system released by Microsoft. It was released to manufacturing on August 27, 2013, and reached general availability on October 17, 2013, almost a year after the retail release of its predecessor.\n\nWindows 8.1 was made available as a free download for retail copies of Windows 8 and Windows RT users via the Windows Store. Unlike service packs on previous versions of Windows, users who obtained Windows 8 from retail sources or pre-loaded on a computer had to download Windows 8.1 from the Windows Store, while volume licensing customers had to obtain installation media.",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Features Simulation Recreated:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("- Metro Start Screen with horizontal scrolling live tiles", fontSize = 11.sp)
                Text("- Taskbar with functioning hover-state previews", fontSize = 11.sp)
                Text("- Custom window snapping physics", fontSize = 11.sp)
                Text("- Fully responsive client-side Notepad, Draw/Paint, and Minesweeper", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PortalMockPage(onGoGoogle: () -> Unit, onGoWikipedia: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Hero banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0078D7)),
            shape = RoundedCornerShape(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Welcome to Windows 8.1", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Light)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Experience the beautiful flat tile UI simulator built in Jetpack Compose.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Popular Portals:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGoGoogle() },
                border = BorderStroke(1.dp, Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Google", tint = Color(0xFF4285F4), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Mock Search engine", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGoWikipedia() },
                border = BorderStroke(1.dp, Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Wikipedia", tint = Color.DarkGray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Wikipedia", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Encyclopedia info", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// --- SETTINGS PERSONALIZATION APP ---
@Composable
fun SettingsApp(viewModel: SystemViewModel) {
    val activeWallpaper by viewModel.wallpaper.collectAsState()
    val metroColor by viewModel.metroAccentColor.collectAsState()

    val wallpapers = listOf(
        "Default Metro Teal" to WallpaperType.WINDOWS_DEFAULT,
        "Dark Charcoal" to WallpaperType.CHARCOAL_DARK,
        "Royal Blue Gradient" to WallpaperType.OCEAN_GRADIENT,
        "Sunset Orange" to WallpaperType.SUNSET_METRO,
        "Emerald Green" to WallpaperType.EMERALD_GREEN
    )

    val metroColors = listOf(
        Color(0xFF0078D7), // Windows Blue
        Color(0xFF00B294), // Teal
        Color(0xFFD13438), // Rust/Red
        Color(0xFF8764B8), // Purple
        Color(0xFFF7630C), // Orange
        Color(0xFF107C41)  // Dark Green
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // Settings sidebar
        Column(
            modifier = Modifier
                .width(140.dp)
                .fillMaxHeight()
                .background(Color(0xFF1E1E1E))
                .padding(12.dp)
        ) {
            Text("PC SETTINGS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
            
            Text("PERSONALIZE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, modifier = Modifier.background(Color(0xFF333333)).fillMaxWidth().padding(6.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("SYSTEM SPEC", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, modifier = Modifier.padding(6.dp))
        }

        // Settings Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("PERSONALIZATION", fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            // Desktop wallpaper
            Text("DESKTOP WALLPAPER SELECTION", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            wallpapers.forEach { (name, type) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setWallpaper(type) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = activeWallpaper == type,
                        onClick = { viewModel.setWallpaper(type) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, fontSize = 13.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Start screen accent
            Text("START SCREEN ACCENT COLOR", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metroColors.forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(col)
                            .border(
                                width = if (metroColor == col) 3.dp else 1.dp,
                                color = if (metroColor == col) Color.Black else Color.LightGray
                            )
                            .clickable { viewModel.setMetroAccentColor(col) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // About System Spec
            Text("SYSTEM INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Text("OS Edition: Windows 8.1 Pro Simulator", fontSize = 12.sp, color = Color.Black)
            Text("Engine: Jetpack Compose on Android", fontSize = 12.sp, color = Color.Black)
            Text("Processor: Gemini Core Processor @ 3.5GHz", fontSize = 12.sp, color = Color.Black)
            Text("Memory: 8 GB (Simulated)", fontSize = 12.sp, color = Color.Black)
            Text("Created By: Google AI Studio Coder", fontSize = 12.sp, color = Color.Black)
        }
    }
}
