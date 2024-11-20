import { joinGame } from '../services/gameService.js';

export async function joinGameHandler(event) {
  try {
    const { gameId, userId } = JSON.parse(event.body);
    const result = await joinGame(gameId, userId);
    return {
      statusCode: 200,
      body: JSON.stringify(result),
    };
  } catch (error) {
    return {
      statusCode: 500,
      body: JSON.stringify({ message: 'Error joining game', error: error.message }),
    };
  }
}
