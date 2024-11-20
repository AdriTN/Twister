package com.grupo18.twister.core.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo18.twister.core.screens.authentication.AuthScreen
import com.grupo18.twister.core.screens.edit.EditScreen
import com.grupo18.twister.core.screens.home.HomeScreen
import com.grupo18.twister.core.screens.search.SearchScreen
import com.grupo18.twister.core.screens.settings.SettingsScreen
import com.grupo18.twister.core.screens.twists.Question
import com.grupo18.twister.core.screens.twists.SingleQuestion
import com.grupo18.twister.core.screens.twists.SoloTwist
import com.grupo18.twister.core.screens.welcome.WelcomeScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    var loginData by rememberSaveable { mutableStateOf<LoginData?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (loginData == null) Welcome else Home
    ) {
        composable<Welcome> {
            WelcomeScreen(
                onNavigateToAuth = {
                    navController.navigate(Auth)
                }
            )
        }

        composable<Auth> {
            AuthScreen(
                onAuthSuccess = { data ->
                    loginData = data
                    navController.navigate(Home) {
                        popUpTo(Welcome) { inclusive = true }
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                userName = "Adrian",
                navController = navController
            )
        }

        composable<Twists> {
            SoloTwist(quizData = physicsQuiz)
        }

        composable<Search> {
            SearchScreen(navController = navController)
        }

        composable<Edit> {
            EditScreen(navController = navController)
        }

        composable<Settings> {
            SettingsScreen(navController = navController)
        }
    }
}

val physicsQuiz = Question(
    id = 1,
    description = "Quiz de Física General",
    image = "https://example.com/physics_quiz_banner.jpg", // Banner opcional para el quiz
    questions = listOf(
        SingleQuestion(
            description = "¿Cuál es la velocidad de la luz en el vacío?",
            image = null, // No hay imagen para esta pregunta
            options = listOf(
                "300,000 km/s",
                "150,000 km/s",
                "100,000 km/s",
                "500,000 km/s"
            ),
            solution = 0 // Respuesta correcta es la primera opción
        ),
        SingleQuestion(
            description = "¿Cuál es la unidad de medida de la fuerza?",
            image = null,
            options = listOf(
                "Joule",
                "Pascal",
                "Newton",
                "Watt"
            ),
            solution = 2 // Respuesta correcta es "Newton"
        ),
        SingleQuestion(
            description = "Identifica el tipo de onda en la siguiente imagen.",
            image = "https://example.com/transverse_wave.jpg", // Imagen de una onda transversal
            options = listOf(
                "Onda longitudinal",
                "Onda transversal",
                "Onda estacionaria",
                "Onda mecánica"
            ),
            solution = 1 // Respuesta correcta es "Onda transversal"
        ),
        SingleQuestion(
            description = "¿Qué ley establece que la energía no se crea ni se destruye, solo se transforma?",
            image = null,
            options = listOf(
                "Primera Ley de la Termodinámica",
                "Ley de la Gravitación Universal",
                "Ley de Coulomb",
                "Ley de Hooke"
            ),
            solution = 0 // Respuesta correcta es "Primera Ley de la Termodinámica"
        ),
        SingleQuestion(
            description = "¿Qué sucede cuando un objeto alcanza la velocidad de escape de la Tierra?",
            image = "https://example.com/rocket_escape_velocity.jpg", // Imagen de un cohete
            options = listOf(
                "El objeto orbita alrededor de la Tierra",
                "El objeto cae de vuelta a la Tierra",
                "El objeto se aleja indefinidamente de la Tierra",
                "El objeto queda suspendido en el espacio"
            ),
            solution = 2 // Respuesta correcta es "El objeto se aleja indefinidamente de la Tierra"
        )
    )
)