package com.example

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class SystemViewModel : ViewModel() {

    // --- Windows State ---
    private val _openWindows = MutableStateFlow<List<WindowData>>(emptyList())
    val openWindows: StateFlow<List<WindowData>> = _openWindows.asStateFlow()

    private val _activeWindowId = MutableStateFlow<Int?>(null)
    val activeWindowId: StateFlow<Int?> = _activeWindowId.asStateFlow()

    private var nextWindowId = 1
    private var zIndexCounter = 1

    // Drag-snap preview guide state
    private val _hoveredSnapGuide = MutableStateFlow<SnapState>(SnapState.NONE)
    val hoveredSnapGuide: StateFlow<SnapState> = _hoveredSnapGuide.asStateFlow()

    private val _draggedWindowId = MutableStateFlow<Int?>(null)
    val draggedWindowId: StateFlow<Int?> = _draggedWindowId.asStateFlow()

    // --- File System State ---
    val rootDirectory = MockFile("C:", true, children = mutableListOf(
        MockFile("Users", true, children = mutableListOf(
            MockFile("User", true, children = mutableListOf(
                MockFile("Desktop", true, children = mutableListOf(
                    MockFile("Welcome.txt", false, "Welcome to the Windows 8.1 Simulator on Android!\n\nThis simulator is built using Kotlin and Jetpack Compose. Enjoy exploring the fully functional Start Screen, desktop apps, responsive File Explorer, window snapping, taskbar previews, and much more!"),
                    MockFile("Readme.txt", false, "How to use Window Snapping:\n- Drag any window by its title bar.\n- Move it to the very left edge to snap to the left 50%.\n- Move it to the very right edge to snap to the right 50%.\n- Move it to the top edge to maximize it to full screen!\n\nTaskbar Previews:\n- Hover or press/hold a running app icon in the taskbar to see a mini thumbnail preview of the window state!"),
                    MockFile("About Windows.txt", false, "Windows 8.1 Pro\nVersion: 6.3 (Build 9600)\nSystem Type: Simulated 64-bit Operating System on ARMv8\nMemory: 8 GB (Simulated)\nProcessor: Gemini Core Processor @ 3.5GHz")
                )),
                MockFile("Documents", true, children = mutableListOf(
                    MockFile("Meeting Notes.txt", false, "Project: Win8.1 Simulator in Compose\nStatus: Complete\nRating: 5-star\n\nNotes: The user requested classic desktop icons, start menu, window snapping, taskbar previews, and responsive file explorer."),
                    MockFile("Shopping List.txt", false, "- Buy some Metro tiles\n- Get classic wallpaper\n- Minesweeper bombs\n- Calculator buttons")
                )),
                MockFile("Downloads", true, children = mutableListOf()),
                MockFile("Pictures", true, children = mutableListOf(
                    MockFile("Sample.txt", false, "Drawing with Paint automatically saves to this folder or you can read notes from here!")
                ))
            ))
        )),
        MockFile("Windows", true, children = mutableListOf(
            MockFile("System32", true, children = mutableListOf(
                MockFile("calc.exe", false, "Calculator Executable"),
                MockFile("notepad.exe", false, "Notepad Executable")
            ))
        ))
    ))

    // Track active paths for each File Explorer window ID
    private val _explorerPaths = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val explorerPaths: StateFlow<Map<Int, List<String>>> = _explorerPaths.asStateFlow()

    // --- Active Folder File Selection (for Explorer UI) ---
    private val _selectedFileNames = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val selectedFileNames: StateFlow<Map<Int, String?>> = _selectedFileNames.asStateFlow()

    // --- Screen Mode ---
    private val _isStartScreenOpen = MutableStateFlow<Boolean>(true) // default opens to start screen!
    val isStartScreenOpen: StateFlow<Boolean> = _isStartScreenOpen.asStateFlow()

    // --- Personalization Settings ---
    private val _wallpaper = MutableStateFlow<WallpaperType>(WallpaperType.WINDOWS_DEFAULT)
    val wallpaper: StateFlow<WallpaperType> = _wallpaper.asStateFlow()

    private val _metroAccentColor = MutableStateFlow<Color>(Color(0xFF0078D7)) // Classic Windows Blue
    val metroAccentColor: StateFlow<Color> = _metroAccentColor.asStateFlow()

    // --- Calculator App State (by Window ID) ---
    private val _calculatorDisplays = MutableStateFlow<Map<Int, String>>(emptyMap())
    val calculatorDisplays: StateFlow<Map<Int, String>> = _calculatorDisplays.asStateFlow()
    // Internal calculators state
    private val calculatorInputs = mutableMapOf<Int, Double?>()
    private val calculatorOperators = mutableMapOf<Int, String?>()
    private val calculatorResetOnNextDigit = mutableMapOf<Int, Boolean>()

    // --- Notepad App State (by Window ID: open file path and text) ---
    private val _notepadFiles = MutableStateFlow<Map<Int, Pair<List<String>?, String>>>(emptyMap())
    val notepadFiles: StateFlow<Map<Int, Pair<List<String>?, String>>> = _notepadFiles.asStateFlow()

    // --- Paint App State (by Window ID) ---
    private val _paintStrokes = MutableStateFlow<Map<Int, List<PaintStroke>>>(emptyMap())
    val paintStrokes: StateFlow<Map<Int, List<PaintStroke>>> = _paintStrokes.asStateFlow()

    // --- Minesweeper App State (by Window ID) ---
    private val _minesweeperGrids = MutableStateFlow<Map<Int, List<MinesweeperCell>>>(emptyMap())
    val minesweeperGrids: StateFlow<Map<Int, List<MinesweeperCell>>> = _minesweeperGrids.asStateFlow()

    private val _minesweeperStatus = MutableStateFlow<Map<Int, MinesweeperStatus>>(emptyMap())
    val minesweeperStatus: StateFlow<Map<Int, MinesweeperStatus>> = _minesweeperStatus.asStateFlow()

    private val _minesweeperMinesCount = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val minesweeperMinesCount: StateFlow<Map<Int, Int>> = _minesweeperMinesCount.asStateFlow()

    // --- Internet Explorer State (by Window ID) ---
    private val _browserUrls = MutableStateFlow<Map<Int, String>>(emptyMap())
    val browserUrls: StateFlow<Map<Int, String>> = _browserUrls.asStateFlow()

    private val _browserSearchQuery = MutableStateFlow<Map<Int, String>>(emptyMap())
    val browserSearchQuery: StateFlow<Map<Int, String>> = _browserSearchQuery.asStateFlow()


    // --- INITIALIZATION ---
    init {
        // Start system clock or do setup if necessary
    }

    // --- PERSONALIZATION ACTIONS ---
    fun setWallpaper(type: WallpaperType) {
        _wallpaper.value = type
    }

    fun setMetroAccentColor(color: Color) {
        _metroAccentColor.value = color
    }

    fun toggleStartScreen() {
        _isStartScreenOpen.update { !it }
    }

    fun setStartScreenOpen(isOpen: Boolean) {
        _isStartScreenOpen.value = isOpen
    }

    // --- WINDOW ACTIONS ---
    fun openWindow(appType: AppType, title: String, filePath: List<String>? = null) {
        // Toggle off start screen when launching from desktop or specific icons
        _isStartScreenOpen.value = false

        val id = nextWindowId++
        val defaultWidth = 480f
        val defaultHeight = 360f

        // Cascade positions a bit so windows don't completely overlap
        val cascadeOffset = (id % 5) * 30f
        val startX = 60f + cascadeOffset
        val startY = 80f + cascadeOffset

        val newWindow = WindowData(
            id = id,
            title = title,
            appType = appType,
            x = startX,
            y = startY,
            width = if (appType == AppType.FILE_EXPLORER) 540f else defaultWidth,
            height = if (appType == AppType.FILE_EXPLORER) 380f else defaultHeight,
            zIndex = ++zIndexCounter
        )

        _openWindows.update { it + newWindow }
        _activeWindowId.value = id

        // Initialize specific app states for this window
        when (appType) {
            AppType.FILE_EXPLORER -> {
                val startPath = filePath ?: listOf("C:", "Users", "User", "Desktop")
                _explorerPaths.update { it + (id to startPath) }
            }
            AppType.NOTEPAD -> {
                val initialText = if (filePath != null) {
                    getFileContentAtPath(filePath) ?: ""
                } else {
                    ""
                }
                _notepadFiles.update { it + (id to Pair(filePath, initialText)) }
            }
            AppType.CALCULATOR -> {
                _calculatorDisplays.update { it + (id to "0") }
            }
            AppType.PAINT -> {
                _paintStrokes.update { it + (id to emptyList()) }
            }
            AppType.MINESWEEPER -> {
                initMinesweeper(id)
            }
            AppType.INTERNET_EXPLORER -> {
                _browserUrls.update { it + (id to "http://www.google.com") }
                _browserSearchQuery.update { it + (id to "") }
            }
            AppType.SETTINGS -> {}
            AppType.SNAKE -> {}
            AppType.COPILOT -> {}
        }
    }

    fun closeWindow(id: Int) {
        _openWindows.update { list -> list.filter { it.id != id } }
        // Clean up app states
        _explorerPaths.update { it - id }
        _selectedFileNames.update { it - id }
        _calculatorDisplays.update { it - id }
        _notepadFiles.update { it - id }
        _paintStrokes.update { it - id }
        _minesweeperGrids.update { it - id }
        _minesweeperStatus.update { it - id }
        _minesweeperMinesCount.update { it - id }
        _browserUrls.update { it - id }
        _browserSearchQuery.update { it - id }

        // Recalculate focus
        val remaining = _openWindows.value.filter { !it.isMinimized }.sortedBy { it.zIndex }
        _activeWindowId.value = remaining.lastOrNull()?.id
    }

    fun minimizeWindow(id: Int) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id) it.copy(isMinimized = true) else it
            }
        }
        // Focus next window
        val remaining = _openWindows.value.filter { !it.isMinimized && it.id != id }.sortedBy { it.zIndex }
        _activeWindowId.value = remaining.lastOrNull()?.id
    }

    fun restoreMinimizedWindow(id: Int) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id) it.copy(isMinimized = false) else it
            }
        }
        focusWindow(id)
    }

    fun focusWindow(id: Int) {
        _activeWindowId.value = id
        _openWindows.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(isMinimized = false, zIndex = ++zIndexCounter)
                } else {
                    it
                }
            }
        }
    }

    fun toggleMaximizeWindow(id: Int) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id) {
                    val isMax = !it.isMaximized
                    if (isMax) {
                        // Maximize: store current coordinates to restore later
                        it.copy(
                            isMaximized = true,
                            snapState = SnapState.NONE,
                            prevX = it.x,
                            prevY = it.y,
                            prevWidth = it.width,
                            prevHeight = it.height
                        )
                    } else {
                        // Restore original coordinates
                        it.copy(
                            isMaximized = false,
                            snapState = SnapState.NONE,
                            x = it.prevX,
                            y = it.prevY,
                            width = it.prevWidth,
                            height = it.prevHeight
                        )
                    }
                } else {
                    it
                }
            }
        }
        focusWindow(id)
    }

    fun updateWindowPosition(id: Int, deltaX: Float, deltaY: Float) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id) {
                    // Dragging unsnaps/unmaximizes!
                    if (it.isMaximized || it.snapState != SnapState.NONE) {
                        // Restore sizes but drag with mouse centered (approximate)
                        val prevW = it.prevWidth
                        val prevH = it.prevHeight
                        // Position window so title bar center aligns with current drag
                        val newX = it.x + deltaX
                        val newY = it.y + deltaY
                        it.copy(
                            isMaximized = false,
                            snapState = SnapState.NONE,
                            x = newX,
                            y = newY,
                            width = prevW,
                            height = prevH
                        )
                    } else {
                        it.copy(
                            x = it.x + deltaX,
                            y = it.y + deltaY
                        )
                    }
                } else {
                    it
                }
            }
        }
    }

    fun updateWindowSize(id: Int, deltaW: Float, deltaH: Float, screenWidth: Float, screenHeight: Float) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id && !it.isMaximized && it.snapState == SnapState.NONE) {
                    val newW = (it.width + deltaW).coerceAtLeast(200f).coerceAtMost(screenWidth - 20f)
                    val newH = (it.height + deltaH).coerceAtLeast(150f).coerceAtMost(screenHeight - 80f)
                    it.copy(width = newW, height = newH)
                } else {
                    it
                }
            }
        }
    }

    fun startWindowDrag(id: Int) {
        _draggedWindowId.value = id
    }

    fun handleWindowDrag(id: Int, screenTouchX: Float, screenTouchY: Float, screenWidth: Float, screenHeight: Float) {
        // Detect window snapping guides
        val edgeThreshold = 40f
        val topThreshold = 40f

        val snap = when {
            screenTouchX < edgeThreshold -> SnapState.LEFT
            screenTouchX > screenWidth - edgeThreshold -> SnapState.RIGHT
            screenTouchY < topThreshold -> SnapState.TOP
            else -> SnapState.NONE
        }
        _hoveredSnapGuide.value = snap
    }

    fun endWindowDrag(id: Int) {
        _draggedWindowId.value = null
        val snap = _hoveredSnapGuide.value
        _hoveredSnapGuide.value = SnapState.NONE

        if (snap != SnapState.NONE) {
            snapWindow(id, snap)
        }
    }

    fun snapWindow(id: Int, snap: SnapState) {
        _openWindows.update { list ->
            list.map {
                if (it.id == id) {
                    val prevX = if (it.snapState == SnapState.NONE && !it.isMaximized) it.x else it.prevX
                    val prevY = if (it.snapState == SnapState.NONE && !it.isMaximized) it.y else it.prevY
                    val prevW = if (it.snapState == SnapState.NONE && !it.isMaximized) it.width else it.prevWidth
                    val prevH = if (it.snapState == SnapState.NONE && !it.isMaximized) it.height else it.prevHeight

                    it.copy(
                        snapState = snap,
                        isMaximized = (snap == SnapState.TOP),
                        prevX = prevX,
                        prevY = prevY,
                        prevWidth = prevW,
                        prevHeight = prevH
                    )
                } else {
                    it
                }
            }
        }
    }

    // --- MOCK FILE SYSTEM UTILITIES ---
    fun getDirectoryAtPath(path: List<String>): MockFile? {
        var current: MockFile? = rootDirectory
        for (i in 1 until path.size) {
            current = current?.children?.find { it.name.equals(path[i], ignoreCase = true) && it.isDirectory }
        }
        return current
    }

    fun getFileContentAtPath(path: List<String>): String? {
        val dirPath = path.dropLast(1)
        val fileName = path.lastOrNull() ?: return null
        val dir = getDirectoryAtPath(dirPath)
        return dir?.children?.find { !it.isDirectory && it.name.equals(fileName, ignoreCase = true) }?.content
    }

    fun updateFileContent(path: List<String>, content: String) {
        val dirPath = path.dropLast(1)
        val fileName = path.lastOrNull() ?: return
        val dir = getDirectoryAtPath(dirPath) ?: return
        val file = dir.children.find { !it.isDirectory && it.name.equals(fileName, ignoreCase = true) }
        if (file != null) {
            file.content = content
        } else {
            // Create a new text file at path
            dir.children.add(MockFile(fileName, false, content))
        }
        // Trigger list update on any explorer browsing this directory
        _explorerPaths.update { Map -> Map.toMap() }
    }

    fun getExplorerPath(windowId: Int): List<String> {
        return _explorerPaths.value[windowId] ?: listOf("C:", "Users", "User", "Desktop")
    }

    fun navigateExplorer(windowId: Int, dirName: String) {
        val currentPath = getExplorerPath(windowId)
        val newPath = currentPath + dirName
        _explorerPaths.update { it + (windowId to newPath) }
        _selectedFileNames.update { it + (windowId to null) } // clear selection
    }

    fun navigateBackExplorer(windowId: Int) {
        val currentPath = getExplorerPath(windowId)
        if (currentPath.size > 1) {
            val newPath = currentPath.dropLast(1)
            _explorerPaths.update { it + (windowId to newPath) }
            _selectedFileNames.update { it + (windowId to null) }
        }
    }

    fun selectFileInExplorer(windowId: Int, fileName: String?) {
        _selectedFileNames.update { it + (windowId to fileName) }
    }

    fun createFolderInExplorer(windowId: Int, folderName: String = "New Folder") {
        val path = getExplorerPath(windowId)
        val dir = getDirectoryAtPath(path) ?: return
        
        // Ensure folder name is unique
        var name = folderName
        var counter = 2
        while (dir.children.any { it.name.equals(name, ignoreCase = true) && it.isDirectory }) {
            name = "$folderName ($counter)"
            counter++
        }

        dir.children.add(MockFile(name, true))
        _explorerPaths.update { Map -> Map.toMap() } // trigger recompose
    }

    fun createFileInExplorer(windowId: Int, fileName: String = "New Text Document.txt") {
        val path = getExplorerPath(windowId)
        val dir = getDirectoryAtPath(path) ?: return

        var name = fileName
        var counter = 2
        while (dir.children.any { it.name.equals(name, ignoreCase = true) && !it.isDirectory }) {
            val extension = if (fileName.contains(".")) fileName.substringAfterLast(".") else "txt"
            val baseName = if (fileName.contains(".")) fileName.substringBeforeLast(".") else fileName
            name = "$baseName ($counter).$extension"
            counter++
        }

        dir.children.add(MockFile(name, false, ""))
        _explorerPaths.update { Map -> Map.toMap() } // trigger recompose
    }

    fun deleteSelectedFileInExplorer(windowId: Int) {
        val path = getExplorerPath(windowId)
        val selected = _selectedFileNames.value[windowId] ?: return
        val dir = getDirectoryAtPath(path) ?: return

        dir.children.removeAll { it.name == selected }
        _selectedFileNames.update { it + (windowId to null) }
        _explorerPaths.update { Map -> Map.toMap() } // trigger recompose
    }

    // --- NOTEPAD ACTIONS ---
    fun updateNotepadText(windowId: Int, text: String) {
        _notepadFiles.update { map ->
            val pair = map[windowId]
            if (pair != null) {
                map + (windowId to Pair(pair.first, text))
            } else {
                map
            }
        }
    }

    fun saveNotepadFile(windowId: Int) {
        val notepadState = _notepadFiles.value[windowId] ?: return
        val path = notepadState.first
        val text = notepadState.second

        if (path != null) {
            updateFileContent(path, text)
        } else {
            // Save as New Text Document.txt on Desktop
            val desktopPath = listOf("C:", "Users", "User", "Desktop")
            val dir = getDirectoryAtPath(desktopPath) ?: return

            var name = "Notepad Note.txt"
            var counter = 2
            while (dir.children.any { it.name.equals(name, ignoreCase = true) }) {
                name = "Notepad Note ($counter).txt"
                counter++
            }

            dir.children.add(MockFile(name, false, text))
            _notepadFiles.update { it + (windowId to Pair(desktopPath + name, text)) }
            // update window title
            _openWindows.update { list ->
                list.map {
                    if (it.id == windowId) it.copy(title = "$name - Notepad") else it
                }
            }
            _explorerPaths.update { Map -> Map.toMap() } // trigger lists
        }
    }

    // --- CALCULATOR ACTIONS ---
    fun onCalculatorKey(windowId: Int, key: String) {
        val currentDisplay = _calculatorDisplays.value[windowId] ?: "0"
        val reset = calculatorResetOnNextDigit[windowId] ?: false

        when (key) {
            in "0".."9", "." -> {
                if (currentDisplay == "0" || reset) {
                    _calculatorDisplays.update { it + (windowId to if (key == ".") "0." else key) }
                    calculatorResetOnNextDigit[windowId] = false
                } else {
                    if (key == "." && currentDisplay.contains(".")) return
                    _calculatorDisplays.update { it + (windowId to currentDisplay + key) }
                }
            }
            "+", "-", "*", "/" -> {
                calculatorInputs[windowId] = currentDisplay.toDoubleOrNull()
                calculatorOperators[windowId] = key
                calculatorResetOnNextDigit[windowId] = true
            }
            "=" -> {
                val operator = calculatorOperators[windowId] ?: return
                val op1 = calculatorInputs[windowId] ?: return
                val op2 = currentDisplay.toDoubleOrNull() ?: return

                val result = when (operator) {
                    "+" -> op1 + op2
                    "-" -> op1 - op2
                    "*" -> op1 * op2
                    "/" -> if (op2 != 0.0) op1 / op2 else Double.NaN
                    else -> 0.0
                }

                val resultStr = if (result.isNaN()) {
                    "Error"
                } else if (result == result.toLong().toDouble()) {
                    result.toLong().toString()
                } else {
                    String.format("%.6f", result).trimEnd('0').trimEnd('.')
                }

                _calculatorDisplays.update { it + (windowId to resultStr) }
                calculatorInputs[windowId] = null
                calculatorOperators[windowId] = null
                calculatorResetOnNextDigit[windowId] = true
            }
            "C" -> {
                _calculatorDisplays.update { it + (windowId to "0") }
                calculatorInputs[windowId] = null
                calculatorOperators[windowId] = null
                calculatorResetOnNextDigit[windowId] = false
            }
            "CE" -> {
                _calculatorDisplays.update { it + (windowId to "0") }
                calculatorResetOnNextDigit[windowId] = false
            }
            "±" -> {
                if (currentDisplay != "0" && currentDisplay != "Error") {
                    val toggled = if (currentDisplay.startsWith("-")) currentDisplay.drop(1) else "-$currentDisplay"
                    _calculatorDisplays.update { it + (windowId to toggled) }
                }
            }
            "←" -> { // Backspace
                if (currentDisplay.length > 1 && currentDisplay != "Error") {
                    _calculatorDisplays.update { it + (windowId to currentDisplay.dropLast(1)) }
                } else {
                    _calculatorDisplays.update { it + (windowId to "0") }
                }
            }
        }
    }

    // --- PAINT ACTIONS ---
    fun addPaintStroke(windowId: Int, stroke: PaintStroke) {
        _paintStrokes.update { map ->
            val list = map[windowId] ?: emptyList()
            map + (windowId to (list + stroke))
        }
    }

    fun clearPaint(windowId: Int) {
        _paintStrokes.update { it + (windowId to emptyList()) }
    }

    fun savePaintImage(windowId: Int) {
        // Mock save image by adding a text log file in C:\Users\User\Pictures
        val picturesPath = listOf("C:", "Users", "User", "Pictures")
        val dir = getDirectoryAtPath(picturesPath) ?: return
        val count = _paintStrokes.value[windowId]?.size ?: 0

        var name = "My Drawing.txt"
        var counter = 2
        while (dir.children.any { it.name.equals(name, ignoreCase = true) }) {
            name = "My Drawing ($counter).txt"
            counter++
        }

        dir.children.add(MockFile(name, false, "Windows Paint Image File\nStrokes: $count\nSaved on Mobile device inside Simulator!"))
        _explorerPaths.update { Map -> Map.toMap() } // trigger
    }

    // --- MINESWEEPER ACTIONS ---
    fun initMinesweeper(windowId: Int) {
        val rows = 9
        val cols = 9
        val minesCount = 10

        val cells = ArrayList<MinesweeperCell>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                cells.add(MinesweeperCell(r, c))
            }
        }

        // Place mines randomly
        var minesPlaced = 0
        while (minesPlaced < minesCount) {
            val randIndex = Random.nextInt(cells.size)
            if (!cells[randIndex].isMine) {
                cells[randIndex] = cells[randIndex].copy(isMine = true)
                minesPlaced++
            }
        }

        // Calculate adjacent mines
        for (i in cells.indices) {
            val cell = cells[i]
            if (!cell.isMine) {
                var count = 0
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = cell.row + dr
                        val nc = cell.col + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            val neighbor = cells.find { it.row == nr && it.col == nc }
                            if (neighbor?.isMine == true) {
                                count++
                            }
                        }
                    }
                }
                cells[i] = cell.copy(adjacentMinesCount = count)
            }
        }

        _minesweeperGrids.update { it + (windowId to cells) }
        _minesweeperStatus.update { it + (windowId to MinesweeperStatus.PLAYING) }
        _minesweeperMinesCount.update { it + (windowId to minesCount) }
    }

    fun revealMinesweeperCell(windowId: Int, row: Int, col: Int) {
        val status = _minesweeperStatus.value[windowId] ?: MinesweeperStatus.PLAYING
        if (status != MinesweeperStatus.PLAYING) return

        val grid = _minesweeperGrids.value[windowId]?.toMutableList() ?: return
        val cellIndex = grid.indexOfFirst { it.row == row && it.col == col }
        if (cellIndex == -1) return

        val cell = grid[cellIndex]
        if (cell.isRevealed || cell.isFlagged) return

        // Reveal cell
        grid[cellIndex] = cell.copy(isRevealed = true)

        if (cell.isMine) {
            // Hit a mine! Game Over. Reveal all mines.
            for (i in grid.indices) {
                if (grid[i].isMine) {
                    grid[i] = grid[i].copy(isRevealed = true)
                }
            }
            _minesweeperGrids.update { it + (windowId to grid) }
            _minesweeperStatus.update { it + (windowId to MinesweeperStatus.LOST) }
            return
        }

        // Standard flood fill for zero-adjacent mine cells
        if (cell.adjacentMinesCount == 0) {
            val queue = mutableListOf(Pair(row, col))
            val visited = mutableSetOf(Pair(row, col))

            while (queue.isNotEmpty()) {
                val (currRow, currCol) = queue.removeAt(0)
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = currRow + dr
                        val nc = currCol + dc
                        if (nr in 0 until 9 && nc in 0 until 9) {
                            val key = Pair(nr, nc)
                            if (!visited.contains(key)) {
                                visited.add(key)
                                val idx = grid.indexOfFirst { it.row == nr && it.col == nc }
                                if (idx != -1) {
                                    val neighbor = grid[idx]
                                    if (!neighbor.isRevealed && !neighbor.isFlagged && !neighbor.isMine) {
                                        grid[idx] = neighbor.copy(isRevealed = true)
                                        if (neighbor.adjacentMinesCount == 0) {
                                            queue.add(Pair(nr, nc))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check Win Condition: all non-mine cells are revealed
        val win = grid.none { !it.isMine && !it.isRevealed }
        if (win) {
            // Flag all remaining mines
            for (i in grid.indices) {
                if (grid[i].isMine) {
                    grid[i] = grid[i].copy(isFlagged = true)
                }
            }
            _minesweeperStatus.update { it + (windowId to MinesweeperStatus.WON) }
            _minesweeperMinesCount.update { it + (windowId to 0) }
        }

        _minesweeperGrids.update { it + (windowId to grid) }
    }

    fun flagMinesweeperCell(windowId: Int, row: Int, col: Int) {
        val status = _minesweeperStatus.value[windowId] ?: MinesweeperStatus.PLAYING
        if (status != MinesweeperStatus.PLAYING) return

        val grid = _minesweeperGrids.value[windowId]?.toMutableList() ?: return
        val cellIndex = grid.indexOfFirst { it.row == row && it.col == col }
        if (cellIndex == -1) return

        val cell = grid[cellIndex]
        if (cell.isRevealed) return

        val newFlagState = !cell.isFlagged
        grid[cellIndex] = cell.copy(isFlagged = newFlagState)

        // Adjust remaining mines counter
        val flagsChange = if (newFlagState) -1 else 1
        _minesweeperMinesCount.update { map ->
            val count = map[windowId] ?: 10
            map + (windowId to (count + flagsChange).coerceAtLeast(0))
        }

        _minesweeperGrids.update { it + (windowId to grid) }
    }

    // --- BROWSER ACTIONS ---
    fun setBrowserUrl(windowId: Int, url: String) {
        _browserUrls.update { it + (windowId to url) }
    }

    fun setBrowserSearchQuery(windowId: Int, query: String) {
        _browserSearchQuery.update { it + (windowId to query) }
    }

    fun searchBrowser(windowId: Int, query: String) {
        val cleanQuery = query.trim().replace(" ", "+")
        _browserUrls.update { it + (windowId to "http://search.com/?q=$cleanQuery") }
        _browserSearchQuery.update { it + (windowId to query) }
    }
}
