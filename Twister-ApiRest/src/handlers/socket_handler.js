import { getUserWithTokenSocket } from "../services/authService.js";
import {
  createGame,
  getGameById,
  deleteUserById,
  joinGameById,
  getActiveGames,
  saveTwistInRoom,
  getTwistQuestions,
  initRoomDB,
  updateRoomDB,
  getRoomDB,
  deleteGameFromRedis,
  getScores,
} from "../models/gameModel.js";
import { get } from "../utils/database.js";

// Almacena el ID del juego y el usuario al unirse
let currentGameId = null;
let currentUserId = null;
let currentUserName = null;
let isAnonymous = false;
export const socketHandlers = (io, socket) => {
  console.log("Nuevo cliente conectado:", socket.id);
  // Evento: Unirse a un juego
  socket.on("JOIN_ROOM", async (data) => {
    console.log("Unirse a la sala solicitado:", data);
    data = JSON.parse(data);
    // Desestructurar los datos recibidos
    const { roomId, token, isAnonymous, userName, imageIndex } = data;

    const userId = await getUserWithTokenSocket(token); // Obtener el ID de usuario desde el token
    currentUserId = userId; // Actualizar el ID del usuario

    console.log("La sala va a cambiar de ", currentGameId, " a ", roomId);
    currentGameId = roomId; // ID de la sala a la que se une
    currentUserId = socket.id; // ID del socket del usuario
    // Asignar el nombre del usuario o usar "Jugador Anónimo" si es anónimo
    currentUserName = userName || "Jugador Anónimo";

    console.log(`Unirse a la sala: ${currentGameId} - ${currentUserName}`);
    const newgame = await joinGameById(
      currentGameId,
      currentUserId,
      socket.id,
      imageIndex
    );
    // Unirse a la sala
    socket.join(currentGameId);
    console.log("Se va a emitir el evento playerJoined");
    // Emitir evento de que un jugador se ha unido a todos los usuarios en la sala
    io.to(currentGameId).emit("playerJoined", {
      socketId: currentUserId,
      id: currentUserName,
      imageIndex,
    });

    const twistQuestions = await getTwistQuestions(roomId);
    initRoomDB(roomId, twistQuestions);

    // Emitir evento de que un jugador se ha unido al propio socket con información completa
    console.log("Player joined susecsfully!");
    socket.emit("PLAYER_JOINED", {
      currentGameId,
      playerId: currentUserId,
      game: newgame,
      playerName: currentUserName,
      imageIndex: imageIndex,
      twistQuestions: JSON.stringify(twistQuestions),
    });
  });

  // Evento: Solicitar PIN
  socket.on("REQUEST_PIN", async (data) => {
    try {
      console.log("Solicitud de PIN recibida:", data);
      data = JSON.parse(data);
      if (!data || !data.token) {
        socket.emit("ERROR", { message: "Datos de solicitud incorrectos" });
        return;
      }
      const userId = await getUserWithTokenSocket(data.token); // Obtener el ID de usuario desde el token
      if (userId != null) {
        currentUserId = userId;
      }

      if (data.isNew) {
        const game = await createGame(userId, socket);
        socket.join(game.id);
        console.log("Game id es", game.id);
        socket.emit("PIN_PROVIDED", { pin: game.id, game });
      } else {
        console.log("Solicitud de juego existente:", data.roomId);
        var game = await getGameById(data.roomId, userId, socket.id);
        if (!game) {
          socket.emit("ERROR", { message: "Juego no encontrado" });
          return;
        }
        currentGameId = game.id;
        console.log("Game id es PIN_STARTED_PROVIDED", game);
        socket.emit("PIN_STARTED_PROVIDED", game);
      }
    } catch (err) {
      console.error("Error procesando la solicitud:", err);
      socket.emit("ERROR", { message: "Error procesando solicitud de PIN" });
    }
  });

  // Evento: Iniciar el juego
  socket.on("startGame", (pinRoom) => {
    console.log(`Juego iniciado en la sala: ${pinRoom}`);
    const activeGames = getActiveGames();
    console.log("Sigo vivo con activeGames", activeGames);
    for (const pin in activeGames) {
      console.log("Se va a comparar ", pin, " con ", pinRoom);
      if (pin === pinRoom) {
        const game = activeGames[pin];
        game.players.forEach((player) => {
          io.to(player.socketId).emit("GAME_STARTED", { pinRoom: pin });
          console.log(
            `Juego iniciado en la sala: ${pin} enviado a ${player.socketId}`
          );
        });
      }
    }
    return activeGames;
  });

  // Evento: Iniciar el juego
  socket.on("getGame", (data) => {
    data = JSON.parse(data);
    const result = saveTwistInRoom(data.twistId, data.roomId);
    if (!result) {
      socket.emit("ERROR", { message: "Ya existe el twist" });
    }
    io.to(data.roomId).emit("gameStarted", { roomId: data.roomId });
  });

  // Evento: Comprobar jugadores restantes
  // Evento: Comprobar jugadores restantes (solo para el administrador)
  socket.on("CHECK_PLAYERS_LEFT", async (data) => {
    try {
      // Parsear los datos recibidos
      data = JSON.parse(data);
      console.log(`Comprobando jugadores en la sala: ${data.roomId}`);

      // Obtener los juegos activos
      const activeGames = getActiveGames();

      // Verificar si el juego existe en activeGames
      const game = activeGames[data.roomId];

      if (game) {
        // Emitir la lista de jugadores solo al administrador
        socket.emit("PLAYERS_LEFT_LIST", {
          roomId: data.roomId,
          players: game.players || [], // Asegurar que players siempre sea un array
        });
        console.log(`Jugadores en la sala ${data.roomId}:`, game.players);
      } else {
        console.log(`No se encontró ningún juego con roomId: ${data.roomId}`);
        socket.emit("PLAYERS_LEFT_LIST", {
          roomId: data.roomId,
          players: [], // Sala no encontrada, devolver lista vacía
        });
      }
    } catch (error) {
      console.error("Error al procesar CHECK_PLAYERS_LEFT:", error);
    }
  });

  // Evento: Respuestas de los sockets
  socket.on("sendAnswer", async (data) => {
    data = JSON.parse(data);
    const { answer, roomId, playerName, questionId, time } = data;
    console.log(
      "Respuesta recibida:",
      answer,
      "en el tiempo de ",
      time,
      "de",
      playerName,
      "en",
      roomId + " con questionId: " + questionId
    );
    let score = await updateRoomDB(roomId, answer, playerName, questionId, time);
    socket.emit("ANSWER_SENT", { score: score });
  });

  // Evento: Respuestas de los sockets
  socket.on("getAnswers", async (data) => {
    data = JSON.parse(data);
    const { roomId, questionId } = data;

    console.log("Respuestas solicitadas para la sala", roomId, "y la pregunta", questionId);

    // Obtener datos de la sala
    var room = await getRoomDB(roomId);

    if (room == null) {
      console.error("La sala o las preguntas no se encontraron");
      socket.emit("ANSWERS", { error: "Room or questions not found" });
      return;
    }
    room = JSON.parse(room);

    // Filtrar respuestas por questionId
    const question = room.twistQuestions.find((q) => q.id === questionId);
    if (!question) {
      console.log(
        "Pregunta con ID ${questionId} no encontrada en la sala ${roomId}"
      );
      socket.emit("ANSWERS", { error: "Question not found" });
      return;
    }

    console.log("Respuestas recibidas:", question.answers);
    socket.emit("ANSWERS", question.answers);
  });

  // Evento: Respuestas de los sockets
  socket.on("nextQuestion", async (data) => {
    console.log("Next question event received");
    data = JSON.parse(data);
    const { roomId } = data;

    // Obtener datos de la sala
    var room = await getRoomDB(roomId);

    if (room == null) {
      console.error("La sala o las preguntas no se encontraron");
      socket.emit("ANSWERS", { error: "Room or questions not found" });
      return;
    }

    const activeGames = getActiveGames();
    console.log("Active games", activeGames);
    for (const pin in activeGames) {
      if (pin === roomId) {
        const game = activeGames[pin];
        game.players.forEach((player) => {
          io.to(player.socketId).emit("NEXT_QUESTION", { roomId: pin });
        });
      }
    }
    return activeGames;
  });

  // Evento: Respuestas de los sockets
  socket.on("getCorrectAnswer", async (data) => {
    data = JSON.parse(data);
    const { roomId, questionId, playerName } = data;

    console.log("Se ha pedido una answer");

    // Obtener datos de la sala
    var room = await getRoomDB(roomId);

    if (room == null) {
      console.error("La sala o las preguntas no se encontraron");
      socket.emit("ANSWERS", { error: "Room or questions not found" });
      return;
    }
    room = JSON.parse(room);

    console.log("Este es le Room", room, "con questionId", questionId);
    const question = room.twistQuestions.find((q) => q.id === questionId);
    if (!question) {
      console.log(
        `Pregunta con ID ${questionId} no encontrada en la sala ${roomId}`
      );
      socket.emit("ANSWERS", { error: "Question not found" });
      return;
    }

    // Filtra la respuesta correcta
    const correctAnswer = question.answers.find((a) => a.isCorrect === true);
    if (!correctAnswer) {
      console.log(
        `Respuesta correcta no encontrada en la pregunta ${questionId}`
      );
      socket.emit("ANSWERS", { error: "Correct answer not found" });
      return;
    }

    console.log("Respuestas recibidas:", question.answers);

    const score = await getScores(roomId, playerName);
    // Sumar valores relevantes (ajusta esto según tus necesidades)
    console.log("Scores ", score);

    //console.log(`La suma de los valores de las respuestas es: ${sum}`);
    socket.emit("CORRECT_ANSWER", { correctAnswer, score });
  });

  // Evento: Obtener los 3 mejores puntajes
  socket.on("getTopScores", async (data) => {
    try {
      data = JSON.parse(data);
      const { roomId } = data;

      console.log(`Solicitando los 3 mejores puntajes de la sala ${roomId}`);

      // Obtener los puntajes de la sala desde la base de datos
      const scores = await getScores(roomId);
      if (!scores || scores.length === 0) {
        console.log(`No se encontraron puntajes para la sala ${roomId}`);
        socket.emit("TOP_SCORES", { error: "No scores found" });
        return;
      }
      
      // Convierte el objeto a un array de objetos
      const scoresArray = Object.entries(scores).map(([player, score]) => ({ player, score }));

      // Ordena el array de puntajes de mayor a menor
      const sortedScores = scoresArray.sort((a, b) => b.score - a.score);

      // Obtiene los 3 mejores puntajes (en este caso puede que haya menos de 3)
      const topScores = sortedScores.slice(0, 3);


      console.log(`Top 3 puntajes para la sala ${roomId}:`, topScores);

      // Emitir los resultados al cliente
      socket.emit("TOP_SCORES", { topScores });
    } catch (error) {
      console.error("Error al obtener los 3 mejores puntajes:", error);
      socket.emit("TOP_SCORES", { error: "Error retrieving top scores" });
    }
  });



  socket.on("gameOverEvent", async (data) => {
    data = JSON.parse(data);
    const { roomId } = data;
    console.log("Game over event received");
    //TODO ENVIAR A TODOS LOS JUGADORES el ranking final
    // Obtener datos de la sala
    var room = await getRoomDB(roomId);

    if (room == null) {
      console.error("La sala o las preguntas no se encontraron");
      socket.emit("GAME_OVER", { error: "Room or questions not found" });
      return;
    }

    const activeGames = getActiveGames();
    console.log("Active games", activeGames);
    for (const pin in activeGames) {
      if (pin === roomId) {
        const game = activeGames[pin];
        game.players.forEach((player) => {
          io.to(player.socketId).emit("GAME_OVER", { roomId: pin, winnerId: "1" });
        });
      }
    }
    deleteGameFromRedis(roomId)
    return activeGames;
  });

  // Evento: Desconexión del cliente
  socket.on("disconnect", async () => {
    console.log(
      "Cliente desconectado:",
      socket.id,
      "en la sala",
      currentGameId
    );

    const result = await deleteUserById(socket.id);

    // Verificamos si la conexión a Redis está abierta
    if (result === -1) {
      console.log(
        "Falló la eliminación del jugador o el juego ha sido eliminado."
      );
      return; // Salimos si hubo un fallo
    } else if (typeof result === "string") {
      console.log("El administrador ha abandonado la sala");
      socket.emit("roomDeleted", {
        message:
          "La sala ha sido eliminada porque solo quedaba el administrador.",
      });
    }

    // Notificar a otros jugadores en la sala
    if (currentGameId) {
      const isInRoom = socket.rooms.has(currentGameId);

      if (isInRoom) {
        const room = io.sockets.adapter.rooms[currentGameId];
        console.log("Sala actual:", room);

        // Verificar si hay sockets en la sala
        const socketsInRoom = room.sockets;

        if (socketsInRoom && Object.keys(socketsInRoom).length > 0) {
          console.log("Sockets en la sala:", Object.keys(socketsInRoom));

          // Emitir el evento de jugador que se ha ido
          io.to(currentGameId).emit("playerLeft", {
            playerId: socket.id,
            playerName: currentUserName,
          });

          // Si solo queda el administrador
          if (result === socket.id) {
            socket.emit("roomDeleted", {
              message: "Eres el único en la sala. La sala ha sido eliminada.",
            });
          }
        } else {
          console.log("No hay jugadores en la sala o la sala no existe.");
        }
      } else {
        console.log("El socket no pertenece a la sala actual.");
      }
    }
  });

  // Otros eventos personalizados
  socket.on("customEvent", (data) => {
    console.log("Evento personalizado recibido:", data);
    // Manejo del evento aquí
  });
};
