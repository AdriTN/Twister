package com.grupo18.twister.core.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.screens.twists.liveTwist.displayPlayerImage
import com.grupo18.twister.R

@Composable
fun SelectablePlayerImage(
    context: Context,
    onSelectionChange: (String, String) -> Unit
) {
    var currentImageIndex by remember { mutableIntStateOf(1) }
    val maxImageIndex = 24
    var playerName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val jockeyFontFamily = FontFamily(Font(R.font.jockeyone))

    val bitmap = remember(currentImageIndex) {
        displayPlayerImage(currentImageIndex.toString(), context)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Logo de la aplicación como fondo
            Image(
                painter = painterResource(id = R.drawable.ico),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(70.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )

            // Texto superpuesto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Agrupando la sombra y el texto "JOINING GAME!"
                Box {
                    // Sombra para el texto "JOINING GAME!"
                    Text(
                        text = "JOINING GAME!",
                        fontSize = 42.sp,
                        fontFamily = jockeyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                    )
                    Text(
                        text = "JOINING GAME!",
                        fontSize = 42.sp,
                        fontFamily = jockeyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Agrupando la sombra y el texto "Please, fill out the form"
                    Text(
                        text = "Please, fill out the form",
                        fontSize = 24.sp,
                        fontFamily = jockeyFontFamily,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
            }
        }

        Spacer(modifier = Modifier.height(46.dp))

        Text(
            text = "Select Your Name",
            fontSize = 24.sp,
            fontFamily = jockeyFontFamily,
            color = Color.Black
        )

        // Campo de texto para ingresar el nombre del jugador
        TextField(
            value = playerName,
            onValueChange = { newName ->
                if (newName.length <= 10) { // Esta comprobación sigue siendo necesaria para limitar la entrada
                    playerName = newName
                    errorMessage = "" // Limpiar el mensaje de error cuando el usuario escribe
                }
            },
            label = {
                Text(
                    text = "Player Name",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            },
            isError = errorMessage.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFF02437C), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Red
            ),
            placeholder = {
                Text("Enter your name", color = Color.Gray)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text // Establece el tipo de teclado, opcional
            ),
            visualTransformation = VisualTransformation.None
        )

        Spacer(modifier = Modifier.height(76.dp))

        Text(
            text = "Select Your Avatar",
            fontSize = 24.sp,
            fontFamily = jockeyFontFamily,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la imagen actual y los botones al lado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Botón izquierdo
            Button(
                onClick = {
                    if (currentImageIndex > 1) {
                        currentImageIndex--
                    } else {
                        currentImageIndex = maxImageIndex
                    }
                },
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Previous Image",
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 180f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(120.dp)
                    )
                } ?: Text("No Image", textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón derecho
            Button(
                onClick = {
                    if (currentImageIndex < maxImageIndex) {
                        currentImageIndex++
                    } else {
                        currentImageIndex = 1
                    }
                },
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Next Image",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Mostrar mensaje de error si el nombre está vacío
        if (errorMessage.isNotEmpty()) {
            Text(
                text = "Error: $errorMessage",
                color = Color.Red,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(76.dp))

        // Botón para confirmar la selección
        Button(
            onClick = {
                if (playerName.isBlank()) {
                    // Mostrar mensaje de error
                    Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    // Llamar a la función onSelectionChange con el índice y el nombre del jugador
                    onSelectionChange(currentImageIndex.toString(), playerName)
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(), // O ajustar según el tamaño que desees
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White
            ),
        ) {
            // Contenedor para el texto y el icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Asegura que el icono esté alineado a la derecha
            ) {
                Text(
                    text = "Confirm",
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f) // Toma todo el espacio disponible
                )
                // Icono a la derecha
                Image(
                    painter = painterResource(id = R.drawable.continueico), // Asegúrate de que el recurso exista
                    contentDescription = "Continue",
                    modifier = Modifier
                        .size(24.dp) // Ajusta el tamaño según necesites
                        .padding(start = 8.dp) // Espaciado entre el texto y el icono
                )
            }
        }
    }
}
