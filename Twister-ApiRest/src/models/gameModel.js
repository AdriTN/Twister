import { get, post } from '../utils/database.js'; // Importa las funciones get y post para interactuar con la DB

// Función para agregar un jugador a un juego
export async function addPlayerToGame(gameId, userId) {
  try {
    // Verificamos si el juego existe en la base de datos
    const sqlGame = 'SELECT * FROM games WHERE gameId = ?';
    const game = await get(sqlGame, [gameId]);

    if (game.length === 0) {
      throw new Error('Juego no encontrado');
    }

    // Verificamos si ya el jugador está en el juego
    const sqlPlayerCheck = 'SELECT * FROM game_players WHERE gameId = ? AND userId = ?';
    const existingPlayer = await get(sqlPlayerCheck, [gameId, userId]);

    if (existingPlayer.length > 0) {
      throw new Error('El jugador ya está en el juego');
    }

    // Agregamos al jugador a la tabla de game_players (suponiendo que tienes una tabla de jugadores)
    const sqlAddPlayer = 'INSERT INTO game_players (gameId, userId) VALUES (?, ?)';
    await post(sqlAddPlayer, [gameId, userId]);

    // Retornamos un mensaje de éxito o el estado actualizado del juego
    return { message: 'Jugador agregado al juego con éxito' };

  } catch (error) {
    console.error('Error al agregar jugador al juego:', error);
    throw error;
  }
}

// Función para iniciar un juego (por ejemplo, cambiar estado a "iniciado")
export async function startGame(gameId) {
  try {
    // Verificamos si el juego existe
    const sqlGame = 'SELECT * FROM games WHERE gameId = ?';
    const game = await get(sqlGame, [gameId]);

    if (game.length === 0) {
      throw new Error('Juego no encontrado');
    }

    // Cambiar el estado del juego a "iniciado"
    const sqlStartGame = 'UPDATE games SET status = ? WHERE gameId = ?';
    await post(sqlStartGame, ['iniciado', gameId]);

    return { message: 'Juego iniciado con éxito' };

  } catch (error) {
    console.error('Error al iniciar el juego:', error);
    throw error;
  }
}
