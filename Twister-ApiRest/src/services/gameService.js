import { redisClient } from '../app.js'; // Asegúrate de exportar el cliente de Redis desde aquí
import { createGame, getGameById } from '../models/gameModel.js';

// Función para crear un nuevo juego
export async function handleCreateGame(req, res) {
  const { hostId, gameName } = req.body; // Recibe datos necesarios para crear el juego

  if (!hostId || !gameName) {
    return res.status(400).json({ message: 'Host ID and game name are required.' });
  }

  try {
    const newGame = await createGame({ hostId, gameName });
    res.status(201).json({ message: 'Game created successfully', game: newGame });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
}

// Función para actualizar la lista de jugadores (agregar o eliminar)
export async function handleUpdatePlayer(req, res) {
  const { pin, userId, action } = req.body; // Recibimos el PIN, ID del usuario y la acción (add/remove)

  if (!pin || !userId || !action) {
    return res.status(400).json({ message: 'PIN, userId, and action are required.' });
  }

  try {
    // Verificamos si la sala existe
    const gameData = await redisClient.hgetall(`game:${pin}`);
    
    if (!gameData) {
      return res.status(404).json({ message: 'Game not found.' });
    }

    // Obtener la lista de jugadores
    const players = JSON.parse(gameData.players) || [];

    if (action === 'add') {
      // Comprobar si el jugador ya está en la sala
      if (players.includes(userId)) {
        return res.status(400).json({ message: 'User already joined the game.' });
      }
      // Agregar el jugador a la lista
      players.push(userId);
    } else if (action === 'remove') {
      // Comprobar si el jugador está en la lista
      const playerIndex = players.indexOf(userId);
      if (playerIndex === -1) {
        return res.status(400).json({ message: 'User not found in the game.' });
      }
      // Eliminar el jugador de la lista
      players.splice(playerIndex, 1);
    } else {
      return res.status(400).json({ message: 'Invalid action. Use "add" or "remove".' });
    }

    // Actualizar la lista de jugadores en Redis
    await redisClient.hmset(`game:${pin}`, {
      players: JSON.stringify(players),
    });

    res.json({ message: `Player ${action}ed successfully`, players: players });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
}

// Función para iniciar un juego
export async function handleStartGame(req, res) {
  const { pin } = req.body; // Se recibe el PIN de la sala

  try {
    // Verificamos si la sala existe
    const gameData = await redisClient.hgetall(`game:${pin}`);

    if (!gameData) {
      return res.status(404).json({ message: 'Game not found.' });
    }

    const { hostId, players } = gameData;

    // Actualiza el estado del juego
    await redisClient.hmset(`game:${pin}`, {
      status: 'in_progress', // Cambiar estado a "en progreso"
    });

    res.json({ message: 'Game started successfully', hostId, players });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
}

// Función para obtener un juego por ID
export async function handleGetGame(req, res) {
  const { gameId } = req.params; // Recibe el ID del juego desde los parámetros

  try {
    const game = await getGameById(gameId);
    if (!game) {
      return res.status(404).json({ message: 'Game not found' });
    }
    res.status(200).json(game);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
}
