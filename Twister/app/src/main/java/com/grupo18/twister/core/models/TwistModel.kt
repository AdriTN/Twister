package com.grupo18.twister.core.models

import android.net.Uri

data class TwistModel(
    val id: String = "00000",
    val title: String,
    val description: String,
    val imageUri: Uri? = null
)
