package com.grupo18.twister.core.network.socket

import com.grupo18.twister.models.common.Event
import com.grupo18.twister.models.network.domain.UploadSocketGameRequest
import com.grupo18.twister.models.network.events.StartResponse
import com.grupo18.twister.models.network.responses.JoinPinResponse
import com.grupo18.twister.models.network.responses.RoomResponse
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.json.Json

class LobbySocketClient(private val socket: Socket) {

    var actualRoom: String? = null

    fun updateRoomId(newRoomId: String) {
        actualRoom = newRoomId
        println("RoomId actualizado a: $actualRoom")
    }

    /**
     * Escucha eventos de LOBBY: jugadores que se unen, PINs, etc.
     */
    fun listenForLobbyEvents(onEventReceived: (Event) -> Unit) {
        // PLAYER_JOINED
        socket.on("PLAYER_JOINED", Emitter.Listener { args ->
            println("PLAYER_JOINED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val response = Json.decodeFromString<JoinPinResponse>(jsonString)
                    println("Jugador unido: $response")
                    onEventReceived(Event(
                        message = "PLAYER_JOINED: $jsonString",
                        type = "PLAYER_JOINED",
                        id = ""
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar PLAYER_JOINED: ${e.message}")
                }
            }
        })

        // PIN_PROVIDED
        socket.on("PIN_PROVIDED", Emitter.Listener { args ->
            println("PIN_PROVIDED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val roomResponse = Json.decodeFromString<RoomResponse>(jsonString)
                    val pin = roomResponse.pin
                    println("PIN recibido: $pin")
                    onEventReceived(Event(
                        message = "PIN_PROVIDED: $jsonString",
                        type = "PIN_PROVIDED",
                        id = ""
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar PIN_PROVIDED: ${e.message}")
                }
            }
        })

        // playerJoined (versión minúscula)
        socket.off("playerJoined").on("playerJoined", Emitter.Listener { args ->
            println("playerJoined: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                onEventReceived(Event(
                    message = "newPlayer: $jsonString",
                    type = "newPlayer",
                    id = ""
                ))
            }
        })

        // PIN_STARTED_PROVIDED
        socket.on("PIN_STARTED_PROVIDED", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                onEventReceived(Event(
                    message = "PIN_STARTED_PROVIDED: $jsonString",
                    type = "PIN_STARTED_PROVIDED",
                    id = ""
                ))
            }
        })

        // GAME_STARTED
        socket.on("GAME_STARTED", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                val startResponse = Json.decodeFromString<StartResponse>(jsonString)
                println("GAME_IS_STARTING: $startResponse")
                onEventReceived(Event(
                    message = "GAME_IS_STARTING: $jsonString",
                    type = "GAME_IS_STARTING",
                    id = ""
                ))
            }
        })

        // roomDeleted
        socket.on("roomDeleted", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                println("roomDeleted: $jsonString")
                onEventReceived(Event(
                    message = "Disconnected: $jsonString",
                    type = "Disconnected",
                    id = ""
                ))
            }
        })

        // playerLeft
        socket.on("playerLeft", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                println("Player left: $jsonString")
                onEventReceived(Event(
                    message = "playerLeft: $jsonString",
                    type = "playerLeft",
                    id = ""
                ))
            }
        })

        // PLAYERS_LEFT_LIST
        socket.on("PLAYERS_LEFT_LIST", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                println("PLAYERS_LEFT_LIST: $jsonString")
                onEventReceived(Event(
                    message = "PLAYERS_LEFT_LIST: $jsonString",
                    type = "PLAYERS_LEFT_LIST",
                    id = ""
                ))
            }
        })
    }

    /**
     * Envía un evento genérico al servidor
     */
    fun sendEvent(event: Event) {
        socket.emit("sendEvent", event)
    }

    /**
     * Inicia el juego desde la sala
     */
    fun startGame(roomId: String) {
        socket.emit("startGame", roomId)
    }

    /**
     * Subir configuración de juego (p.ej. twistId, etc.)
     */
    fun uploadGame(uploadGame: UploadSocketGameRequest) {
        val jsonString = """{
            "roomId": "${uploadGame.roomId}",
            "twistId": "${uploadGame.twistId}"
        }"""
        socket.emit("getGame", jsonString)
    }
}
