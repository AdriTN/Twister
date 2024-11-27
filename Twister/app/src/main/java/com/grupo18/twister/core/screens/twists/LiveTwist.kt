package com.grupo18.twister.core.screens.twists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grupo18.twister.core.components.ColorBlock
import com.grupo18.twister.core.interfaces.RealTimeApi
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.viewmodel.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope


// Clase para gestionar eventos en tiempo real
class RealTimeClient(private val api: RealTimeApi) {
    private val scope = CoroutineScope(Dispatchers.IO)
    // Long polling para escuchar eventos
    fun listenForEvents(roomId: String, onEventReceived: (Event) -> Unit) {
        scope.launch {
            try {
                while (true) { // Hacer solicitudes continuamente
                    val events = api.getEventsLongPolling(roomId)
                    // Procesar los eventos
                    events.forEach { event ->
                        onEventReceived(event) // Notificar a la UI de un nuevo evento
                    }
                    // Puedes agregar un retraso si es necesario para evitar hacer solicitudes
                    // demasiado frecuentes en intervalos de tiempo cortos
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejar errores (por ejemplo, reconexión automática si la conexión se pierde)
            }
        }
    }

    // Enviar un evento al servidor
    suspend fun sendEvent(event: Event) {
        // Se ejecuta en el scope adecuado
        withContext(Dispatchers.IO) {
            api.sendEvent(event)
        }
    }
}


@Composable
fun LiveTwist(roomId: String) {
    // Estado para almacenar los eventos recibidos
    val events = remember { mutableStateListOf<Event>() }
    val coroutineScope = rememberCoroutineScope()
    val roomViewModel = remember { RoomViewModel() }

    // Inicia la escucha de eventos en tiempo real con long polling
    LaunchedEffect(roomId) {
        roomViewModel.listenForNewQuestion(roomId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar botones para interactuar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ColorBlock(
                color = Color(0xFF4A90E2),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.ArrowForward,
                contentDescription = "Arrow",
                onClick = {
                    coroutineScope.launch {
                        roomViewModel.sendEvent(Event("Arrow clicked"))
                    }
                }
            )
            ColorBlock(
                color = Color(0xFFE94E3B),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Circle,
                contentDescription = "Circle",
                onClick = {
                    coroutineScope.launch {
                        roomViewModel.sendEvent(Event("Circle clicked"))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ColorBlock(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Stop,
                contentDescription = "Square",
                onClick = {
                    coroutineScope.launch {
                        roomViewModel.sendEvent(Event("Square clicked"))
                    }
                }
            )
            ColorBlock(
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Hexagon,
                contentDescription = "Hexagon",
                onClick = {
                    coroutineScope.launch {
                        roomViewModel.sendEvent(Event("Hexagon clicked"))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar eventos recibidos
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            events.forEach { event ->
                // Componente para mostrar cada evento
                Text(text = event.message, color = Color.Gray)
            }
        }
    }
}

