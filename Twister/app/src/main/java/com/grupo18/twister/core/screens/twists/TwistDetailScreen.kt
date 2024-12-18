package com.grupo18.twister.core.screens.twists

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.gson.Gson
import com.grupo18.twister.R
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.navigation.Routes
import com.grupo18.twister.core.viewmodel.TwistViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwistDetailScreen(
    navController: NavController,
    twist: TwistModel?,
    twistViewModel: TwistViewModel
) {
    val twists by twistViewModel.twists.collectAsState()
    val twist = twists.find { it.id == twist?.id }

    if (twist == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Twist Detail") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Twist no encontrado.")
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(twist.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                TwistDetailContent(
                    twist = twist,
                    onPlaySolo = {
                        val twistJson = Gson().toJson(twist)
                        navController.navigate("soloTwist/${twistJson}")
                    },
                    navController
                )
            }
        }
    }
}

@Composable
fun TwistDetailContent(
    twist: TwistModel,
    onPlaySolo: () -> Unit,
    navController: NavController,
) {
    val context = LocalContext.current
    val localFilePath = "${context.filesDir}/images/${twist.imageUri}"
    val localFile = File(localFilePath)

    // Painter que carga la imagen local (si existe) o fallback
    val painter = if (localFile.exists()) {
        rememberAsyncImagePainter(localFile)
    } else {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(twist.imageUri)
                .placeholder(R.drawable.default_twist)
                .error(R.drawable.default_twist)
                .build()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Parte superior
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val imageSize = 240.dp
            Box(
                modifier = Modifier
                    .size(240.dp)                // Dimensiones de tu imagen circular
                    .clip(CircleShape),          // Aplica forma de círculo
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,           // Tu rememberAsyncImagePainter(...)
                    contentDescription = twist.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = twist.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ejemplo: rating + “498 Players”
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "498 Players",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(16.dp))
                repeat(5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star rating",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = twist.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Botones al final
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val twistJson = Uri.encode(Gson().toJson(twist))
                        navController.navigate(Routes.GAME_SCREEN.replace("{twist}", twistJson))
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Present")
                }
                Button(
                    onClick = onPlaySolo,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Play Solo")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
