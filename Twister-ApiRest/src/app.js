import express, { json } from 'express';
import serverless from 'serverless-http';
import { getUser, createUser } from './models/userModel.js';
import { joinGameHandler } from './handlers/game_handler.js';
import { initDB } from './utils/database.js';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(json());

// Inicializar la base de datos
(async () => {
  try {
    await initDB();
  } catch (error) {
    console.error('Error al inicializar la base de datos:', error);
    process.exit(1); // Detener la aplicación si algo falla
  }
})();

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Servidor escuchando en el puerto ${PORT}`);
});
app.get('/users/:id', getUser);

app.post('/users', (req, res) => {
  const { username, email, password } = req.body;
  createUser(username, email, password);
  console.log('Received:', username, email, password);
  res.send({ message: 'User data received' });
});

app.post('/games/join', joinGameHandler);

export const handler = serverless(app);
