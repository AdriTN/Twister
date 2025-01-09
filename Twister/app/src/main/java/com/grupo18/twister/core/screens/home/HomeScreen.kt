package com.grupo18.twister.core.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.grupo18.twister.R
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.viewmodel.TwistViewModel
import java.io.File

@Composable
fun HomeScreen(navController: NavController, twistViewModel: TwistViewModel) {
    val context = LocalContext.current
    val app = context.applicationContext as MyApp

    val currentUser by app.getUser().collectAsState()
    val twists by twistViewModel.twists.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val publicTwists by twistViewModel.publicTwists.collectAsState()

    // Efecto para cargar los twists al iniciar la pantalla
    LaunchedEffect(currentUser) {
        if (currentUser?.isAnonymous == false) {
            isLoading = true
            errorMessage = null
            try {
                twistViewModel.clearTwists()
                twistViewModel.loadTwists(
                    token = currentUser!!.token,
                    scope = coroutineScope,
                    context = context
                ) { loading ->
                    isLoading = loading
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        twistViewModel.loadPublicTwists(
            token = currentUser?.token ?: "",
            scope = coroutineScope,
            context = context
        ) { loading ->
            isLoading = loading
        }
    }

    Scaffold(
        bottomBar = { CustomBottomNavigationBar(navController) }
    ) { padding ->
        // Envolvemos todo el contenido en un Column scrolleable
        Column(
            modifier = Modifier
                .fillMaxSize()
                // IMPORTANTE: activamos el scroll vertical
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Cabecera con nombre de usuario o botón de login
            HeaderWithProfileOrLogin(currentUser, navController)

            Spacer(modifier = Modifier.height(24.dp))

            if (currentUser?.isAnonymous == false) {
                SectionTitle("Your Twists")
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    // Indicador de carga
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (!errorMessage.isNullOrEmpty()) {
                    // Mensaje de error
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (twists.isEmpty()) {
                    // Sin twists
                    Text(
                        text = "You have no twists yet. Create one now!",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Sección horizontal (twists del usuario)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(twists) { twist ->
                            TwistCard(
                                twist = twist,
                                onClick = {
                                    val twistJson = Gson().toJson(twist)
                                    navController.navigate("twistDetail/${twistJson}")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Otra sección horizontal ("Most Played")
            // Renombramos la sección
            SectionTitle("Public Twists")
            Spacer(modifier = Modifier.height(16.dp))

// Si quieres reciclar la variable `isLoading`, la usas también aquí.
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (publicTwists.isEmpty()) {
                Text(
                    text = "No public twists found.",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    items(publicTwists) { twist ->
                        TwistCard(
                            twist = twist,
                            onClick = {
                                val twistJson = Gson().toJson(twist)
                                navController.navigate("twistDetail/${twistJson}")
                            }
                        )
                    }
                }
            }

            // ¡Listo! Todo el contenido se puede desplazar verticalmente
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Mantenemos el resto de composables tal y como estaban...

@Composable
fun TwistCard(twist: TwistModel, onClick: () -> Unit) {
    val context = LocalContext.current

    // Determinamos la fuente de la imagen:
    val localFilePath = "${context.filesDir}/images/${twist.imageUri}"
    val localFile = File(localFilePath)

    val painter = when {
        localFile.exists() -> {
            rememberAsyncImagePainter(model = localFile)
        }
        else -> {
            // Siempre un painter de la imagen por defecto
            rememberAsyncImagePainter(R.drawable.default_twist)
        }
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(270.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(6.dp))

            // Imagen circular
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Twist Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = twist.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = twist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HeaderWithProfileOrLogin(currentUser: UserModel?, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            if (currentUser?.isAnonymous == true) {
                Text(
                    text = "You are browsing as a guest. Please log in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = "Hi, ${currentUser?.username ?: "User"}!",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        if (currentUser?.isAnonymous == true) {
            Button(
                onClick = { navController.navigate("auth") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Login")
            }
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.navigate("profile") },
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
