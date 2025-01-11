package com.grupo18.twister.core.screens.twists

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
import androidx.navigation.NavController
import com.google.gson.Gson
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.viewmodel.TwistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTwistDetailScreen(
    navController: NavController,
    twistId: String,
    twistViewModel: TwistViewModel
) {
    var loadedTwist by remember { mutableStateOf<TwistModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

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
                // Usa tu mismo “TwistDetailContent(...)”
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    TwistDetailContent(
                        twist = twist,
                        onPlaySolo = {
                            val twistJson = Gson().toJson(twist)
                            navController.navigate("soloTwist/${twistJson}")
                        },
                        navController = navController
                    )
                }
            } ?: run {
                // El Twist no existe o no es público
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