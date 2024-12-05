import { redisClient } from '../app.js'; // Importa el cliente de Redis

// Función para crear un nuevo juego
export async function createGame(gameData) {
  const gameId = `game:${Date.now()}`; // Generar un ID único para el juego
  const game = {
    id: gameId
  };

  // Guardar el juego en Redis
  await redisClient.set(gameId, JSON.stringify(game));

  return game; // Retorna el juego creado
}

// Función para obtener un juego por ID
export async function getGameById(gameId) {
  const game = await redisClient.get(gameId);
  return game ? JSON.parse(game) : null; // Devuelve el juego parseado o null si no existe
}

// Función para añadir un jugador a un juego
export async function addPlayerToGame(gameId, playerId) {
  const game = await getGameById(gameId);
  if (!game) {
    throw new Error('Game not found');
  }

  // Si hay un array de jugadores, añade el nuevo jugador
  if (!game.players) {
    game.players = [];
  }
  game.players.push(playerId);

  // Guarda el juego actualizado en Redis
  await redisClient.set(gameId, JSON.stringify(game));

  return game;
}
