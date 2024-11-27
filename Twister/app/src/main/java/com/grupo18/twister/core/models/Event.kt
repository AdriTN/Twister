package com.grupo18.twister.core.models

data class Event(
    val id: String, // Identificador único del evento
    val type: String? = null, // Tipo de evento (por ejemplo, "Square clicked", "Arrow clicked")
    val message: String = "", // Mensaje descriptivo sobre el evento
    val timestamp: Long? = System.currentTimeMillis() // Marca de tiempo del evento (opcional)
)