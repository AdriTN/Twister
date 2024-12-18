import { redisClient } from "../app.js";
import { handleGetTwistSocket } from "../services/twistService.js";

// Almacena información sobre juegos activos y sus jugadores
const activeGames = {};
const activeTwists = {};

// Clase para manejar la lógica del juego
class Game {
  constructor(id, adminId, socket) {
    this.id = id;
    this.adminId = adminId;
    this.createdAt = Date.now();
    this.socket = socket.id;
    this.players = []; // Inicializar un array de jugadores
    this.twistId = "";
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
  await ensureRedisClient();
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

export async function getGameById(pin, userId, socketId) {
  ensureRedisClient();
  try {
    console.log("getGameById", pin, userId, socketId);
    const gameData = await redisClient.get(pin);
    console.log("gameData", gameData);
    if (!gameData) {
      return null; // Si el juego no existe, retornar null
    }

    const game = JSON.parse(gameData);

    // Asegurarse de que players esté inicializado
    if (!game.players) {
      game.players = [];
    }
    // Verificar si el usuario ya está en el juego
    const playerExists = game.players.find(player => player.id === userId);
    if (playerExists) {
      playerExists.socketId = socketId;
    }
    await redisClient.set(pin, JSON.stringify(game));
    
    // Actualizar el juego en la memoria
    activeGames[pin] = game;
    console.log("game.players", game.players);
    console.log("activeGames", activeGames);

    return game;
  } catch (error) {
    console.error("Error getting game:", error);
    throw new Error("Could not retrieve game");
  }
}

async function updatePlayer(game, userId, socketId, imageIndex) {
  const playerExists = game.players.find(player => player.id === userId);
  
  if (playerExists) {
    playerExists.socketId = socketId; // Actualiza el socketId
  } else {
    game.players.push({ id: userId, socketId, imageIndex }); // Agrega el nuevo jugador
  }
}

export async function joinGameById(pin, userId, socketId, imageIndex) {
  await ensureRedisClient(); // Asegúrate de que sea asíncrono
  try {
    const gameData = await redisClient.get(pin);
    
    if (!gameData) {
      return null; // Si el juego no existe, retorna null
    }

    const game = JSON.parse(gameData);
    if (!game.players) {
      game.players = []; // Inicializa si no existe
    }
    
    await updatePlayer(game, userId, socketId, imageIndex);
    
    await redisClient.set(pin, JSON.stringify(game));
    activeGames[pin] = game;
    
    console.log("game.players", game.players);
    console.log("activeGames", activeGames);

    return game; // Retorna el juego actualizado
  } catch (error) {
    console.error("Error joining game:", error);
    throw new Error("Could not join game");
  }
}

export function getActiveGames() {
  return activeGames
}

export async function getTwistQuestions(roomId){
  try {
    console.log("Obteniendo preguntas de twist para la sala:", roomId);
    const twistQuestions = await handleGetTwistSocket(roomId);
    console.log("Preguntas de twist obtenidas:", twistQuestions);
    return twistQuestions;
  } catch (error) {
    console.error("Error getting twist questions:", error);
    throw new Error("Could not get twist questions");
  }
}

export async function saveTwistInRoom(twistId, roomId) {
  await ensureRedisClient();
  try {
    var gameData = await redisClient.get(roomId);
    if (!gameData) {
      return false;
    }
    gameData = JSON.parse(gameData);
    gameData.twistId = twistId;
    console.log("Guardando twist en la sala:", roomId, "con game:", gameData, "y con twistId:", twistId);
    await redisClient.set(roomId, JSON.stringify(gameData));
    activeTwists[roomId] = twistId;
    console.log("Twist guardado en la sala:", roomId, "con activeTwists:", activeTwists);
    return true;
  } catch (error) {
    console.error("Error saving twist in room:", error);
    throw new Error("Could not save twist in room");
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
      console.log(`Jugador ${removedPlayer} eliminado del juego ${pin}`);

      // Actualizar el juego en Redis
      await redisClient.set(pin, JSON.stringify(game));

      // Verificar el estado del juego después de eliminar al jugador
      if (game.players.length === 0 && game.createdAt < Date.now() - 120000) {
        // Si no quedan jugadores y ha pasado el tiempo, eliminar el juego
        delete activeGames[pin];
        delete activeTwists[pin];
        await redisClient.del(pin);
        const timePassed = Date.now() - game.createdAt;
        console.log(`Juego ${pin} eliminado por falta de jugadores y tiempo de espera de ${timePassed} ms`);
        return -1; // Fallo porque se eliminó el juego
      } else if (game.players.length === 1) {
        // Si solo queda un jugador, se asume que es el administrador
        const adminSocketId = game.players[0].socketId; // Obtener el socket ID del administrador
        return adminSocketId; // Devolver el socket ID del administrador
      }

      return 1; // Éxito al eliminar al jugador
    }
  }

  return -1; // Fallo: el socket ID no se encontró en ninguna sala
}

