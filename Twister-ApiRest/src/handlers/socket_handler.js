import { getUserWithTokenSocket } from "../services/authService.js";
import { createGame, getGameById, deleteUserById } from "../models/gameModel.js";

export const socketHandlers = (io, socket) => {
  console.log("Nuevo cliente conectado:", socket.id);

  // Almacena el ID del juego y el usuario al unirse
  let currentGameId = null;
  let currentUserId = null;
  let currentUserName = null;
  let isAnonymous = false;


  // Evento: Unirse a un juego
  socket.on("JOIN_ROOM", async (data) => {
    data = JSON.parse(data);
    // Desestructurar los datos recibidos
    const { roomId, isAnonymous, userName } = data;

    // Asignar el ID del juego y el ID del usuario
    const currentGameId = roomId; // ID de la sala a la que se une
    const currentUserId = socket.id; // ID del socket del usuario
    // Asignar el nombre del usuario o usar "Jugador Anónimo" si es anónimo
    const currentUserName = isAnonymous ? "Jugador Anónimo" : userName; 

    console.log(`Unirse a la sala: ${currentGameId} - ${currentUserName}`);
    const imageval = await getGameById(currentGameId, currentUserId, socket.id);
    // Unirse a la sala
    socket.join(currentGameId);

    // Emitir evento de que un jugador se ha unido
    io.to(currentGameId).emit("playerJoined", { playerId: currentUserId, playerName: currentUserName });
    // Emitir evento de que un jugador se ha unido
    console.log("imageval", imageval);
    socket.emit("PLAYER_JOINED", { currentGameId, playerId: currentUserId, imageId: imageval });
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
      currentUserId = userId; // Actualizar el ID del usuario

      if (data.isNew) {
        const game = await createGame(userId, socket); // Crear un juego con el ID de usuario y el socket
        socket.emit("PIN_PROVIDED", { pin: game.id, game });
      } else {
        const game = await getGameById(data.pin, userId, socket.id); // Obtener el juego por PIN
        if (!game) {
          socket.emit("ERROR", { message: "Juego no encontrado" });
          return;
        }
        currentGameId = game.id; // Guardar el ID del juego actual
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

  // Evento: Desconexión del cliente
  socket.on("disconnect", async () => {
    console.log("Cliente desconectado:", socket.id);
    deleteUserById(socket.id);
    
    // Notificar a otros jugadores
    if (currentGameId) {
      io.to(currentGameId).emit("playerLeft", { playerId: socket.id, playerName: currentUserName });
    }
  });

  // Otros eventos personalizados
  socket.on("customEvent", (data) => {
    console.log("Evento personalizado recibido:", data);
    // Manejo del evento aquí
  });
};
