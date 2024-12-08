package com.grupo18.twister.core.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.authentication.MyApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val myApp = context.applicationContext as MyApp

    // Recoger el usuario actual como estado
    val currentUser by myApp.getUser().collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
            ,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Información Personal
            currentUser?.let { ProfileHeader(it) }

            Spacer(modifier = Modifier.height(24.dp))

            // Logros y Estadísticas
            SectionTitle("Logros")
            currentUser?.let { AchievementsSection(it) }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Configuración y Preferencias
            SectionTitle("Configuración")
            SettingsSection(navController)
        }
    }
}

@Composable
fun ProfileHeader(user: UserModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = user?.username ?: "Usuario Desconocido", fontSize = 24.sp)
            Text(text = "Bio o Estado aquí", fontSize = 16.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementsSection(user: UserModel) {
    // Implementa una lista de logros
    LazyRow {
        items(user?.achievements ?: emptyList()) { achievement ->
            AchievementBadge(achievement)
        }
    }
}

@Composable
fun AchievementBadge(achievement: String) {
    // Diseño de un badge individual
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.Blue, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = achievement, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun SettingsSection(navController: NavController) {
    Column {
        SettingItem(title = "Cambiar Contraseña") {
            navController.navigate("changePassword")
        }
        SettingItem(title = "Preferencias de Notificación") {
            navController.navigate("notificationSettings")
        }
        SettingItem(title = "Cerrar Sesión") {
            // Implementa la lógica de cierre de sesión
        }
    }
}

@Composable
fun SettingItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Ir a $title")
    }
}
