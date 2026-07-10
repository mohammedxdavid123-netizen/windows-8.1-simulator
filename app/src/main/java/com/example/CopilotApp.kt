package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// OkHttpClient with optimized 60-second timeouts for Gemini API calls
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

// Secure client-side Gemini call helper using built-in JSONObject
private suspend fun callGeminiApi(history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "API_KEY_MISSING"
    }

    val model = "gemini-3.5-flash"
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    // Construct request history payload manually for 100% dependency-safe JSON
    val contentsJson = history.joinToString(separator = ",") { msg ->
        val role = if (msg.isUser) "user" else "model"
        """
        {
          "role": "$role",
          "parts": [
            {
              "text": ${escapeJsonString(msg.text)}
            }
          ]
        }
        """.trimIndent()
    }

    val payload = """
    {
      "contents": [
        $contentsJson
      ],
      "systemInstruction": {
        "parts": [
          {
            "text": "You are Copilot, a helpful, clever, and friendly AI Assistant powered by Google Gemini. You are running in a Windows 8.1 Simulator built for Mohammed David. You are knowledgeable about computing, Windows 8.1, programming (Kotlin, Compose, etc.), and general Q&A. Keep your replies friendly, structured, concise, and occasionally refer to the fact that you are running in this simulated desktop environment."
          }
        ]
      }
    }
    """.trimIndent()

    try {
        val requestBody = payload.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && responseBody != null) {
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val firstPart = parts.getJSONObject(0)
            firstPart.getString("text")
        } else {
            "Error: API returned code ${response.code}\n${responseBody ?: "No details available."}"
        }
    } catch (e: Exception) {
        "Error: ${e.localizedMessage ?: "Failed to connect to Gemini API."}"
    }
}

// JSON Escaper to prevent formatting issues
private fun escapeJsonString(text: String): String {
    val builder = StringBuilder()
    builder.append("\"")
    for (c in text) {
        when (c) {
            '\"' -> builder.append("\\\"")
            '\\' -> builder.append("\\\\")
            '\b' -> builder.append("\\b")
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            '\t' -> builder.append("\\t")
            else -> {
                if (c < ' ') {
                    val t = "000" + Integer.toHexString(c.code)
                    builder.append("\\u" + t.substring(t.length - 4))
                } else {
                    builder.append(c)
                }
            }
        }
    }
    builder.append("\"")
    return builder.toString()
}

@Composable
fun CopilotApp(
    windowId: Int,
    viewModel: SystemViewModel,
    modifier: Modifier = Modifier
) {
    val accentColor by viewModel.metroAccentColor.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Chat History State
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome",
                text = "Hello Mohammed David! I'm Copilot, your AI assistant powered by Gemini. How can I help you customize or explore your Windows 8.1 Simulator today?",
                isUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when a new message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Quick suggestions matching Mohammed David & Windows 8
    val suggestions = listOf(
        "Tell me a joke!",
        "Change my wallpaper",
        "How do I play Minesweeper?",
        "Write a poem about Kotlin"
    )

    fun sendMessage(text: String) {
        if (text.isBlank() || isSending) return
        val userMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), text = text, isUser = true)
        chatMessages.add(userMsg)
        inputText = ""
        isSending = true

        scope.launch {
            // Call API
            val response = callGeminiApi(chatMessages.toList())

            if (response == "API_KEY_MISSING") {
                chatMessages.add(
                    ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        text = "⚠️ Gemini API Key is missing! Please configure your GEMINI_API_KEY inside the Secrets panel of AI Studio to enable live AI responses.",
                        isUser = false
                    )
                )
            } else {
                chatMessages.add(
                    ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        text = response,
                        isUser = false
                    )
                )
            }
            isSending = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek dark slate aesthetic
    ) {
        // APP HEADER with Indigo/Teal accent gradients
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assistant,
                    contentDescription = "Copilot",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "COPILOT ASSISTANT",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Active - Powered by Gemini 3.5 Flash",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = {
                    chatMessages.clear()
                    chatMessages.add(
                        ChatMessage(
                            id = "welcome",
                            text = "Chat history cleared. How can I help you now, Mohammed David?",
                            isUser = false
                        )
                    )
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // CHAT TIMELINE (SCROLLABLE MESSAGE BUBBLES)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages) { message ->
                    val isUser = message.isUser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assistant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Message Bubble
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .testTag(if (isUser) "user_msg" else "ai_msg"),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) accentColor else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isUser) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Loading / Typing indicator
                if (isSending) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assistant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Pulsing dots typing box
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val dotAlpha1 by infiniteTransition.animateFloat(
                                        initialValue = 0.2f, targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600, delayMillis = 0),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    val dotAlpha2 by infiniteTransition.animateFloat(
                                        initialValue = 0.2f, targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600, delayMillis = 200),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    val dotAlpha3 by infiniteTransition.animateFloat(
                                        initialValue = 0.2f, targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600, delayMillis = 400),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )

                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = dotAlpha1)))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = dotAlpha2)))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = dotAlpha3)))
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUICK CONVERSATION SUGGESTION CHIPS
        if (chatMessages.size == 1 && !isSending) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { sendMessage(suggestion) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = suggestion,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // INPUT FIELD BAR
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            tonalElevation = 4.dp
        ) {
            Column {
                Divider(color = Color.White.copy(alpha = 0.08f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask Copilot anything...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .testTag("copilot_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = accentColor
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    sendMessage(inputText)
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                sendMessage(inputText)
                                focusManager.clearFocus()
                            }
                        },
                        enabled = inputText.isNotBlank() && !isSending,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isSending) accentColor else Color.White.copy(alpha = 0.05f))
                            .testTag("copilot_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isSending) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Security reminder banner for prototypes
                Text(
                    text = "Security Warning: Keys are secured inside AI Studio. Do not share raw APKs publicly.",
                    fontSize = 8.sp,
                    color = Color.Gray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
        }
    }
}
