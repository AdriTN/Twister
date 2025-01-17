package com.grupo18.twister.ui.components.views.results

import androidx.compose.runtime.Composable
import com.grupo18.twister.models.network.domain.OpcionRespuesta
import com.grupo18.twister.models.network.domain.RespuestaJugador

/**
 * Muestra los resultados de una pregunta, sea en modo Admin o Jugador.
 * - [responses]: Lista de respuestas de los jugadores (modo Admin).
 * - [options]: Opciones disponibles (correcta/incorrectas).
 * - [isAdmin]: Indica si el usuario es Admin.
 * - [score]: Puntuación del jugador (cuando no es Admin).
 * - [respuestaJugador]: Respuesta que dio el jugador.
 * - [onNextQuestionClick]: Callback para pasar a la siguiente pregunta (solo Admin).
 */
@Composable
fun ResultsView(
    responses: List<RespuestaJugador>,
    options: List<OpcionRespuesta>,
    isAdmin: Boolean,
    score: Int,
    respuestaJugador: String,
    onNextQuestionClick: () -> Unit
) {
    if (isAdmin) {
        // Vista para administrador
        AdminResultsSection(
            responses = responses,
            options = options,
            onNextQuestionClick = onNextQuestionClick
        )
    } else {
        // Vista para jugador
        PlayerFeedbackView(
            respuestaJugador = respuestaJugador,
            score = score
        )
    }
}