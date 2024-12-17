import { redisClient } from "../app.js";

// Almacena información sobre juegos activos y sus jugadores
const activeGames = {};

// Clase para manejar la lógica del juego
class Game {
  constructor(id, adminId, socket) {
    this.id = id;
    this.adminId = adminId;
    this.createdAt = Date.now();
    this.socket = socket.id;
    this.players = []; // Inicializar un array de jugadores
  }
}

async function ensureRedisClient() {
  if (!redisClient.isOpen) {
    try {
      await redisClient.connect(); // Intenta conectar si no está conectado
      console.log("Conexión a Redis establecida.");
    } catch (error) {
      console.error("Error al conectar a Redis:", error);
      throw new Error("Could not connect to Redis");
    }
  }
}

// Función para crear un nuevo juego
export async function createGame(adminId, socket) {
  ensureRedisClient();
  const pin = Math.floor(100000 + Math.random() * 900000).toString();
  console.log(`Creando juego con PIN: ${pin} y el administrador: ${adminId}`);
  const game = new Game(pin, adminId, socket);

  try {
    // Inicializar la sesion de redis
    await redisClient.set(pin, JSON.stringify(game));
    // Almacenar el juego activo en la memoria
    activeGames[pin] = game; 
    return game;
  } catch (error) {
    console.error("Error creating game:", error);
    throw new Error("Could not create game");
  }
}

// Función para obtener un juego por ID y añadir un usuario si no está presente
export async function getGameById(pin, userId, socketId) {
  ensureRedisClient();
  try {
    const gameData = await redisClient.get(pin);
    
    if (!gameData) {
      return null; // Si el juego no existe, retornar null
    }

    const game = JSON.parse(gameData);

    // Asegurarse de que players esté inicializado
    if (!game.players) {
      game.players = [];
    }
    var ImageId = null;
    // Verificar si el usuario ya está en el juego
    const playerExists = game.players.find(player => player.id === userId);
    if (!playerExists) {
      ImageId = Math.floor(Math.random() * 24) + 1;
      game.players.push({ id: userId, socketId: socketId, imageId: ImageId}); // Añadir el usuario si no está ya
    } else {
      // Actualizar el socketId si el jugador ya está en la lista
      playerExists.socketId = socketId;
    }

    // Guardar el juego actualizado en Redis
    await redisClient.set(pin, JSON.stringify(game));
    
    // Actualizar el juego en la memoria
    activeGames[pin] = game;

    return ImageId;
  } catch (error) {
    console.error("Error getting game:", error);
    throw new Error("Could not retrieve game");
  }
}

// Función para añadir un jugador a un juego
export async function addPlayerToGame(pin, playerId, socketId) {
  ensureRedisClient();

  const game = await getGameById(pin, playerId, socketId);
  if (!game) {
    throw new Error("Game not found");
  }

  // Verifica si el jugador ya está en el juego
  if (game.players.some(player => player.id === playerId)) {
    throw new Error("Player already in game");
  }

  game.players.push({ id: playerId, socketId: socketId, imageId: Math.floor(Math.random() * 24) + 1});

  try {
    await redisClient.set(pin, JSON.stringify(game));
    // Actualizar el juego en la memoria
    activeGames[pin] = game;
    return game;
  } catch (error) {
    console.error("Error updating game:", error);
    throw new Error("Could not update game");
  }
}

// Función para eliminar un jugador por su socketId
export async function deleteUserById(socketId) {
  ensureRedisClient();

  // Buscar el juego activo donde el socketId está presente
  for (const pin in activeGames) {
    const game = activeGames[pin];
    const playerIndex = game.players.findIndex(player => player.socketId === socketId);
    
    if (playerIndex !== -1) {
      // Eliminar al jugador de la lista
      const removedPlayer = game.players.splice(playerIndex, 1);
      console.log(`Jugador ${removedPlayer.id} eliminado del juego ${pin}`);

      // Actualizar el juego en Redis
      await redisClient.set(pin, JSON.stringify(game));

      // Si no quedan jugadores, puedes eliminar el juego si es necesario
      if (game.players.length === 0) {
        delete activeGames[pin]; // Eliminar el juego de la memoria
        await redisClient.del(pin); // También puedes eliminarlo de Redis
        console.log(`Juego ${pin} eliminado por falta de jugadores`);
      }

      break; // Salir del bucle después de encontrar el jugador
    }
  }
}
