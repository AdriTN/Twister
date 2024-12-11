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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.viewmodel.TwistViewModel

@Composable
fun HomeScreen(navController: NavController, twistViewModel: TwistViewModel) {
    val context = LocalContext.current
    val app = context.applicationContext as MyApp

    val currentUser by app.getUser().collectAsState()
    val twists by twistViewModel.twists.collectAsState() // Obtener twists desde el ViewModel
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Efecto para cargar twists al iniciar la pantalla
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
                SectionTitle("Your Twists")
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Color.Red
                    )
                } else if (twists.isEmpty()) {
                    Text(
                        text = "You have no twists yet. Create one now!",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    TwistsGrid(
                        items = twists.map { it.title },
                        onItemClick = { title ->
                            // Navegar al detalle del twist
                            val twist = twists.find { it.title == title }
                            if (twist != null) {
                                navController.navigate("twistDetail/${twist.id}")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            SectionTitle("Most Played")
            Spacer(modifier = Modifier.height(16.dp))
            TwistsGrid(items = List(8) { "Popular ${it + 1}" }) { popular ->
                // Acción al seleccionar un elemento popular
            }
        }
    }
}

@Composable
fun TwistsGrid(items: List<String>, onItemClick: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            TwistCard(name = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun TwistCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person, // Puedes cambiar a un ícono más adecuado
                contentDescription = "Twist Icon",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
