package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.security.configureAppCheck
import com.example.ui.navigation.ClickwiseApp
import com.example.ui.theme.ClickwiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before any UI, ViewModels, or AI operations.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            ClickwiseTheme {
                ClickwiseApp()
            }
        }
    }
}
