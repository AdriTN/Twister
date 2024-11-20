package com.grupo18.twister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grupo18.twister.core.screens.navigation.NavigationWrapper
import com.grupo18.twister.ui.theme.TwisterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwisterTheme {
                NavigationWrapper()
            }
        }
    }
}
