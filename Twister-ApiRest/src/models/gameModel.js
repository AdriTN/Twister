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

// export async function getGameById(pin, userId, socketId) {
//   ensureRedisClient();
//   try {
//     const gameData = await redisClient.get(pin);
    
//     if (!gameData) {
//       return null; // Si el juego no existe, retornar null
//     }

//     const game = JSON.parse(gameData);

//     // Asegurarse de que players esté inicializado
//     if (!game.players) {
//       game.players = [];
//     }
//     var ImageId = null;
//     // Verificar si el usuario ya está en el juego
//     const playerExists = game.players.find(player => player.id === userId);
//     if (!playerExists) {
//       ImageId = Math.floor(Math.random() * 24) + 1;
//       game.players.push({ id: userId, socketId: socketId, imageId: ImageId}); // Añadir el usuario si no está ya
//     } else {
//       // Actualizar el socketId si el jugador ya está en la lista
//       playerExists.socketId = socketId;
//     }

//     // Guardar el juego actualizado en Redis
//     await redisClient.set(pin, JSON.stringify(game));
    
//     // Actualizar el juego en la memoria
//     activeGames[pin] = game;

//     return ImageId;
//   } catch (error) {
//     console.error("Error getting game:", error);
//     throw new Error("Could not retrieve game");
//   }
// }

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
    // Guardar el juego actualizado en Redis
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

