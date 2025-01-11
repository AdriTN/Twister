package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.AnswerProvidedEvent
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.GameOverEvent
import com.grupo18.twister.core.models.GameStateEvent
import com.grupo18.twister.core.models.JoinPinResponse
import com.grupo18.twister.core.models.RoomResponse
import com.grupo18.twister.core.models.ScoreEvent
import com.grupo18.twister.core.models.StartResponse
import com.grupo18.twister.core.models.TopScoresEvent
import com.grupo18.twister.core.models.UploadSocketGameRequest
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.json.Json

// Clase para gestionar eventos en tiempo real
class RealTimeClient(private val socket: Socket) {

    var actualRoom: String? = null

    fun updateRoomId(newRoomId: String) {
        actualRoom = newRoomId
        println("nroomId actualizado a: $actualRoom")
    }

    fun listenForEvents(roomId: String? = null, onEventReceived: (Event) -> Unit) {
        // Escuchar eventos comunes
        socket.on("PLAYER_JOINED", Emitter.Listener { args ->
            println("Player_joined: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                val roomResponse = Json.decodeFromString<JoinPinResponse>(firstArg)
                println("Jugador unido: $roomResponse")
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

        socket.off("playerJoined").on("playerJoined", Emitter.Listener { args ->
            println("playerJoined: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                println("Player joined: $firstArg")
                try {
                    onEventReceived(Event(message = "newPlayer: $firstArg", type = "newPlayer", id = ""))
                } catch (e: Exception) {
                    // Manejo de errores más descriptivo
                    println("Error al deserializar el JSON: ${e.localizedMessage}")
                }
            } else {
                // Mensaje de error mejorado si no hay argumentos
                println("Error: Se recibió un argumento vacío o inesperado: ${args.joinToString()}")
            }
        })

        socket.on("PIN_STARTED_PROVIDED", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    onEventReceived(Event(message = "PIN_STARTED_PROVIDED: $firstArg", type = "PIN_STARTED_PROVIDED", id = ""))
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
            if (args.isNotEmpty()) {
                var firstArg = args[0].toString()
                val startResponse: StartResponse = Json.decodeFromString(firstArg)
                println("GAME_IS_STARTING: $startResponse")
                onEventReceived(Event(message = "GAME_IS_STARTING: $firstArg", type = "GAME_IS_STARTING", id = ""))
            }
        })

        socket.on("roomDeleted", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    println("Argumento recibido roomDeleted: $firstArg")
                    onEventReceived(Event(message = "Disconnected: $firstArg", type = "Disconnected", id = ""))
                } catch (e: Exception) {
                    // Manejo de errores más descriptivo
                    println("Error al deserializar el JSON: ${e.localizedMessage}")
                }
            } else {
                // Mensaje de error mejorado si no hay argumentos
                println("Error: Se recibió un argumento vacío o inesperado: ${args.joinToString()}")
            }
        })

        socket.on("playerLeft", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    println("Player left: $firstArg")
                    onEventReceived(Event(message = "playerLeft: $firstArg", type = "playerLeft", id = ""))
                } catch (e: Exception) {
                    // Manejo de errores más descriptivo
                    println("Error al deserializar el JSON: ${e.localizedMessage}")
                }
            } else {
                // Mensaje de error mejorado si no hay argumentos
                println("Error: Se recibió un argumento vacío o inesperado: ${args.joinToString()}")
            }
        })

        socket.on("PLAYERS_LEFT_LIST", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    println("PLAYERS_LEFT_LIST: $firstArg")
                    onEventReceived(Event(message = "PLAYERS_LEFT_LIST: $firstArg", type = "PLAYERS_LEFT_LIST", id = ""))
                } catch (e: Exception) {
                    // Manejo de errores más descriptivo
                    println("Error al deserializar el JSON: ${e.localizedMessage}")
                }
            } else {
                // Mensaje de error mejorado si no hay argumentos
                println("Error: Se recibió un argumento vacío o inesperado: ${args.joinToString()}")
            }
        })
    }

    fun listenForEventsInGame(roomId: String? = null, onEventReceived: (Event) -> Unit) {
        // Evento: Estado actual del juego
        socket.on("GAME_STATE", Emitter.Listener { args ->
            println("GAME_STATE: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    val gameState = Json.decodeFromString<GameStateEvent>(firstArg)
                    onEventReceived(Event(
                        message = "GAME_STATE: ${gameState.state}",
                        type = "GAME_STATE",
                        id = ""
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar GAME_STATE: ${e.localizedMessage}")
                }
            }
        })

        // Evento: Siguiente pregunta
        socket.on("NEXT_QUESTION", Emitter.Listener { args ->
            println("NEXT_QUESTION: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    onEventReceived(Event(
                        message = "NEXT_QUESTION",
                        type = "NEXT_QUESTION",
                        id = ""
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar NEXT_QUESTION: ${e.localizedMessage}")
                }
            }
        })

        // Evento: Respuesta proporcionada por un jugador
        socket.on("ANSWER_PROVIDED", Emitter.Listener { args ->
            println("ANSWER_PROVIDED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    val answer = Json.decodeFromString<AnswerProvidedEvent>(firstArg)
                    onEventReceived(Event(
                        message = "ANSWER_PROVIDED por ${answer.playerId}: ${answer.answer}",
                        type = "ANSWER_PROVIDED",
                        id = answer.playerId
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar ANSWER_PROVIDED: ${e.localizedMessage}")
                }
            }
        })

        socket.on("ANSWER_SENT", Emitter.Listener { args ->
            println("ANSWER_SENT: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    val answer = Json.decodeFromString<ScoreEvent>(firstArg)
                    onEventReceived(Event(
                        message = "ANSWER_SENT: ${answer.score}",
                        type = "ANSWER_SENT",
                        id = answer.score.toString()
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar ANSWER_PROVIDED: ${e.localizedMessage}")
                }
            }
        })

        // Evento: Top 3 players
        socket.on("TOP_SCORES", Emitter.Listener { args ->
            println("TOP_SCORES: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    // Intenta deserializar el primer argumento a un objeto específico de TopScoresEvent
                    val topScoresEvent = Json.decodeFromString<TopScoresEvent>(firstArg)
                    val winners = topScoresEvent.topScores.joinToString(", ") { "${it.name}: ${it.score}" }

                    onEventReceived(Event(
                        message = winners,
                        type = "TOP_SCORES",
                        id = "TOP_3" // Usa un identificador general para este evento
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar TOP_SCORES: ${e.localizedMessage}")
                }
            }
        })


        // Evento: Juego finalizado
        socket.on("GAME_OVER", Emitter.Listener { args ->
            println("GAME_OVER: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val firstArg = args[0].toString()
                try {
                    val gameOver = Json.decodeFromString<GameOverEvent>(firstArg)
                    onEventReceived(Event(
                        message = "GAME_OVER. Ganador: ${gameOver.winnerId}",
                        type = "GAME_OVER",
                        id = gameOver.winnerId
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar GAME_OVER: ${e.localizedMessage}")
                }
            }
        })

//        // Evento: Tiempo de respuesta expirado para una pregunta
//        socket.on("QUESTION_TIMEOUT", Emitter.Listener { args ->
//            println("QUESTION_TIMEOUT: ${args.joinToString()}")
//            if (args.isNotEmpty()) {
//                val firstArg = args[0].toString()
//                try {
//                    val timeout = Json.decodeFromString<QuestionTimeoutEvent>(firstArg)
//                    onEventReceived(Event(
//                        message = "QUESTION_TIMEOUT para la pregunta: ${timeout.questionId}",
//                        type = "QUESTION_TIMEOUT",
//                        id = timeout.questionId
//                    ))
//                } catch (e: Exception) {
//                    println("Error al deserializar QUESTION_TIMEOUT: ${e.localizedMessage}")
//                }
//            }
//        })

        socket.on("ANSWERS", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                println("ANSWERS: ${args.joinToString()}")
                // El primer argumento recibido como JSON en forma de String
                val firstArg = args[0].toString()

                // Encapsular el JSON en un objeto Event y enviarlo
                onEventReceived(
                    Event(
                        message = firstArg, // El JSON crudo recibido
                        type = "ANSWERS",
                        id = "" // Puedes ajustar este campo si necesitas un identificador
                    )
                )
            }
        })

        socket.on("CORRECT_ANSWER", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                println("CORRECT_ANSWER: ${args.joinToString()}")
                // El primer argumento recibido como JSON en forma de String
                val firstArg = args[0].toString()

                // Encapsular el JSON en un objeto Event y enviarlo
                onEventReceived(
                    Event(
                        message = firstArg, // El JSON crudo recibido
                        type = "CORRECT_ANSWER",
                        id = "" // Puedes ajustar este campo si necesitas un identificador
                    )
                )
            }
        })


        // Opcional: Filtrar eventos por roomId si es necesario
        if (roomId != null) {
            socket.emit("joinGameRoom", roomId)
        }
    }

    // Enviar un evento al servidor
    fun sendEvent(event: Event) {
        socket.emit("sendEvent", event)
    }

    fun startGame(roomId: String) {
        socket.emit("startGame", roomId)
    }

    fun uploadGame(uploadGame: UploadSocketGameRequest) {
        // Construir la cadena JSON manualmente
        val jsonString = """{
        "roomId": "${uploadGame.roomId}",
        "twistId": "${uploadGame.twistId}"
    }"""

        // Emitir la cadena JSON a través del socket
        socket.emit("getGame", jsonString)
    }

    fun uploadAnswer(answer: String, roomId: String?, playerName: String? = null, questionId: String, time: Int) {
        val jsonString = """{
        "answer": "$answer",
        "roomId": "$roomId",
        "questionId": "$questionId",
        "time": "$time",
        "playerName": "$playerName"
    }"""
        socket.emit("sendAnswer", jsonString)
    }

    fun getAnswers(roomId: String, questionId: String){
        val jsonString = """{
        "roomId": "$roomId",
        "questionId": "$questionId"
    }"""
        println("getAnswers: $jsonString")
        socket.emit("getAnswers", jsonString)
    }

    fun nextQuestion(roomId: String, questionId: String){
        val jsonString = """{
        "roomId": "$roomId",
        "questionId": "$questionId"
    }"""
        println("nextQuestion: $jsonString")
        socket.emit("nextQuestion", jsonString)
    }

    fun getCorrectAnswer(roomId: String, questionId: String, playerName: String){
        val jsonString = """{
        "roomId": "$roomId",
        "questionId": "$questionId",
        "playerName": "$playerName"
    }"""
        println("getCorrectAnswer: $jsonString")
        socket.emit("getCorrectAnswer", jsonString)
    }

    fun getTopPlayers(roomId: String){
        val jsonString = """{
        "roomId": "$roomId"
    }"""
        println("getTopScores: $jsonString")
        socket.emit("getTopScores", jsonString)
    }

    fun gameOverEvent(roomId: String){
        val jsonString = """{
        "roomId": "$roomId"
    }"""
        println("gameOverEvent: $jsonString")
        socket.emit("gameOverEvent", jsonString)
    }
}
