package com.grupo18.twister.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo18.twister.R
import com.grupo18.twister.core.screens.navigation.*


@Composable
fun CustomBottomNavigationBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Bottom),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            containerColor = Color(0xFFE0E0E0)
        ) {
            // Icono izquierdo (Home)
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                },
                label = null,
                selected = false,
                onClick = {
                    // Navegar a Home y limpiar el stack de navegación
                    navController.navigate(Home) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )

            // Icono izquierdo (Search)
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                },
                label = null,
                selected = false,
                onClick = { navController.navigate(Search) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Icono derecho (Edit)
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                },
                label = null,
                selected = false,
                onClick = { navController.navigate(Edit) }
            )

            // Icono derecho (Settings)
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                },
                label = null,
                selected = false,
                onClick = { navController.navigate(Settings) }
            )
        }

        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(y = -20.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { navController.navigate(Twists) }) {
                Image(
                    painter = painterResource(id = R.drawable.ico),
                    contentDescription = "Logo Central",
                    modifier = Modifier
                        .size(50.dp)
                )
            }
        }

    }
}

