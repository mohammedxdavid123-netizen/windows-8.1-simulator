package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Central state viewmodel
    private val viewModel = SystemViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val isStartScreenOpen by viewModel.isStartScreenOpen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Taskbar(viewModel = viewModel)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()) // Reserve space for the taskbar
                    ) {
                        if (isStartScreenOpen) {
                            StartScreen(viewModel = viewModel)
                        } else {
                            DesktopScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
