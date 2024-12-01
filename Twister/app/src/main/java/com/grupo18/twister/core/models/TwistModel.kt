package com.grupo18.twister.core.models

import java.util.UUID

data class TwistModel(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
