import { getUserWithTokenSocket } from "../services/authService.js";
import { createGame, getGameById, deleteUserById } from "../models/gameModel.js";

export const socketHandlers = (io, socket) => {
  console.log("Nuevo cliente conectado:", socket.id);

  // Almacena el ID del juego y el usuario al unirse
  let currentGameId = null;
  let currentUserId = null;

  // Evento: Unirse a un juego
  socket.on("joinGame", async (data) => {
    console.log(`Jugador ${socket.id} se unió a la sala:`, data.roomId);
    currentGameId = data.roomId; // Guardar el ID del juego actual
    currentUserId = socket.id; // Guardar el ID del usuario

    // Lógica para unirse a la sala
    socket.join(data.roomId);
    io.to(data.roomId).emit("playerJoined", { playerId: socket.id });
  });

  // Evento: Solicitar PIN
  socket.on("REQUEST_PIN", async (data) => {
    try {
      data = JSON.parse(data);
      if (!data || !data.token) {
        socket.emit("ERROR", { message: "Datos de solicitud incorrectos" });
        return;
      }
      const userId = getUserWithTokenSocket(data.token); // Obtener el ID de usuario desde el token
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
    io.to(currentGameId).emit("playerLeft", { playerId: socket.id });
      }
  );
  

  // Otros eventos personalizados
  socket.on("customEvent", (data) => {
    console.log("Evento personalizado recibido:", data);
    // Manejo del evento aquí
  });
};
