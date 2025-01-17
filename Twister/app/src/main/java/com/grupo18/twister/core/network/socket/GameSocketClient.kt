package com.grupo18.twister.core.network.socket

import com.grupo18.twister.models.common.Event
import com.grupo18.twister.models.network.events.AnswerProvidedEvent
import com.grupo18.twister.models.network.events.GameOverEvent
import com.grupo18.twister.models.network.events.GameStateEvent
import com.grupo18.twister.models.network.events.ScoreEvent
import com.grupo18.twister.models.network.events.TopScoresEvent
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.json.Json

class GameSocketClient(private val socket: Socket) {

    /**
     * Escucha eventos que ocurren DURANTE la partida
     */
    fun listenForInGameEvents(onEventReceived: (Event) -> Unit) {

        // GAME_STATE
        socket.on("GAME_STATE", Emitter.Listener { args ->
            println("GAME_STATE: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val gameState = Json.decodeFromString<GameStateEvent>(jsonString)
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

        // NEXT_QUESTION
        socket.on("NEXT_QUESTION", Emitter.Listener { args ->
            println("NEXT_QUESTION: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                onEventReceived(Event(
                    message = "NEXT_QUESTION",
                    type = "NEXT_QUESTION",
                    id = ""
                ))
            }
        })

        // ANSWER_PROVIDED
        socket.on("ANSWER_PROVIDED", Emitter.Listener { args ->
            println("ANSWER_PROVIDED: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val answer = Json.decodeFromString<AnswerProvidedEvent>(jsonString)
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

        // ANSWER_SENT
        socket.on("ANSWER_SENT", Emitter.Listener { args ->
            println("ANSWER_SENT: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val answer = Json.decodeFromString<ScoreEvent>(jsonString)
                    onEventReceived(Event(
                        message = "ANSWER_SENT: ${answer.score}",
                        type = "ANSWER_SENT",
                        id = answer.score.toString()
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar ANSWER_SENT: ${e.localizedMessage}")
                }
            }
        })

        // TOP_SCORES
        socket.on("TOP_SCORES", Emitter.Listener { args ->
            println("TOP_SCORES: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val topScoresEvent = Json.decodeFromString<TopScoresEvent>(jsonString)
                    val winners = topScoresEvent.topScores.joinToString(", ") { "${it.name}: ${it.score}" }
                    onEventReceived(Event(
                        message = winners,
                        type = "TOP_SCORES",
                        id = "TOP_3"
                    ))
                } catch (e: Exception) {
                    println("Error al deserializar TOP_SCORES: ${e.localizedMessage}")
                }
            }
        })

        // GAME_OVER
        socket.on("GAME_OVER", Emitter.Listener { args ->
            println("GAME_OVER: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val jsonString = args[0].toString()
                try {
                    val gameOver = Json.decodeFromString<GameOverEvent>(jsonString)
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

        // ANSWERS
        socket.on("ANSWERS", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                println("ANSWERS: ${args.joinToString()}")
                val jsonString = args[0].toString()
                onEventReceived(Event(
                    message = jsonString,
                    type = "ANSWERS",
                    id = ""
                ))
            }
        })

        // CORRECT_ANSWER
        socket.on("CORRECT_ANSWER", Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                println("CORRECT_ANSWER: ${args.joinToString()}")
                val jsonString = args[0].toString()
                onEventReceived(Event(
                    message = jsonString,
                    type = "CORRECT_ANSWER",
                    id = ""
                ))
            }
        })
    }

    /**
     * Métodos para emitir eventos DURANTE el juego
     */

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

    /**
     * Si en esta fase se requiere unirse a la sala de juego:
     */
    fun joinGameRoom(roomId: String) {
        socket.emit("joinGameRoom", roomId)
    }
}
