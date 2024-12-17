package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.JoinResponse
import com.grupo18.twister.core.models.NewUserResponse
import com.grupo18.twister.core.models.RoomResponse
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.json.Json

// Clase para gestionar eventos en tiempo real
class RealTimeClient(private val socket: Socket) {

    fun listenForEvents(roomId: String? = null, onEventReceived: (Event) -> Unit) {
        // Escuchar eventos comunes
        socket.on("PLAYER_JOINED", Emitter.Listener { args ->
            println("PLAYER_JOINED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                val roomResponse = Json.decodeFromString<JoinResponse>(firstArg)
                println("Jugador unido: $roomResponse")
                // Puedes crear un evento específico si es necesario
                onEventReceived(Event(message = "PLAYER_JOINED: $firstArg", type = "PLAYER_JOINED", id = ""))
            }
        })

        socket.on("PIN_PROVIDED", Emitter.Listener { args ->
            // Imprimir el contenido de args para depurar
            println("PIN_PROVIDED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    // Asegúrate de que el primer argumento sea el JSON esperado
                    println("Argumento recibido: $firstArg")
                    val roomResponse = Json.decodeFromString<RoomResponse>(firstArg)
                    val pin = roomResponse.pin
                    println("PIN recibido: $pin")
                    onEventReceived(Event(message = "PIN_PROVIDED: $firstArg", type = "PIN_PROVIDED", id = ""))
                } catch (e: Exception) {
                    println("Error al deserializar el JSON: ${e.message}") // Manejo de errores
                }
            } else {
                println("Formato de argumento inesperado: ${args.toString()}")
            }
        })

        socket.on("playerJoined", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    // Intenta deserializar el primer argumento a NewUserResponse
                    val roomResponse = Json.decodeFromString<NewUserResponse>(firstArg)
                    // Notifica que se recibió un nuevo evento de jugador
                    onEventReceived(Event(message = roomResponse.toString(), type = "newPlayer", id = ""))
                } catch (e: Exception) {
                    // Manejo de errores más descriptivo
                    println("Error al deserializar el JSON: ${e.localizedMessage}")
                }
            } else {
                // Mensaje de error mejorado si no hay argumentos
                println("Error: Se recibió un argumento vacío o inesperado: ${args.joinToString()}")
            }
        })




        socket.on("GAME_STARTED", Emitter.Listener { args ->
            if (args.isNotEmpty() && args[0] is Map<*, *>) {
                val eventData = args[0] as Map<*, *>
                val roomId = eventData["roomId"] as? String // Asegúrate de que "roomId" esté en los datos
                if (roomId != null) {
                    println("Juego iniciado en la sala: $roomId")
                    onEventReceived(Event("GAME_STARTED: $roomId"))
                } else {
                    println("Error: 'roomId' no encontrado en los datos del evento.")
                }
            }
        })

        // Puedes seguir agregando más eventos aquí de manera similar.
    }

    // Enviar un evento al servidor
    fun sendEvent(event: Event) {
        socket.emit("sendEvent", event)
    }
}
