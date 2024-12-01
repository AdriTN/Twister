package com.grupo18.twister.core.screens.navigation

import QRScannerScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo18.twister.core.helpers.NotificationHelper
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

@Composable
fun NavigationWrapper(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val myApp = context.applicationContext as? MyApp

    // Verificar si myApp es null para evitar ClassCastException
    if (myApp == null) {
        // Manejar el caso donde myApp es null, por ejemplo, mostrar un mensaje de error
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: Aplicación no inicializada correctamente.")
        }
        return
    }

    // Recoger el usuario actual como estado
    val currentUser by myApp.getUser().collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH)
                },
                navController = navController // Pasar el NavController
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = { user ->
                    myApp.saveUser(user) // Guarda el usuario en MyApp
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
                    myApp.clearUser()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onSendTestNotification = {
                    NotificationHelper.sendNotification(
                        context = context,
                        title = "Notificación de Prueba",
                        message = "Esta es una notificación de prueba."
                    )
                },
                user = currentUser // Pasar el usuario actual a SettingsScreen
            )
        }

        composable(Routes.QR_SCANNER) {
            QRScannerScreen(
                paddingValues = PaddingValues(),
                onQRCodeScanned = { pin ->
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
