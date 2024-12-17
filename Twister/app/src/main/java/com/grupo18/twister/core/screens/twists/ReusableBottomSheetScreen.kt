package com.grupo18.twister.core.screens.twists

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.grupo18.twister.R
import com.journeyapps.barcodescanner.CaptureActivity

@Composable
fun TempTwist(
    modifier: Modifier = Modifier,
    onAuthSuccess: (pin: String) -> Unit // Actualizado para aceptar pin y nombre
) {
    val context = LocalContext.current
    val pinText = remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) } // Estado para controlar la visibilidad del diálogo de nombre
    val nameText = remember { mutableStateOf("") } // Estado para almacenar el nombre ingresado

    fun joinGame(pin: String) {
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            Toast.makeText(context, "PIN inválido: $pin", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar el diálogo para ingresar el nombre
        showNameDialog = true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                val scannedPin = it.getStringExtra("SCAN_RESULT")
                scannedPin?.let { pin ->
                    pinText.value = pin
                    joinGame(pin)
                } ?: run {
                    Toast.makeText(context, "No se encontró el PIN en el QR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Diálogo para ingresar el nombre
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar el diálogo sin ingresar un nombre */ },
            title = { Text(text = "Ingresa tu nombre") },
            text = {
                TextField(
                    value = nameText.value,
                    onValueChange = { newValue ->
                        // Permitir solo letras y espacios, y limitar la longitud
                        if (newValue.all { it.isLetter() || it.isWhitespace() } && newValue.length <= 20) {
                            nameText.value = newValue
                        }
                    },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = nameText.value.trim()
                        if (name.isNotEmpty()) {
                            onAuthSuccess(pinText.value)
                            // Resetear los estados
                            showNameDialog = false
                            nameText.value = ""
                        } else {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Opcional: Permitir cancelar la acción
                        showNameDialog = false
                        pinText.value = ""
                        nameText.value = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Ajusta la altura al contenido
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Join a Twist Session",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp) // Reducido de 32.dp
        )

        Spacer(modifier = Modifier.height(24.dp)) // Reducido de 60.dp

        TextField(
            value = pinText.value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                    pinText.value = newValue
                    if (newValue.length == 6) {
                        joinGame(newValue)
                    }
                }
            },
            label = { Text("Enter Game PIN") },
            modifier = Modifier
                .fillMaxWidth(0.7f) // Ajuste para adaptarse mejor
                .padding(bottom = 8.dp), // Reducido de 16.dp
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp)) // Reducido de 40.dp

        Box(
            modifier = Modifier
                .size(150.dp) // Ajustado para mejor adaptabilidad
                .clip(RoundedCornerShape(12.dp)) // Ajustado para un aspecto más compacto
                .clickable {
                    val intent = Intent(context, CaptureActivity::class.java)
                    launcher.launch(intent)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.qr),
                contentDescription = "Scan QR Code",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Reducido de 30.dp
    }
}

