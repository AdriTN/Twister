package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.models.Event
import android.content.pm.ActivityInfo
import androidx.compose.ui.platform.LocalContext
import com.grupo18.twister.core.api.RealTimeClient
import org.json.JSONObject
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.grupo18.twister.core.models.GameResponse
import com.grupo18.twister.core.models.RoomResponse

fun generateQRCode(text: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        bmp
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}


@Composable
fun WaitingRoom(token: String, onStartGame: (roomId: String) -> Unit) {
    val players = remember { mutableStateListOf<String>() } // List of players
    val socket = ApiClient.getSocket()
    val realTimeClient = remember { RealTimeClient(socket) }
    var isLoading by remember { mutableStateOf(true) } // Loading state
    var pinProvided by remember { mutableStateOf(false) } // Flag to check if PIN is provided
    var pinRoom by remember { mutableStateOf("") } // Store the PIN provided
    var roomId by remember { mutableStateOf("") } // Store the PIN provided
    var isAdmin by remember { mutableStateOf(false) }

    // **Forzar orientación si es admin**
    val context = LocalContext.current

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        else {
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Animation for the dots
    val dots = listOf("", ".", "..", "...")
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val currentIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dots.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val currentDots = dots[(currentIndex.toInt()) % dots.size]

    // Connect the socket and listen for events when the screen loads
    LaunchedEffect(roomId) {
        println("Connecting to socket for room $roomId")
        socket.connect()
        realTimeClient.listenForEvents(roomId) { event ->
            when {
                event.message.startsWith("PLAYER_JOINED: ") -> {
                    val playerName = event.message.removePrefix("PLAYER_JOINED: ")
                    players.add(playerName)
                }
                event.message.startsWith("PIN_PROVIDED: ") -> {
                    val jsonString = event.message.removePrefix("PIN_PROVIDED: ")
                    println("PIN_PROVIDED: $jsonString") // Debug print
                    val game = Gson().fromJson(jsonString, RoomResponse::class.java)
                    println("Game: $game") // Debug print")
                    pinProvided = true
                    isLoading = false
                    isAdmin = true
                    pinRoom = game.pin
                    println(pinRoom)
                }
            }
        }

        val dataJson = JSONObject(mapOf(
            "roomId" to roomId,
            "token" to token,
            "isNew" to true
        )).toString()

        socket.emit("REQUEST_PIN", dataJson)
    }

    // UI de la pantalla existente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .fillMaxHeight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp, bottom = 9.dp), color = Color.DarkGray)
                    Text(
                        text = "Loading",
                        color = Color.DarkGray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for players$currentDots",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = if (isAdmin) 20.dp else 46.dp) // Menor offset si es admin
                )

            }

            // Display the PIN if it has been provided
            if (pinProvided) {
                Text(
                    text = "PIN: ${pinRoom.substring(0,4)} ${pinRoom.substring(4,pinRoom.length)}",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

                // Generar y mostrar el código QR
                val qrBitmap = generateQRCode(roomId)
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.size(128.dp) // Ajusta el tamaño según sea necesario
                    )
                }
            }

            // Styled list of connected players
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (players.isEmpty()) {
                    Text(
                        text = "No players connected yet.",
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    players.forEach { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = player,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (isAdmin){
                Button(
                    onClick = {
                        realTimeClient.sendEvent(Event("START_GAME")) // Notify players
                        onStartGame(roomId) // Go to the next screen
                    },
                    enabled = players.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.4f)
                ) {
                    Text(
                        text = "Start game",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Wait to the admin to start the game",
                    fontSize = 16.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            }

        }

    // Disconnect the socket when leaving the composition
    DisposableEffect(Unit) {
        onDispose {
            socket.disconnect()
        }
    }
}

