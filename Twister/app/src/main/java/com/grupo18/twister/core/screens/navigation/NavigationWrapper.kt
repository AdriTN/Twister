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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.grupo18.twister.core.factories.TwistViewModelFactory
import com.grupo18.twister.core.helpers.NotificationHelper
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.screens.authentication.AuthScreen
import com.grupo18.twister.core.screens.welcome.WelcomeScreen
import com.grupo18.twister.core.screens.edit.EditScreen
import com.grupo18.twister.core.screens.edit.ManageQuestionsScreen
import com.grupo18.twister.core.screens.home.HomeScreen
import com.grupo18.twister.core.screens.home.ProfileScreen
import com.grupo18.twister.core.screens.search.SearchScreen
import com.grupo18.twister.core.screens.settings.SettingsScreen
import com.grupo18.twister.core.screens.twists.AddQuestionScreen
import com.grupo18.twister.core.screens.twists.LiveTwist
import com.grupo18.twister.core.screens.twists.TempTwist
import com.grupo18.twister.core.viewmodel.QuestionViewModel
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
    val questionViewModel: QuestionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
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
                    myApp.saveUser(user)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }

        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }

        composable(Routes.EDIT) {
            EditScreen(navController = navController, twistViewModel = twistViewModel, questionViewModel = questionViewModel)
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
                    navController.navigate("liveTwist/$pin")
                }
            )
        }

        composable(Routes.TEMP_TWIST) {
            TempTwist { pin ->
                navController.navigate("liveTwist/$pin")
            }
        }

        composable(Routes.LIVE_TWIST) { backStackEntry ->
            val pin = backStackEntry.arguments?.getString("pin") ?: ""
            if (pin.isNotEmpty()) {
                LiveTwist(pin)
            }
        }

        composable("addQuestion/{twistId}") { backStackEntry ->
            val twistId = backStackEntry.arguments?.getString("twistId")
            twistId?.let {
                AddQuestionScreen(navController = navController, twistId = it)
            }
        }

        composable("manageQuestions/{twistId}") { backStackEntry ->
            val twistId = backStackEntry.arguments?.getString("twistId")
            twistId?.let {
                ManageQuestionsScreen(navController = navController, twistId = it, questionViewModel = questionViewModel)
            }
        }
    }
}
