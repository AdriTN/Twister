// Archivo: SettingsScreen.kt
package com.grupo18.twister.core.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.UserModel
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onSendTestNotification: () -> Unit,
    user: UserModel? // Recibir el objeto UserModel
) {
    // Estado de permiso para notificaciones
    val notificationPermissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    // Estado para controlar si las notificaciones están habilitadas
    var notificationsEnabled by rememberSaveable { mutableStateOf(false) }

    // Solicitar permiso cuando las notificaciones están habilitadas
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = { CustomBottomNavigationBar(navController) }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección: Configuración de la cuenta
            item {
                AccountSection(user = user)
            }

            // Botón para cerrar sesión
            item {
                Button(
                    onClick = { onLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Log Out")
                }
            }

            // Divider
            item {
                Divider()
            }

            // Sección: Configuración de notificaciones
            item {
                NotificationSettings(
                    notificationsEnabled = notificationsEnabled,
                    onToggleNotifications = { isEnabled ->
                        notificationsEnabled = isEnabled
                        if (isEnabled) {
                            notificationPermissionState.launchPermissionRequest()
                        } else {
                            // Lógica para deshabilitar notificaciones (opcional)
                            // Por ejemplo, cancelar suscripciones o servicios de notificaciones
                        }
                    },
                    permissionState = notificationPermissionState
                )
            }

            // Botón para enviar una notificación de prueba
            item {
                Button(
                    onClick = { onSendTestNotification() },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Enviar Notificación de Prueba")
                }
            }

            // Divider
            item {
                Divider()
            }

            // Sección: Configuración del tema
            item {
                ThemeSettings(isDarkTheme, onToggleTheme)
            }

            // Divider
            item {
                Divider()
            }

            // Sección: Otras opciones
            item {
                OtherSettings()
            }

            // Divider
            item {
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationSettings(
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    permissionState: PermissionState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Notifications",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Enable Notifications", fontSize = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onToggleNotifications
            )
        }

        // Mostrar el estado del permiso
        when (permissionState.status) {
            is PermissionStatus.Granted -> {
                Text(
                    "Notifications are enabled.",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is PermissionStatus.Denied -> {
                val shouldShowRationale =
                    permissionState.status.shouldShowRationale
                if (shouldShowRationale) {
                    Text(
                        "Please allow notifications to stay updated.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        "Notifications are denied. Please enable them in settings.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSection(user: UserModel?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Account", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = user?.username ?: "Nombre de Usuario",
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = user?.email ?: "correo@ejemplo.com",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ThemeSettings(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Dark Mode",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Dark Mode", fontSize = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkTheme,
                onCheckedChange = onToggleTheme
            )
        }
    }
}

@Composable
fun OtherSettings() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("More Options", style = MaterialTheme.typography.titleLarge)

        SettingItem("Privacy Policy") { /* Acción al pulsar */ }
        SettingItem("Terms of Service") { /* Acción al pulsar */ }
        SettingItem("Help & Support") { /* Acción al pulsar */ }
    }
}

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp)
    }
}
