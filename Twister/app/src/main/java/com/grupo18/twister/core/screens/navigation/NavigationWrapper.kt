// Archivo: NavigationWrapper.kt
package com.grupo18.twister.core.screens.navigation

import QRScannerScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.screens.authentication.AuthScreen
import com.grupo18.twister.core.screens.edit.EditScreen
import com.grupo18.twister.core.screens.home.HomeScreen
import com.grupo18.twister.core.screens.search.SearchScreen
import com.grupo18.twister.core.screens.settings.SettingsScreen
import com.grupo18.twister.core.screens.twists.LiveTwist
import com.grupo18.twister.core.screens.twists.TempTwist
import com.grupo18.twister.core.screens.welcome.WelcomeScreen
import com.grupo18.twister.ui.theme.TwisterTheme

@Composable
fun NavigationWrapper(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    // Obtener instancia de MyApp usando conversión segura
    val myApp = LocalContext.current.applicationContext as? MyApp

    // Obtener el estado actual del usuario desde MyApp
    val currentUser: UserModel? = myApp?.getUser()

    // Determinar la ruta de inicio basada en el estado del usuario
    val startDestination = if (currentUser == null) Routes.WELCOME else Routes.HOME

    // Iniciar la navegación
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH)
                }
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = { data ->
                    myApp?.saveUser(data) // Guardar el usuario en MyApp
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.TWISTS) {
            TempTwist()
        }

        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }

        composable(Routes.EDIT) {
            EditScreen(navController = navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onLogout = {
                    myApp?.clearUser()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.QR_SCANNER) {
            var scannedPIN by remember { mutableStateOf("") }
            QRScannerScreen(
                paddingValues = PaddingValues(),
                onQRCodeScanned = { pin ->
                    scannedPIN = pin // Asigna el PIN escaneado
                    navController.navigate("liveTwist/$pin")
                }
            )
        }

        composable(Routes.LIVE_TWIST) { backStackEntry ->
            val pin = backStackEntry.arguments?.getString("pin") ?: ""
            if (pin.isNotEmpty()) {
                LiveTwist(pin)
            }
        }
    }
}
