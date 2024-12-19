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
import android.content.pm.ActivityInfo
import androidx.compose.ui.platform.LocalContext
import com.grupo18.twister.core.api.RealTimeClient
import org.json.JSONObject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.grupo18.twister.core.components.SelectablePlayerImage
import com.grupo18.twister.core.models.GameResponse
import com.grupo18.twister.core.models.JoinPinResponse
import com.grupo18.twister.core.models.PlayerModel
import com.grupo18.twister.core.models.RoomResponse
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.R
import com.grupo18.twister.core.models.PlayersLeftList
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.StartResponse
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.TwistQuestionsResponse
import com.grupo18.twister.core.models.UploadSocketGameRequest
import kotlinx.coroutines.delay
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
    val imagePath = "player_avatars/ico ($imageId).png" // Construir la ruta de la imagen en assets
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
    val bitmap = remember(player.imageIndex) { displayPlayerImage(player.imageIndex, context) }
    println("Se va a cargar la imagen del jugador ${player.id}")

    // Animación de shake
    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        shakeAnim.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 300
                // Definir la posición de inicio y los movimientos de shake
                0f at 0 with FastOutSlowInEasing
                -10f at 50 with FastOutSlowInEasing
                10f at 100 with FastOutSlowInEasing
                0f at 150 with FastOutSlowInEasing
            }
        )
    }

    // Aplicar la animación en el modifier
    Box(
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer {
                translationX = shakeAnim.value
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFFDADADA), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Avatar de ${player.id}",
                    modifier = Modifier
                        .size(64.dp) // Tamaño fijo para todas las imágenes
                        .clip(CircleShape)
                        .background(Color.LightGray) // Fondo en caso de que la imagen no cargue
                )
            }

            // Mostrar el nombre dentro de un contenedor de ancho fijo
            Box(
                modifier = Modifier
                    .width(85.dp)
                    .align(alignment = Alignment.CenterHorizontally)
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

@Composable
fun WaitingRoom(
    navController: NavController,
    token: String,
    userIsAdmin: Boolean,
    pin: String,
    twist: TwistModel?,
    onStartGame: (roomId: String, playerName: String, questions: SnapshotStateList<QuestionModel>) -> Unit
) {
    var players = remember { mutableStateListOf<PlayerModel>() }
    val socket = ApiClient.getSocket()
    val realTimeClient = remember { RealTimeClient(socket) }
    val questions: SnapshotStateList<QuestionModel> = remember { mutableStateListOf() }
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
    var uploadedTwist = false

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            while (true) {
                delay(1000) // Espera 5 segundos entre cada verificación
                if (pinRoom == "0000" || pinRoom.isEmpty()){
                    continue
                }
                if (!uploadedTwist){
                    twist?.let { realTimeClient.uploadGame(UploadSocketGameRequest(it.id, pinRoom)) }
                    uploadedTwist = true
                }
                // Lógica para verificar si alguien ha salido
                val checkJson = JSONObject(
                    mapOf(
                        "roomId" to pinRoom,
                        "token" to token
                    )
                ).toString()
                println("Se va a enviar CHECK_PLAYERS_LEFT con $checkJson")
                socket.emit("CHECK_PLAYERS_LEFT", checkJson) // Enviar evento al servidor para verificar
            }

        } else {
            (context as? androidx.activity.ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Animación para los puntos
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

    // Conectar el socket y escuchar eventos cuando la pantalla carga
    LaunchedEffect(roomId) {
        if (isInRoom) return@LaunchedEffect
        println("Connecting to socket for room $roomId")
        socket.connect()
        realTimeClient.listenForEvents(roomId) { event ->
            when {
                event.message.startsWith("PLAYER_JOINED: ") -> {
                    println("El event.message es ${event.message}")
                    val jsonString = event.message.removePrefix("PLAYER_JOINED: ")
                    val newPlayer = Gson().fromJson(jsonString, JoinPinResponse::class.java)
                    println("El newPlayer es $newPlayer")
                    println("El question list es $newPlayer.twistQuestions")
                    // Suponiendo que jsonString es el JSON completo que has recibido
                    val response = Gson().fromJson(newPlayer.twistQuestions, TwistQuestionsResponse::class.java)
                    val decodedQuestions: List<QuestionModel> = response.twistQuestions
                    questions.addAll(decodedQuestions)
                    println("El decodedQuestions es $questions")

                    players.add(
                        PlayerModel(
                            id = newPlayer.playerName,
                            imageIndex = newPlayer.imageIndex
                        )
                    )
                    isLoading = false
                }

                event.message.startsWith("PIN_PROVIDED: ") -> {
                    val jsonString = event.message.removePrefix("PIN_PROVIDED: ")
                    val game = Gson().fromJson(jsonString, RoomResponse::class.java)
                    println("Game: $game")
                    pinProvided = true
                    isLoading = false
                    pinRoom = game.pin.toString()
                    realTimeClient.updateRoomId(pinRoom)
                }

                event.message.startsWith("PIN_STARTED_PROVIDED: ") -> {
                    val jsonString = event.message.removePrefix("PIN_STARTED_PROVIDED: ")
                    println("PIN_STARTED_PROVIDED: $jsonString")
                    val joinPinResponse = Gson().fromJson(jsonString, GameResponse::class.java)
                    println("joinPinResponse: $joinPinResponse")
                    pinProvided = true
                    isLoading = false
                    pinRoom = joinPinResponse.id
                    realTimeClient.updateRoomId(pinRoom)
                    players = mutableStateListOf(*joinPinResponse.players.toTypedArray())
                    showNameDialog = true
                }

                event.message.startsWith("newPlayer: ") -> {
                    val jsonString = event.message.removePrefix("newPlayer: ").trim()
                    println("New player joined: $jsonString")
                    val newPlayer = Gson().fromJson(jsonString, PlayerModel::class.java)

                    // Busca si el jugador ya existe en la lista
                    val existingPlayerIndex =
                        players.indexOfFirst { it.socketId == newPlayer.socketId }

                    if (existingPlayerIndex != -1) {
                        // Si el jugador ya existe, reemplázalo
                        players[existingPlayerIndex] = newPlayer
                        println("Player updated: ${newPlayer.socketId}")
                    } else {
                        // Si el jugador no existe, agrégalo
                        players.add(newPlayer)
                        println("New player added: ${newPlayer.socketId}")
                    }
                }

                event.message.startsWith("playerLeft: ") -> {
                    val jsonString = event.message.removePrefix("playerLeft: ").trim()
                    println("Player left: $jsonString")
                    val playerToRemove = Gson().fromJson(jsonString, PlayerModel::class.java)

                    // Busca el jugador en la lista
                    val existingPlayerIndex = players.indexOfFirst { it.socketId == playerToRemove.socketId }

                    if (existingPlayerIndex != -1) {
                        // Si el jugador existe, elimínalo
                        players.removeAt(existingPlayerIndex)
                        println("Player removed: ${playerToRemove.socketId}")
                    } else {
                        // Si el jugador no existe en la lista
                        println("Player not found: ${playerToRemove.socketId}")
                    }
                }

                event.message.startsWith("Disconnected") -> {
                    println("Se ha cerrado la sala")
                    isInRoom = false
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                event.message.startsWith("GAME_IS_STARTING") -> {
                    val jsonString = event.message.removePrefix("GAME_IS_STARTING: ").trim()
                    val pinReq = Gson().fromJson(jsonString, StartResponse::class.java)
                    println("Voy a la siguiente pantalla")
                    println("pinRoom: $pinRoom - pinReq: $pinReq")
                    if (pinRoom == pinReq.pinRoom) {
                        onStartGame(pinRoom, playerName, questions) // Ir a la siguiente pantalla
                    }
                }

                event.message.startsWith("PLAYERS_LEFT_LIST") -> {
                    val jsonString = event.message.removePrefix("PLAYERS_LEFT_LIST: ").trim()
                    println("PLAYERS_LEFT_LIST: $jsonString")

                    try {
                        val playersLeftList = Gson().fromJson(jsonString, PlayersLeftList::class.java)
                        if (playersLeftList.roomId == pinRoom) {
                            val currentSockets = players.map { it.socketId }.toSet()
                            val newSockets = playersLeftList.players.map { it.socketId }.toSet()
                            println("Current sockets: $currentSockets - New sockets: $newSockets")
                            if (currentSockets != newSockets) {
                                // Actualiza la lista de jugadores si los sockets son diferentes
                                players.clear()
                                players.addAll(playersLeftList.players)
                                println("Updated players list: $players")
                            } else {
                                println("Room ID does not match: ${playersLeftList.roomId}")
                            }
                        }
                    } catch (e: Exception) {
                        println("Error al deserializar el JSON: $e")
                    }
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
            isInRoom = true
        } else if (!isAdmin) {
            val dataJson = JSONObject(
                mapOf(
                    "roomId" to pin,
                    "token" to token,
                    "isNew" to false
                )
            ).toString()
            println("Se va a enviar JOIN_ROOM con $dataJson")
            socket.emit("REQUEST_PIN", dataJson)
            isInRoom = true
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
                    CircularProgressIndicator(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 9.dp
                        ), color = Color.DarkGray
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
            if (showNameDialog) {
                SelectablePlayerImage(
                    context = context,
                    onSelectionChange = { newSelectedImageId, newPlayerName ->
                        selectedImageId =
                            newSelectedImageId // Actualizar el ID de la imagen seleccionada
                        playerName = newPlayerName // Actualizar el nombre del jugador
                    }
                )
                if (playerName.isNotBlank()) {
                    val dataJson = JSONObject(
                        mapOf(
                            "roomId" to pinRoom,
                            "token" to token,
                            "isAnonymous" to (currentUser?.isAnonymous == true),
                            "userName" to playerName,
                            "imageIndex" to selectedImageId // Enviar el ID de la imagen seleccionada
                        )
                    ).toString()
                    println("Y el socket emit join room con $dataJson")
                    socket.emit("JOIN_ROOM", dataJson)
                    showNameDialog = false
                }
            }

            if (isAdmin) {
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
                        modifier = Modifier.offset(y = 20.dp)
                    )
                }

                // Mostrar el PIN si ha sido proporcionado
                if (pinProvided && pinRoom.isNotBlank()) {
                    Text(
                        text = "PIN: ${pinRoom.substring(0, 3)} ${
                            pinRoom.substring(
                                3,
                                pinRoom.length
                            )
                        }",
                        color = Color.Gray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp // Ancho de la pantalla en dp
                // Define el tamaño del QR en función del ancho de la pantalla
                val qrSize = when {
                    screenWidth < 600.dp -> 128.dp // Tamaño para pantallas pequeñas
                    screenWidth < 900.dp -> 256.dp // Tamaño para pantallas medianas
                    else -> 356.dp // Tamaño para pantallas grandes
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

                // Lista estilizada de jugadores conectados
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()) // Permite el desplazamiento horizontal
                        ) {
                            players.forEach { player ->
                                PlayerItem(player = player, context = context)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        realTimeClient.startGame(pinRoom) // Notificar a los jugadores
                        onStartGame(pinRoom, playerName, questions) // Ir a la siguiente pantalla
                    },
                    //enabled = players.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.4f)
                ) {
                    Text(
                        text = "Start game",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                } else if (pinProvided) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
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

                        Spacer(modifier = Modifier.height(42.dp)) // Espacio entre los textos y el logo

                        // Logo rotatorio
                        val infiniteTransition = rememberInfiniteTransition(label = "")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = ""
                        )
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = ""
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ico),
                            contentDescription = "Loading",
                            modifier = Modifier
                                .size(100.dp) // Ajusta el tamaño del logo según sea necesario
                                .graphicsLayer(
                                    rotationZ = rotation,
                                    scaleX = scale,
                                    scaleY = scale
                                )
                        )

                        // Espaciador para empujar el mensaje al final
                        Spacer(modifier = Modifier.weight(1f)) // Empuja el siguiente contenido hacia abajo

                        // Mensaje al final
                        Text(
                            text = "Wait for the admin to start the game",
                            fontSize = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 16.dp) // Espacio adicional en la parte inferior
                        )
                    }
                }
            }
        }
    }
}
