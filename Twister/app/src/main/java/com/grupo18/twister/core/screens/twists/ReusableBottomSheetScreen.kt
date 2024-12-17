package com.grupo18.twister.core.screens.twists

import android.R.attr.onClick
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.grupo18.twister.R
import com.journeyapps.barcodescanner.CaptureActivity

@Composable
fun TempTwist(onAuthSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val pinText = remember { mutableStateOf("") }

    fun joinGame(pin: String) {
        // Verificar que el PIN contenga solo dígitos y tenga exactamente 5 caracteres
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            //Toast.makeText(context, "Invalid PIN: It must be 8 digits long and contain only numbers. The provided is $pin", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Invalid PIN: $pin", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Attempting to join game with PIN: $pin", Toast.LENGTH_SHORT).show()
        // Llama a la función de éxito de autenticación
        onAuthSuccess(pin)
    }

    // Llamada al launcher para escanear el QR
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                // Suponiendo que el QR escaneado devuelve un string con el PIN
                val scannedPin = it.getStringExtra("SCAN_RESULT") // Ajusta esto según la implementación de tu escáner
                scannedPin?.let { pin ->
                    pinText.value = pin // Asigna el PIN escaneado al TextField
                    joinGame(pin)
                } ?: run {
                    Toast.makeText(context, "No se encontró el PIN en el QR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Column para la interfaz
    Column(
        modifier = Modifier
            .fillMaxWidth()  // Mantener el ancho completo
            .wrapContentHeight()  // Ajustar la altura al contenido
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Join a Twist Session",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Campo de texto para agregar el PIN
        TextField(
            value = pinText.value,
            onValueChange = { newValue ->
                // Permitir solo números
                if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                    pinText.value = newValue
                    // Cuando alcanza 6 caracteres, enviar la request automáticamente
                    if (newValue.length == 6) {
                        joinGame(newValue)
                    }
                }
            },
            label = { Text("Enter Game PIN") },
            modifier = Modifier
                .width(260.dp)
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, // Fondo transparente cuando está enfocado
                unfocusedContainerColor = Color.Transparent // Fondo transparente cuando no está enfocado
            ),
            singleLine = true // Evita salto de línea
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(190.dp) // Ajusta el tamaño según tus necesidades
                .clip(RoundedCornerShape(18.dp)) // Recorta las esquinas de la caja
                .clickable {
                    // Acción para iniciar el escaneo de código QR
                    val intent = Intent(context, CaptureActivity::class.java)
                    launcher.launch(intent)  // Usa el launcher para manejar el resultado
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.qr),
                contentDescription = "Scan QR Code",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }


        Spacer(modifier = Modifier.height(30.dp))
    }
}


