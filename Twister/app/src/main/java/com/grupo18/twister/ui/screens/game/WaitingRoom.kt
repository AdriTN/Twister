package com.grupo18.twister.ui.screens.game

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.grupo18.twister.R
import com.grupo18.twister.core.network.socket.LobbySocketClient
import com.grupo18.twister.core.network.socket.SocketManager
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.models.network.*
import com.grupo18.twister.models.network.domain.PlayerModel
import com.grupo18.twister.models.network.domain.UploadSocketGameRequest
import com.grupo18.twister.models.network.events.StartResponse
import com.grupo18.twister.models.network.responses.GameResponse
import com.grupo18.twister.models.network.responses.JoinPinResponse
import com.grupo18.twister.models.network.responses.RoomResponse
import com.grupo18.twister.models.network.responses.TwistQuestionsResponse
import com.grupo18.twister.ui.components.SelectablePlayerImage
import kotlinx.coroutines.delay
import org.json.JSONObject

// =====================================================
//  FUNCIONES AUXILIALES (QRCode, displayImage, etc.)
// =====================================================
fun generateQRCode(text: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, size, size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y,
                    if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                )
            }
        }
        bmp
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}

fun displayPlayerImage(imageId: String, context: Context): Bitmap? {
    if (imageId.isEmpty()) return null
    val imagePath = "player_avatars/ico ($imageId).png"
    val assetManager = context.assets
    return try {
        val inputStream = assetManager.open(imagePath)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// =====================================================
//                UI DE UN JUGADOR
// =====================================================
@Composable
fun PlayerItem(player: PlayerModel, context: Context) {
    val bitmap = remember(player.imageIndex) { displayPlayerImage(player.imageIndex, context) }

    // Animación de “shake”
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shakeAnim.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 300
                0f at 0 with FastOutSlowInEasing
                -10f at 50 with FastOutSlowInEasing
                10f at 100 with FastOutSlowInEasing
                0f at 150 with FastOutSlowInEasing
            }
        )
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer { translationX = shakeAnim.value }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFFDADADA), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Avatar de ${player.id}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }
            Box(
                modifier = Modifier
                    .width(85.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = player.id,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// =====================================================
//              PANTALLA “WAITING ROOM”
// =====================================================
@Composable
fun WaitingRoom(
    navController: NavController,
    token: String,
    userIsAdmin: Boolean,
    pin: String,
    twist: TwistModel?,
    onStartGame: (roomId: String, playerName: String, questions: SnapshotStateList<QuestionModel>) -> Unit
) {
    // Estado de lista de jugadores
    var players = remember { mutableStateListOf<PlayerModel>() }
    val questions: SnapshotStateList<QuestionModel> = remember { mutableStateListOf() }

    // Socket Manager y LobbySocketClient
    val lobbySocketClient = remember {
        // Verificación #1: Confirmar socket está conectado
        println("Socket connected? ${SocketManager.getSocket().connected()}")
        LobbySocketClient(SocketManager.getSocket())
    }

    // Estados de UI
    var isLoading by remember { mutableStateOf(true) }
    var pinProvided by remember { mutableStateOf(false) }
    var pinRoom by remember { mutableStateOf("") }
    var isInRoom by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var roomId by remember { mutableStateOf("0000") }
    var isAdmin by remember { mutableStateOf(userIsAdmin) }
    var playerName by remember { mutableStateOf("") }
    var selectedImageId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val myApp = context.applicationContext as MyApp
    val currentUser by myApp.currentUser.collectAsState()
    val jockeyFontFamily = FontFamily(Font(R.font.jockeyone))

    var uploadedTwist by remember { mutableStateOf(false) }

    // Ajustar orientación si es Admin
    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            println("Estableciendo orientation = LANDSCAPE, userIsAdmin=$isAdmin")
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            println("Estableciendo orientation = UNSPECIFIED, userIsAdmin=$isAdmin")
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Animación de puntos suspensivos “...” en la pantalla de Loading
    val dots = listOf("", ".", "..", "...")
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDots")
    val currentIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dots.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "LoadingDotsValue"
    )
    val currentDots = dots[(currentIndex.toInt()) % dots.size]

    // Suscripción a eventos de Lobby
    LaunchedEffect(roomId, isInRoom) {
        if (isInRoom) return@LaunchedEffect

        println("=> LaunchedEffect(roomId=$roomId, isInRoom=$isInRoom) - Iniciando escucha de eventos")

        // Escuchar los eventos del lobby
        lobbySocketClient.listenForLobbyEvents { event ->
            println("Evento recibido => ${event.type}: ${event.message}")
            when {
                // Jugador se une
                event.message.startsWith("PLAYER_JOINED: ") -> {
                    println("Evento => PLAYER_JOINED")
                    val jsonString = event.message.removePrefix("PLAYER_JOINED: ")
                    val newPlayer = Gson().fromJson(jsonString, JoinPinResponse::class.java)
                    println("joinPinResponse: $newPlayer")

                    val response = Gson().fromJson(newPlayer.twistQuestions, TwistQuestionsResponse::class.java)
                    val decodedQuestions: List<QuestionModel> = response.twistQuestions
                    questions.addAll(decodedQuestions)

                    players.add(
                        PlayerModel(
                            id = newPlayer.playerName,
                            imageIndex = newPlayer.imageIndex
                        )
                    )
                    isLoading = false
                }

                // PIN recibido
                event.message.startsWith("PIN_PROVIDED: ") -> {
                    println("Evento => PIN_PROVIDED")
                    val jsonString = event.message.removePrefix("PIN_PROVIDED: ")
                    val game = Gson().fromJson(jsonString, RoomResponse::class.java)
                    pinProvided = true
                    isLoading = false
                    pinRoom = (game.pin ?: "").toString()
                    lobbySocketClient.updateRoomId(pinRoom)
                    println("PIN_PROVIDED => roomId = $pinRoom")
                }

                // PIN_START (Inicio)
                event.message.startsWith("PIN_STARTED_PROVIDED: ") -> {
                    println("Evento => PIN_STARTED_PROVIDED")
                    val jsonString = event.message.removePrefix("PIN_STARTED_PROVIDED: ")
                    val joinPinResponse = Gson().fromJson(jsonString, GameResponse::class.java)
                    pinProvided = true
                    isLoading = false
                    pinRoom = joinPinResponse.id
                    lobbySocketClient.updateRoomId(pinRoom)
                    players = mutableStateListOf(*joinPinResponse.players.toTypedArray())
                    showNameDialog = true
                }

                // Nuevo jugador
                event.message.startsWith("newPlayer: ") -> {
                    println("Evento => newPlayer")
                    val jsonString = event.message.removePrefix("newPlayer: ").trim()
                    val newPlayer = Gson().fromJson(jsonString, PlayerModel::class.java)
                    val existingPlayerIndex = players.indexOfFirst { it.socketId == newPlayer.socketId }
                    if (existingPlayerIndex != -1) {
                        players[existingPlayerIndex] = newPlayer
                    } else {
                        players.add(newPlayer)
                    }
                }

                // Jugador se fue
                event.message.startsWith("playerLeft: ") -> {
                    println("Evento => playerLeft")
                    val jsonString = event.message.removePrefix("playerLeft: ").trim()
                    val playerToRemove = Gson().fromJson(jsonString, PlayerModel::class.java)
                    val existingPlayerIndex = players.indexOfFirst { it.socketId == playerToRemove.socketId }
                    if (existingPlayerIndex != -1) {
                        players.removeAt(existingPlayerIndex)
                    }
                }

                // Desconexión
                event.message.startsWith("Disconnected") -> {
                    println("Evento => Disconnected")
                    isInRoom = false
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }

                // Juego iniciando
                event.message.startsWith("GAME_IS_STARTING") -> {
                    println("Evento => GAME_IS_STARTING")
                    val jsonString = event.message.removePrefix("GAME_IS_STARTING: ").trim()
                    val pinReq = Gson().fromJson(jsonString, StartResponse::class.java)
                    if (pinRoom == pinReq.pinRoom) {
                        onStartGame(pinRoom, playerName, questions)
                    }
                }

                // Jugadores que se fueron
                event.message.startsWith("PLAYERS_LEFT_LIST") -> {
                    println("Evento => PLAYERS_LEFT_LIST")
                    val jsonString = event.message.removePrefix("PLAYERS_LEFT_LIST: ").trim()
                    println("PLAYERS_LEFT_LIST: $jsonString")
                    try {
                        val playersLeftList = Gson().fromJson(jsonString, PlayersLeftList::class.java)
                        if (playersLeftList.roomId == pinRoom) {
                            val currentSockets = players.map { it.socketId }.toSet()
                            val newSockets = playersLeftList.players.map { it.socketId }.toSet()
                            if (currentSockets != newSockets) {
                                players.clear()
                                players.addAll(playersLeftList.players)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error al deserializar: $e")
                    }
                }
            }
        }

        // Solicitar PIN o unirse a la sala
        if (isAdmin && !isInRoom) {
            val dataJson = JSONObject(
                mapOf(
                    "roomId" to roomId,
                    "token" to token,
                    "isNew" to true
                )
            ).toString()
            println("REQUEST_PIN => $dataJson")
            SocketManager.getSocket().emit("REQUEST_PIN", dataJson)
            isInRoom = true
        } else if (!isAdmin) {
            val dataJson = JSONObject(
                mapOf(
                    "roomId" to pin,
                    "token" to token,
                    "isNew" to false
                )
            ).toString()
            println("JOIN_ROOM => $dataJson")
            SocketManager.getSocket().emit("REQUEST_PIN", dataJson)
            isInRoom = true
        }

        // Verificación #2: Forzar un timeout si no llega evento => set isLoading = false
        // (Únicamente para debug; puedes quitarlo si no lo deseas)
        delay(10_000)  // 10s
        if (isLoading) {
            println("Timeout de 10s: sigue isLoading => forzamos isLoading = false")
            isLoading = false
        }
    }

    // Si eres admin y tienes un twist => sube la info cuando recibes PIN_PROVIDED
    LaunchedEffect(pinProvided, pinRoom) {
        if (isAdmin && pinProvided && !uploadedTwist && twist != null) {
            println("Enviando uploadGame para ${twist.id} con room $pinRoom")
            lobbySocketClient.uploadGame(UploadSocketGameRequest(twist.id, pinRoom))
            uploadedTwist = true
        }
    }

    // DISEÑO DE UI
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
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp, bottom = 9.dp),
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Loading$currentDots",
                        color = Color.DarkGray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Diálogo para seleccionar nombre e imagen si eres admin
            if (showNameDialog) {
                SelectablePlayerImage(
                    context = context,
                    onSelectionChange = { newSelectedImageId, newPlayerName ->
                        selectedImageId = newSelectedImageId
                        playerName = newPlayerName
                    }
                )
                // Enviamos JOIN_ROOM si se ha elegido un nombre
                if (playerName.isNotBlank()) {
                    val dataJson = JSONObject(
                        mapOf(
                            "roomId" to pinRoom,
                            "token" to token,
                            "isAnonymous" to (currentUser?.isAnonymous == true),
                            "userName" to playerName,
                            "imageIndex" to selectedImageId
                        )
                    ).toString()
                    println("JOIN_ROOM => $dataJson")
                    SocketManager.getSocket().emit("JOIN_ROOM", dataJson)
                    showNameDialog = false
                }
            }

            if (isAdmin) {
                // ADMIN UI
                AdminWaitingRoomContent(
                    pinProvided = pinProvided,
                    pinRoom = pinRoom,
                    players = players,
                    jockeyFontFamily = FontFamily(Font(R.font.jockeyone)),
                ) {
                    // Botón "Start Game"
                    println("Start Game => Emitting startGame con pinRoom=$pinRoom")
                    lobbySocketClient.startGame(pinRoom)
                    onStartGame(pinRoom, playerName, questions)
                }
            } else if (pinProvided) {
                // UI Jugador
                PlayerWaitingRoomContent(
                    pinRoom = pinRoom,
                    jockeyFontFamily = FontFamily(Font(R.font.jockeyone))
                )
            }
        }
    }
}

@Composable
fun AdminWaitingRoomContent(
    pinProvided: Boolean,
    pinRoom: String,
    players: List<PlayerModel>,
    jockeyFontFamily: FontFamily,
    onStartGameClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Waiting for players...",
            color = Color.Gray,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = 20.dp)
        )
    }

    if (pinProvided && pinRoom.isNotBlank()) {
        Text(
            text = "PIN: ${pinRoom.substring(0, 3)} ${pinRoom.substring(3)}",
            color = Color.Gray,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }

    // Generar y mostrar QR
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val qrSize = when {
        screenWidth < 600.dp -> 128.dp
        screenWidth < 900.dp -> 256.dp
        else -> 356.dp
    }

    val qrBitmap = generateQRCode(pinRoom)
    qrBitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Código QR",
            modifier = Modifier.size(qrSize)
        )
    }

    // Lista de jugadores conectados
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
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                players.forEach { player ->
                    PlayerItem(player = player, context = LocalContext.current)
                }
            }
        }
    }

    // Botón para iniciar partida
    Button(
        onClick = onStartGameClick,
        enabled = players.isNotEmpty(),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(0.4f)
    ) {
        Text(
            text = "Start Game",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlayerWaitingRoomContent(
    pinRoom: String,
    jockeyFontFamily: FontFamily
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "YOU ARE IN THE ROOM",
                color = Color.Gray,
                fontSize = 24.sp,
                fontFamily = jockeyFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = pinRoom,
                color = Color.DarkGray,
                fontSize = 29.sp,
                fontFamily = jockeyFontFamily,
                fontWeight = FontWeight.Thin,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(42.dp))

            // Logo rotatorio
            val infiniteTransition = rememberInfiniteTransition(label = "PlayerWaitingRotate")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "PlayerRotateValue"
            )
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "PlayerScaleValue"
            )
            Image(
                painter = painterResource(id = R.drawable.ico),
                contentDescription = "Loading",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(
                        rotationZ = rotation,
                        scaleX = scale,
                        scaleY = scale
                    )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Wait for the admin to start the game",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
