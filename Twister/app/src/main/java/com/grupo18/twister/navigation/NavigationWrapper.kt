package com.grupo18.twister.navigation

import QRScannerScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.grupo18.twister.core.helpers.NotificationHelper
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.ui.screens.auth.AuthScreen
import com.grupo18.twister.ui.screens.welcome.WelcomeScreen
import com.grupo18.twister.ui.screens.edit.EditScreen
import com.grupo18.twister.ui.screens.edit.ManageQuestionsScreen
import com.grupo18.twister.ui.screens.home.HomeScreen
import com.grupo18.twister.ui.screens.home.ProfileScreen
import com.grupo18.twister.ui.screens.search.SearchScreen
import com.grupo18.twister.ui.screens.settings.SettingsScreen
import com.grupo18.twister.ui.screens.edit.AddQuestionScreen
import com.grupo18.twister.ui.screens.game.*
import com.grupo18.twister.viewmodels.factories.TwistViewModelFactory
import com.grupo18.twister.viewmodels.screens.TwistViewModel

@Composable
fun NavigationWrapper(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val myApp = context.applicationContext as? MyApp

    // Si no obtuvimos MyApp, mostramos un error
    if (myApp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: Aplicación no inicializada correctamente.")
        }
        return
    }

    // Obtenemos el usuario actual
    val currentUser by myApp.getUser().collectAsState()

    // Instanciamos TwistViewModel
    val twistViewModelFactory = TwistViewModelFactory(myApp)
    val twistViewModel: TwistViewModel = viewModel(factory = twistViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Routes.HOME else Routes.WELCOME
    ) {
        // Rutas estáticas sin parámetros
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH)
                },
                navController = navController
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = { user ->
                    println("New user logged in: ${user.token}")
                    myApp.saveUser(user)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                twistViewModel = twistViewModel
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }

        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }

        composable(Routes.EDIT) {
            EditScreen(navController = navController, twistViewModel = twistViewModel)
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
                user = currentUser
            )
        }

        composable(Routes.QR_SCANNER) {
            QRScannerScreen(
                paddingValues = PaddingValues(),
                onQRCodeScanned = { pin ->
                    // Navegamos con la función generadora
                    navController.navigate(Routes.liveTwistRoute(pin))
                }
            )
        }

        // Rutas con parámetros en la definición
        // Ejemplo: addQuestionRoute(twistId)
        composable(Routes.ADD_QUESTION) { backStackEntry ->
            val twistId = backStackEntry.arguments?.getString("twistId")
            twistId?.let {
                AddQuestionScreen(navController = navController, twistId = it)
            }
        }

        composable(Routes.MANAGE_QUESTIONS) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            Log.d("TwistDebug", "twistJson: $twistJson")

            val scope = rememberCoroutineScope()

            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }
            twist?.let {
                ManageQuestionsScreen(
                    navController = navController,
                    token = currentUser?.token ?: "",
                    twistViewModel = twistViewModel,
                    scope = scope,           // <--- pasamos el scope aquí
                    twist = it
                )
            }
        }

        composable(Routes.TWIST_DETAIL) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }

            TwistDetailScreen(
                navController = navController,
                twist = twist,
                twistViewModel = twistViewModel
            )
        }

        composable(Routes.PUBlIC_TWIST_DETAIL) { backStackEntry ->
            val twistId = backStackEntry.arguments?.getString("twistId") ?: ""
            PublicTwistDetailScreen(
                navController = navController,
                twistId = twistId,
                twistViewModel = twistViewModel
            )
        }

        composable(Routes.SOLO_TWIST) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }
            if (twist != null) {
                SoloTwist(
                    navController = navController,
                    twist = twist
                )
            }
        }

        composable(Routes.GAME_SCREEN) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }

            GameScreen(
                twist = twist,
                currentUser = currentUser,
                isAdmin = true,
                navController = navController
            )
        }

        composable(Routes.LIVE_TWIST_SCREEN) { backStackEntry ->
            val pin = backStackEntry.arguments?.getString("pin")
            GameScreen(
                pin = pin,
                currentUser = currentUser,
                twist = null,
                navController = navController
            )
        }

        composable(Routes.FINAL_SCREEN) { backStackEntry ->
            val rawTopPlayersString = backStackEntry.arguments?.getString("topPlayers")
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")

            val topScores = parseTopPlayersString(rawTopPlayersString)
            PodiumScreen(
                topPlayers = topScores,
                isAdmin = (isAdmin == "true"),
                onNavigateToHome = {
                    navController.navigate(Routes.HOME)
                }
            )
        }
    }
}

/**
 * Función auxiliar para parsear 'topPlayersString'.
 */
private fun parseTopPlayersString(rawTopPlayersString: String?): List<Pair<String, Int>> {
    if (rawTopPlayersString.isNullOrEmpty()) return emptyList()

    val cleaned = rawTopPlayersString
        .removePrefix("[(")
        .removeSuffix(")]")
        .replace("), (", ";")
    val parts = cleaned.split(";").map { it.trim() }

    return parts.mapNotNull { part ->
        val entry = part.split(",").map { it.trim() }
        if (entry.size == 2) {
            val name = entry[0]
            val scoreString = entry[1]
            scoreString.toIntOrNull()?.let { name to it }
        } else {
            null
        }
    }.take(3)
}
