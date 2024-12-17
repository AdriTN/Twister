package com.grupo18.twister.core.screens.twists.liveTwist

import android.content.Context
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
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.grupo18.twister.core.models.JoinResponse
import com.grupo18.twister.core.models.NewUserResponse
import com.grupo18.twister.core.models.PlayerModel
import com.grupo18.twister.core.models.RoomResponse
import com.grupo18.twister.core.screens.authentication.MyApp
import java.io.IOException

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

fun displayPlayerImage(imageId: String, context: Context): Bitmap? {
    println("El imageId es $imageId")
    if (imageId.isEmpty()) return null
    val imageIde = 1
    val imagePath = "player_avatars/ico ($imageIde).png" // Construir la ruta de la imagen en assets
    println(imagePath)
    val assetManager = context.assets
    return try {
        // Abrir el archivo de imagen desde assets
        val inputStream = assetManager.open(imagePath)
        // Decodificar el inputStream en un Bitmap
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace() // Manejar la excepción si la imagen no se encuentra
        null
    }
}

@Composable
fun PlayerItem(player: PlayerModel, context: Context) {
    val bitmap = remember(player.imageId) { displayPlayerImage(player.imageId, context) }
    println("Se va a cargar la imagen del jugador ${player.id}")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Avatar de ${player.id}",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun WaitingRoom(token: String, userIsAdmin: Boolean, pin: String, onStartGame: (roomId: String) -> Unit) {
    val players = remember { mutableStateListOf<PlayerModel>() } // List of players
    val socket = ApiClient.getSocket()
    val realTimeClient = remember { RealTimeClient(socket) }
    var isLoading by remember { mutableStateOf(true) } // Loading state
    var pinProvided by remember { mutableStateOf(false) } // Flag to check if PIN is provided
    var pinRoom by remember { mutableStateOf("") } // Store the PIN provided
    var isInRoom by remember { mutableStateOf(false) } // Store the PIN provided
    var roomId by remember { mutableStateOf("0000") } // Store the PIN provided
    var isAdmin by remember { mutableStateOf(userIsAdmin) }
    val context = LocalContext.current
    val app = context.applicationContext as MyApp
    val currentUser by app.getUser().collectAsState()

    LaunchedEffect(isAdmin) {
        println("SE REINICIO POR ISADMIN")
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
        println("SE REINICIO POR ROOMID")
        if (isInRoom) return@LaunchedEffect
        println("Connecting to socket for room $roomId")
        socket.connect()
        realTimeClient.listenForEvents(roomId) { event ->
            when {
                event.message.startsWith("PLAYER_JOINED: ") -> {
                    println("El event.message es ${event.message}")
                    val jsonString = event.message.removePrefix("PLAYER_JOINED: ")
                    val joinResponse = Gson().fromJson(jsonString, JoinResponse::class.java)
                    players.add(PlayerModel(id = joinResponse.playerId, imageId = joinResponse.imageId.toString()))
                    isLoading = false
                    isInRoom = true
                }

                event.message.startsWith("PIN_PROVIDED: ") -> {
                    val jsonString = event.message.removePrefix("PIN_PROVIDED: ")
                    println("PIN_PROVIDED: $jsonString")
                    val game = Gson().fromJson(jsonString, RoomResponse::class.java)
                    println("Game: $game")
                    pinProvided = true
                    isLoading = false
                    pinRoom = game.pin.toString()
                    isInRoom = true
                }

                event.message.startsWith("playerJoined: ") -> {
                    val jsonString = event.message.removePrefix("playerJoined: ").trim()
                    val player = Gson().fromJson(jsonString, NewUserResponse::class.java)
                    players.add(PlayerModel(id = player.playerName, imageId = player.playerId))
                }
            }

            }
        if (isAdmin && !isInRoom) {
            val dataJson = JSONObject(
                mapOf(
                    "roomId" to roomId,
                    "token" to token,
                    "isNew" to true
                )
            ).toString()
            println("Se va a solicitar el pin con $dataJson")
            socket.emit("REQUEST_PIN", dataJson)
        }
        else if(!isInRoom){
            val dataJson = JSONObject(
                mapOf(
                    "roomId" to pin,
                    "token" to token,
                    "isNew" to false,
                    "userId" to currentUser?.username,
                    "isAnonymous" to (currentUser?.isAnonymous == true)
                )
            ).toString()
            println("Se va a solicitar join con $dataJson")
            socket.emit("JOIN_ROOM", dataJson)
        }
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
                    text = "PIN: ${pinRoom.substring(0,3)} ${pinRoom.substring(3,pinRoom.length)}",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            if (isAdmin) {
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp // Ancho de la pantalla en dp
                val screenHeight = configuration.screenHeightDp.dp
                // Define el tamaño del QR en función del ancho de la pantalla
                val qrSize = if (screenWidth < 600.dp) {
                    128.dp // Tamaño para pantallas pequeñas
                } else if (screenWidth < 900.dp) {
                    256.dp // Tamaño para pantallas medianas
                } else {
                    356.dp // Tamaño para pantallas grandes
                }

                // Generar y mostrar el código QR
                val qrBitmap = generateQRCode(pinRoom)
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.size(qrSize) // Ajusta el tamaño según sea necesario
                    )
                }
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
                    // En la sección de jugadores
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        players.forEach { player ->
                            PlayerItem(player = player, context = context)
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
                    // enabled = players.isNotEmpty(),
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

