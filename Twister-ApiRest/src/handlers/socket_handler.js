import { getUserWithTokenSocket } from "../services/authService.js";
import {
  createGame,
  getGameById,
  deleteUserById,
  joinGameById,
  getActiveGames,
} from "../models/gameModel.js";

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

    // Emitir evento de que un jugador se ha unido al propio socket con información completa
    console.log("Player joined susecsfully!");
    socket.emit("PLAYER_JOINED", {
      currentGameId,
      playerId: currentUserId,
      game: newgame,
      playerName: currentUserName,
      imageIndex: imageIndex,
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
        const game = await getGameById(data.roomId, userId, socket.id); // Obtener el juego por PIN
        if (!game) {
          socket.emit("ERROR", { message: "Juego no encontrado" });
          return;
        }
        currentGameId = game.id; // Guardar el ID del juego actual
        socket.emit("PIN_STARTED_PROVIDED", game);
      }
    } catch (err) {
      console.error("Error procesando la solicitud:", err);
      socket.emit("ERROR", { message: "Error procesando solicitud de PIN" });
    }
  });

  // Evento: Iniciar el juego
  socket.on("startGame", (data) => {
    console.log(`Juego iniciado en la sala: ${data.roomId}`);
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
        players: game.players || [] // Asegurar que players siempre sea un array
      });
      console.log(`Jugadores en la sala ${data.roomId}:`, game.players);
    } else {
      console.log(`No se encontró ningún juego con roomId: ${data.roomId}`);
      socket.emit("PLAYERS_LEFT_LIST", {
        roomId: data.roomId,
        players: [] // Sala no encontrada, devolver lista vacía
      });
    }
  } catch (error) {
    console.error("Error al procesar CHECK_PLAYERS_LEFT:", error);
  }
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
      console.log("Falló la eliminación del jugador o el juego ha sido eliminado.");
      return; // Salimos si hubo un fallo
    } else if (typeof result === "string") {
      console.log("El administrador ha abandonado la sala");
      socket.emit("roomDeleted", {
      message: "La sala ha sido eliminada porque solo quedaba el administrador.",
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
