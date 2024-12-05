export const socketHandlers = (io, socket) => {
    console.log("Nuevo cliente conectado:", socket.id);
  
    // Evento: Unirse a un juego
    socket.on("joinGame", (data) => {
      console.log(`Jugador ${socket.id} se unió a la sala:`, data.roomId);
  
      // Lógica para unirse a la sala
      socket.join(data.roomId);
      io.to(data.roomId).emit("playerJoined", { playerId: socket.id });
    });
  
    // Evento: Iniciar el juego
    socket.on("startGame", (data) => {
      console.log(`Juego iniciado en la sala: ${data.roomId}`);
  
      // Emitir inicio del juego a todos los clientes de la sala
      io.to(data.roomId).emit("gameStarted", { roomId: data.roomId });
    });
  
    // Evento: Desconexión del cliente 
    socket.on("disconnect", () => {
      console.log("Cliente desconectado:", socket.id);
    });
  
    // Otros eventos personalizados
    socket.on("customEvent", (data) => {
      console.log("Evento personalizado recibido:", data);
      // Manejo del evento aquí
    });
  };
  