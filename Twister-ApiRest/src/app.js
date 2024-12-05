import express, { json } from "express";
import { createServer } from "http"; // Servidor HTTP
import { Server } from "socket.io"; // Socket.IO
import serverless from "serverless-http";
import redis from "redis";

import usersRoutes from "./routes/usersRoutes.js";
import gamesRoutes from "./routes/gamesRoutes.js";
import { initDB } from "./utils/database.js";
import { socketHandlers } from "./handlers/socket_handler.js";

const app = express();
const PORT = process.env.PORT || 3000;

// Crear servidor HTTP y WebSocket
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"],
  },
});

const redisClient = redis.createClient({
  host: "127.0.0.1", // Cambia esto si tu Redis está en otro host
  port: 6379,
});

// Manejo de errores de conexión de Redis
redisClient.on("error", (err) => {
  console.error("Error de conexión a Redis:", err);
});


app.use(json());

// Initialize the database
(async () => {
  try {
    await initDB();
  } catch (error) {
    console.error("Error initializing the database:", error);
    process.exit(1); // Stop the application if something fails
  }
})();

// Usar las rutas
app.use("/users", usersRoutes);
app.use("/games", gamesRoutes);

// Manejo de errores generales
app.use((err, req, res, next) => {
  console.error("Unhandled error:", err.message);
  res.status(500).json({ error: "Internal server error" });
});

// Integrar los manejadores de sockets
io.on("connection", (socket) => {
  socketHandlers(io, socket); // Pasar el servidor y el socket al manejador
});

// Iniciar el servidor HTTP (que incluye sockets)
httpServer.listen(PORT, () => {
  console.log(`Server listening on http://localhost:${PORT}`);
});

// Exportar para AWS Lambda o entornos serverless
export const handler = serverless(app);
export { redisClient };