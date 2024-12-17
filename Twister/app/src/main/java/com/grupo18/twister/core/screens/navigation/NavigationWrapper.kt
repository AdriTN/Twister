package com.grupo18.twister.core.screens.navigation

import QRScannerScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.factories.TwistViewModelFactory
import com.grupo18.twister.core.helpers.NotificationHelper
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.authentication.AuthScreen
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.screens.welcome.WelcomeScreen
import com.grupo18.twister.core.screens.edit.EditScreen
import com.grupo18.twister.core.screens.edit.ManageQuestionsScreen
import com.grupo18.twister.core.screens.home.HomeScreen
import com.grupo18.twister.core.screens.home.ProfileScreen
import com.grupo18.twister.core.screens.navigation.Routes.GAME_SCREEN
import com.grupo18.twister.core.screens.navigation.Routes.LIVE_TWIST_SCREEN
import com.grupo18.twister.core.screens.search.SearchScreen
import com.grupo18.twister.core.screens.settings.SettingsScreen
import com.grupo18.twister.core.screens.twists.AddQuestionScreen
import com.grupo18.twister.core.screens.twists.TwistDetailScreen
import com.grupo18.twister.core.screens.twists.SoloTwist
import com.grupo18.twister.core.screens.twists.liveTwist.GameScreen
import com.grupo18.twister.core.viewmodel.TwistViewModel

@Composable
fun NavigationWrapper(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val myApp = context.applicationContext as? MyApp

    if (myApp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: Aplicación no inicializada correctamente.")
        }
        return
    }

    val currentUser by myApp.getUser().collectAsState()

    val twistViewModelFactory = TwistViewModelFactory(myApp)
    val twistViewModel: TwistViewModel = viewModel(factory = twistViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
//            GameScreen(TwistModel(title = "", description = ""), UserModel(token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTcsImlhdCI6MTczNDM3NjEyOSwiZXhwIjoxNzM0Mzc5NzI5fQ.afLq2EPPZxfZbKIQRul7ktDBtjQV-GM-oWYYIsU3Zgs", username = "Pepe", email = "p@p.com", password = ""))
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
                onQRCodeScanned = { pin -> navController.navigate("liveTwist/$pin") }
            )
        }

        composable(Routes.ADD_QUESTION) { backStackEntry ->
            val twistId = backStackEntry.arguments?.getString("twistId")
            twistId?.let {
                AddQuestionScreen(navController = navController, twistId = it)
            }
        }

        composable(Routes.MANAGE_QUESTIONS) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            Log.d("TwistDebug", "twistJson: $twistJson")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }
            Log.d("TwistDebug", "twistJson: $twistJson")
            twist?.let {
                ManageQuestionsScreen(
                    navController = navController,
                    twist = twist,
                    token = myApp.currentUser.value?.token ?: "",
                    twistViewModel = twistViewModel,
                    scope = androidx.compose.runtime.rememberCoroutineScope()
                )
            }
        }

        // Pantalla de detalle (intermedia)
        composable(Routes.TWIST_DETAIL) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }
            TwistDetailScreen(
                navController = navController,
                twist = twist,
                twistViewModel = twistViewModel
            )
        }

        // Pantalla de jugar en solitario
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

        composable(GAME_SCREEN) { backStackEntry ->
            val twistJson = backStackEntry.arguments?.getString("twist")
            val twist = twistJson?.let { Gson().fromJson(it, TwistModel::class.java) }

            GameScreen(twist, currentUser, isAdmin = true)
        }

        composable(LIVE_TWIST_SCREEN) { backStackEntry ->
            val pin = backStackEntry.arguments?.getString("pin")
            GameScreen(pin = pin, currentUser = currentUser, twist = null)
        }
    }
}
