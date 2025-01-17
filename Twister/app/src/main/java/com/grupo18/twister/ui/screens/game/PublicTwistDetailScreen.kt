package com.grupo18.twister.ui.screens.game

import android.util.Base64
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.gson.Gson
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.viewmodels.screens.TwistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTwistDetailScreen(
    navController: NavController,
    twistId: String,
    twistViewModel: TwistViewModel
) {
    var loadedTwist by remember { mutableStateOf<TwistModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val applicationContext = LocalContext.current.applicationContext

    // Llamas a la función que obtiene UN twist público por su ID
    LaunchedEffect(twistId) {
        twistViewModel.loadPublicTwistById(twistId) { twist ->
            loadedTwist = twist
            isLoading = false
        }
    }

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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            loadedTwist?.let { twist ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    TwistDetailContent(
                        twist = twist,
                        onPlaySolo = {
                            (applicationContext as MyApp).saveTwist(twist)
                            navController.navigate("soloTwist")
                        },
                        navController = navController
                    )
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Twist no encontrado.")
                }
            }
        }
    }
}