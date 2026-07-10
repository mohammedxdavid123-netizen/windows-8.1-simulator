package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.random.Random

enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }
enum class GameStatus { IDLE, RUNNING, PAUSED, GAME_OVER }

data class Point(val x: Int, val y: Int)

@Composable
fun SnakeApp(
    windowId: Int,
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    val accentColor by viewModel.metroAccentColor.collectAsState()

    // Grid Dimensions
    val gridWidth = 20
    val gridHeight = 20

    // Snake State
    var snake by remember { mutableStateOf(listOf(Point(5, 10), Point(4, 10), Point(3, 10))) }
    var direction by remember { mutableStateOf(SnakeDirection.RIGHT) }
    var apple by remember { mutableStateOf(Point(12, 10)) }
    var gameStatus by remember { mutableStateOf(GameStatus.IDLE) }
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    
    // User requested: "first the speed is 6 and snake is blue and apple is red"
    var speed by remember { mutableStateOf(6) }

    val focusManager = LocalFocusManager.current

    // Helper: spawn apple at a valid random spot not on the snake
    fun spawnApple() {
        var nextApple: Point
        do {
            nextApple = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
        } while (snake.contains(nextApple))
        apple = nextApple
    }

    // Reset Game
    fun resetGame() {
        snake = listOf(Point(5, 10), Point(4, 10), Point(3, 10))
        direction = SnakeDirection.RIGHT
        score = 0
        spawnApple()
        gameStatus = GameStatus.RUNNING
    }

    // Input/Keyboard/Button trigger
    fun changeDirection(newDir: SnakeDirection) {
        if (gameStatus != GameStatus.RUNNING) return
        // Prevent 180-degree immediate reverse
        when (newDir) {
            SnakeDirection.UP -> if (direction != SnakeDirection.DOWN) direction = SnakeDirection.UP
            SnakeDirection.DOWN -> if (direction != SnakeDirection.UP) direction = SnakeDirection.DOWN
            SnakeDirection.LEFT -> if (direction != SnakeDirection.RIGHT) direction = SnakeDirection.LEFT
            SnakeDirection.RIGHT -> if (direction != SnakeDirection.LEFT) direction = SnakeDirection.RIGHT
        }
    }

    // Game loop running as a LaunchedEffect
    LaunchedEffect(gameStatus, speed) {
        if (gameStatus == GameStatus.RUNNING) {
            while (true) {
                // delay duration based on speed
                val delayMs = (1000L / speed).coerceIn(50L, 500L)
                delay(delayMs)

                val head = snake.first()
                val nextHead = when (direction) {
                    SnakeDirection.UP -> Point(head.x, head.y - 1)
                    SnakeDirection.DOWN -> Point(head.x, head.y + 1)
                    SnakeDirection.LEFT -> Point(head.x - 1, head.y)
                    SnakeDirection.RIGHT -> Point(head.x + 1, head.y)
                }

                // Check self/wall collision
                val collision = nextHead.x < 0 || nextHead.x >= gridWidth ||
                        nextHead.y < 0 || nextHead.y >= gridHeight ||
                        snake.contains(nextHead)

                if (collision) {
                    gameStatus = GameStatus.GAME_OVER
                    if (score > highScore) {
                        highScore = score
                    }
                    break
                }

                // Check apple collision
                if (nextHead == apple) {
                    snake = listOf(nextHead) + snake
                    score += 10
                    spawnApple()
                } else {
                    snake = listOf(nextHead) + snake.dropLast(1)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek dark slate theme
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionUp -> { changeDirection(SnakeDirection.UP); true }
                        Key.DirectionDown -> { changeDirection(SnakeDirection.DOWN); true }
                        Key.DirectionLeft -> { changeDirection(SnakeDirection.LEFT); true }
                        Key.DirectionRight -> { changeDirection(SnakeDirection.RIGHT); true }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP PANEL: SCORE & SPEED ACCENT SLATED HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCORE: $score",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "BEST: $highScore",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Speed slider/indicator
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SPEED: $speed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("-", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = speed.toFloat(),
                                onValueChange = { speed = it.toInt().coerceIn(1, 15) },
                                valueRange = 1f..15f,
                                modifier = Modifier.width(80.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            )
                            Text("+", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CENTER BOARD: SIZED TO FIT NICELY ON SCREEN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617)) // Pitch black canvas board
                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val cellW = w / gridWidth
                    val cellH = h / gridHeight

                    // Subtle background grid
                    for (x in 1 until gridWidth) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(x * cellW, 0f),
                            end = Offset(x * cellW, h),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 1 until gridHeight) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(0f, y * cellH),
                            end = Offset(w, y * cellH),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Apple: Red (as requested: "apple is red")
                    val appleRadius = cellW * 0.45f
                    drawCircle(
                        color = Color(0xFFD93025), // Apple is red
                        radius = appleRadius,
                        center = Offset(
                            apple.x * cellW + cellW / 2,
                            apple.y * cellH + cellH / 2
                        )
                    )
                    // Draw a cute leaf
                    drawCircle(
                        color = Color(0xFF4ADE80),
                        radius = appleRadius * 0.3f,
                        center = Offset(
                            apple.x * cellW + cellW / 2 + appleRadius * 0.4f,
                            apple.y * cellH + cellH / 2 - appleRadius * 0.6f
                        )
                    )

                    // Draw Snake: Blue (as requested: "snake is blue")
                    snake.forEachIndexed { index, segment ->
                        val isHead = index == 0
                        val color = if (isHead) Color(0xFF1E3A8A) else Color(0xFF2563EB) // Multi-tone blue
                        val cornerRadius = cellW * 0.2f

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(segment.x * cellW + 1f, segment.y * cellH + 1f),
                            size = Size(cellW - 2f, cellH - 2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )

                        // Draw eyes
                        if (isHead) {
                            val eyeRadius = cellW * 0.1f
                            val eyeOffset = cellW * 0.25f
                            when (direction) {
                                SnakeDirection.UP -> {
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + eyeOffset, segment.y * cellH + eyeOffset))
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + cellW - eyeOffset, segment.y * cellH + eyeOffset))
                                }
                                SnakeDirection.DOWN -> {
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + eyeOffset, segment.y * cellH + cellH - eyeOffset))
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + cellW - eyeOffset, segment.y * cellH + cellH - eyeOffset))
                                }
                                SnakeDirection.LEFT -> {
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + eyeOffset, segment.y * cellH + eyeOffset))
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + eyeOffset, segment.y * cellH + cellH - eyeOffset))
                                }
                                SnakeDirection.RIGHT -> {
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + cellW - eyeOffset, segment.y * cellH + eyeOffset))
                                    drawCircle(Color.White, eyeRadius, Offset(segment.x * cellW + cellW - eyeOffset, segment.y * cellH + cellH - eyeOffset))
                                }
                            }
                        }
                    }
                }

                // OVERLAY CARDS BASED ON STATE
                if (gameStatus == GameStatus.IDLE) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RETRO SNAKE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Guide the blue snake to eat red apples.\nUse arrow buttons or keyboard to steer.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START GAME", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                } else if (gameStatus == GameStatus.GAME_OVER) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FINAL SCORE: $score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLAY AGAIN", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                } else if (gameStatus == GameStatus.PAUSED) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { gameStatus = GameStatus.RUNNING },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESUME", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTTOM CONTROLS PANEL: D-PAD AND ACTIONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game management controls (Pause / Reset)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (gameStatus == GameStatus.RUNNING) {
                        IconButton(
                            onClick = { gameStatus = GameStatus.PAUSED },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        }
                    } else if (gameStatus == GameStatus.PAUSED) {
                        IconButton(
                            onClick = { gameStatus = GameStatus.RUNNING },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { resetGame() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                }

                // TOUCH ARROW CONTROLS (as requested: "add controls like arrows to control the snake")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Up Button
                    IconButton(
                        onClick = { changeDirection(SnakeDirection.UP) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = if (direction == SnakeDirection.UP) 0.25f else 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .testTag("snake_arrow_up")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Button
                        IconButton(
                            onClick = { changeDirection(SnakeDirection.LEFT) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = if (direction == SnakeDirection.LEFT) 0.25f else 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .testTag("snake_arrow_left")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        // Dummy spacer center
                        Box(modifier = Modifier.size(46.dp))

                        // Right Button
                        IconButton(
                            onClick = { changeDirection(SnakeDirection.RIGHT) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = if (direction == SnakeDirection.RIGHT) 0.25f else 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .testTag("snake_arrow_right")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    // Down Button
                    IconButton(
                        onClick = { changeDirection(SnakeDirection.DOWN) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = if (direction == SnakeDirection.DOWN) 0.25f else 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .testTag("snake_arrow_down")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
