// Archivo: CustomBottomNavigationBar.kt
package com.grupo18.twister.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.grupo18.twister.R
import com.grupo18.twister.core.screens.navigation.Routes

/**
 * Sealed class que representa los ítems de la barra de navegación inferior.
 */
sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home, "Home")
    object Search : BottomNavItem(Routes.SEARCH, Icons.Default.Search, "Search")
    object Edit : BottomNavItem(Routes.EDIT, Icons.Default.Edit, "Edit")
    object Settings : BottomNavItem(Routes.SETTINGS, Icons.Default.Settings, "Settings")
    // Agrega más ítems según sea necesario
}

/**
 * Composable que representa la barra de navegación inferior personalizada.
 *
 * @param navController Controlador de navegación para manejar las acciones de navegación.
 */
@Composable
fun CustomBottomNavigationBar(navController: NavController) {
    // Lista de ítems de navegación
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Edit,
        BottomNavItem.Settings
    )

    // Obtener la ruta actual para manejar el estado de selección
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Bottom),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barra de navegación principal
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp), // Aumentar la altura de la barra de navegación
            containerColor = Color(0xFFE0E0E0) // Color de fondo de la barra de navegación
        ) {
            // Iterar sobre cada ítem y crear un NavigationBarItem correspondiente
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(32.dp) // Aumentar el tamaño del ícono
                                .padding(top = 4.dp) // Ajustar padding para evitar cortar
                        )
                    },
                    label = { Text(item.label) }, // Opcional: Agregar etiquetas para accesibilidad
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Configuraciones de navegación para evitar múltiples copias y restaurar estado
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }

        // Botón central (Twists)
        Box(
            modifier = Modifier
                .size(60.dp) // Reducir el tamaño del botón central
                .offset(y = -40.dp), // Posiciona el botón central sobre la barra de navegación
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.TEMP_TWIST) },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp) // Reducir elevación
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ico), // Reemplaza con tu ícono
                    contentDescription = "Twists",
                    modifier = Modifier.size(40.dp) // Reducir el tamaño de la imagen
                )
            }
        }
    }
}
