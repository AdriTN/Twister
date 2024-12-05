package com.grupo18.twister.core.screens.twists

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import com.grupo18.twister.R
import com.journeyapps.barcodescanner.CaptureActivity

@Composable
fun TempTwist(onAuthSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val pinText = remember { mutableStateOf("") }

    fun joinGame(pin: String) {
        // Verificar que el PIN contenga solo dígitos y tenga exactamente 5 caracteres
        if (pin.length != 5 || !pin.all { it.isDigit() }) {
            Toast.makeText(context, "Invalid PIN: It must be 5 digits long and contain only numbers.", Toast.LENGTH_SHORT).show()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Join a Game",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Imagen del logo (opcional)
        Image(
            painter = painterResource(id = R.drawable.ico), // Asegúrate de tener un logo
            contentDescription = "Twister Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp)
        )

        // Campo de texto para agregar el PIN
        TextField(
            value = pinText.value,
            onValueChange = { pinText.value = it },
            label = { Text("Enter Game PIN") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Botón para unirse con el PIN
        ModeButton(
            label = "Join with PIN",
            iconRes = R.drawable.qr,  // Puedes poner un icono adecuado para el PIN
            onClick = { joinGame(pinText.value) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para escanear el código QR
        ModeButton(
            label = "Scan QR Code",
            iconRes = R.drawable.qr,  // Asegúrate de tener el recurso QR adecuado
            onClick = {
                // Acción para iniciar el escaneo de código QR
                val intent = Intent(context, CaptureActivity::class.java)
                launcher.launch(intent)  // Usa el launcher para manejar el resultado
            }
        )
    }
}

@Composable
fun ModeButton(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE), // Color llamativo para el botón
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp), // Bordes redondeados para un estilo más moderno
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen en el botón
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "QR Code Icon",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            // Texto en el botón
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}