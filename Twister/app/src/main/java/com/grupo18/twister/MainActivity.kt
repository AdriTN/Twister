package com.grupo18.twister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.grupo18.twister.core.screens.navigation.NavigationWrapper
import com.grupo18.twister.ui.theme.TwisterTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val permissions = rememberMultiplePermissionsState(
                permissions = listOf(
                    android.Manifest.permission.CAMERA
                )
            )
            LaunchedEffect(key1 = Unit) {
                permissions.launchMultiplePermissionRequest()
            }
            // Definir el estado del tema oscuro en MainActivity
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            TwisterTheme(darkTheme = isDarkTheme) {
                // Pasar el estado y la función de callback a NavigationWrapper
                NavigationWrapper(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it }
                )
            }
        }


    }
}
