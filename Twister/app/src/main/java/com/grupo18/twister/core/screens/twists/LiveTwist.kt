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
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.components.ColorBlock
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.viewmodel.RoomViewModel
import io.socket.client.Socket
import io.socket.emitter.Emitter

// Clase para gestionar eventos en tiempo real
class RealTimeClient(private val socket: Socket) {

    // Función para escuchar eventos
    fun listenForEvents(roomId: String? = null, onEventReceived: (Event) -> Unit) {
        // Escuchar el evento "newEvent" (esto depende del evento que envíes desde el servidor)
        socket.on("newEvent", Emitter.Listener { args ->
            if (args.isNotEmpty() && args[0] is Map<*, *>) {
                val eventData = args[0] as Map<*, *>
                val message = eventData["message"] as String
                val event = Event(message) // Asumiendo que Event tiene un constructor que toma un mensaje
                onEventReceived(event)
            }
        })
    }

    // Enviar un evento al servidor
    fun sendEvent(event: Event) {
        // Aquí deberías usar el método adecuado para enviar el evento por el socket
        socket.emit("sendEvent", event) // "sendEvent" es un ejemplo; debe coincidir con el evento que tu servidor espera
    }
}

@Composable
fun LiveTwist(roomId: String) {
    // Estado para almacenar los eventos recibidos
    val events = remember { mutableStateListOf<Event>() }
    val roomViewModel = remember { RoomViewModel() }

    // Inicializa el cliente de sockets
    val socket = ApiClient.getSocket()
    val realTimeClient = remember { RealTimeClient(socket) }

    // Inicia la escucha de eventos al cargar la composición
    LaunchedEffect(roomId) {
        socket.connect() // Conectar el socket
        realTimeClient.listenForEvents(roomId) { event ->
            events.add(event) // Agregar el evento recibido a la lista de eventos
        }
    }

    // LaunchedEffect para enviar eventos usando el ViewModel
    LaunchedEffect(roomId) {
        roomViewModel.listenForNewQuestions(roomId) // Si aún lo necesitas
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
                    realTimeClient.sendEvent(Event("Arrow clicked"))
                }
            )
            ColorBlock(
                color = Color(0xFFE94E3B),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Circle,
                contentDescription = "Circle",
                onClick = {
                    realTimeClient.sendEvent(Event("Circle clicked"))
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
                    realTimeClient.sendEvent(Event("Square clicked"))
                }
            )
            ColorBlock(
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Hexagon,
                contentDescription = "Hexagon",
                onClick = {
                    realTimeClient.sendEvent(Event("Hexagon clicked"))
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

    // Desconectar el socket al salir de la composición
    DisposableEffect(Unit) {
        onDispose {
            socket.disconnect()
        }
    }
}
