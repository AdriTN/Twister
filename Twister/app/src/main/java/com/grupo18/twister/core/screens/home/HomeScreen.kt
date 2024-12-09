package com.grupo18.twister.core.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.models.UserModel

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as MyApp

    val currentUser by app.getUser().collectAsState()

    Scaffold(
        bottomBar = { CustomBottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            HeaderWithProfileOrLogin(currentUser, navController)

            Spacer(modifier = Modifier.height(24.dp))

            if (currentUser?.isAnonymous == false) {
                // Solo mostramos "Your Twists" si el usuario NO es anónimo
                SectionTitle("Your Twists")
                Spacer(modifier = Modifier.height(16.dp))
                GridSection(items = List(12) { "Lorem Ipsum" })
                Spacer(modifier = Modifier.height(32.dp))
            }

            SectionTitle("Most Played")
            Spacer(modifier = Modifier.height(16.dp))
            GridSection(items = List(8) { "Lorem Ipsum" })
        }
    }
}

@Composable
fun HeaderWithProfileOrLogin(currentUser: UserModel?, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Columna de texto con peso para ajustar el layout
        Column(
            modifier = Modifier
                .weight(1f) // Ocupa el espacio disponible en la fila
        ) {
            if (currentUser?.isAnonymous == true) {
                // Usuario anónimo: mensaje especial con un tamaño de texto algo menor para no sobrepasar espacio
                Text(
                    text = "You are browsing as a guest.\nPlease log in to unlock all features.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Usuario normal
                Text(
                    text = "Hi, ${currentUser?.username ?: "Unknown User"}",
                    fontSize = 24.sp
                )
                Text(
                    text = "Let's make this day productive",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (currentUser?.isAnonymous == true) {
            // Usuario anónimo: mostrar botón "Login"
            Button(
                onClick = { navController.navigate("auth") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Login", color = Color.White)
            }
        } else {
            // Usuario normal: mostrar icono de perfil
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, CircleShape)
                    .clickable {
                        navController.navigate("profile")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp
    )
}

@Composable
fun GridSection(items: List<String>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val groupedItems = items.chunked(4)
        items(groupedItems) { group ->
            Box(
                modifier = Modifier
                    .fillParentMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primera fila (2 elementos)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        group.take(2).forEach { item ->
                            GridItem(item)
                        }
                    }
                    // Segunda fila (2 elementos)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        group.drop(2).forEach { item ->
                            GridItem(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(text: String) {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
