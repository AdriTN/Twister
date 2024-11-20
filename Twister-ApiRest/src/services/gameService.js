import { addPlayerToGame, startGame } from '../models/gameModel.js';

// Función para que un jugador se una al juego
export async function joinGame(req, res) {
  const { gameId, userId } = req.body;

  try {
    const result = await addPlayerToGame(gameId, userId);
    res.json(result); // Retorna el mensaje de éxito
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
}

// Función para iniciar un juego
export async function startGameHandler(req, res) {
  const { gameId } = req.body;

  try {
    const result = await startGame(gameId);
    res.json(result); // Retorna el mensaje de éxito
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
}
