import express from "express";
import { getUserWithToken } from "../services/authService.js";
import { handleUpdatePlayer, handleGetGame, handleCreateGame } from "../services/gameService.js";

const router = express.Router();

// Ruta para crear una nueva sala de juego
router.post("/create", async (req, res) => {
  try {
    // Verificar token del usuario
    const user = await getUserWithToken(req, res); // Asegúrate de usar await si es necesario

    if (!user || !user.gameId) {
      return res.status(400).json({ message: "Twist not found, please try later." });
    }

    // Crear una nueva sala de juego
    const newGame = await handleCreateGame(user.gameId);

    // Enviar respuesta con la nueva sala
    return res.status(201).json({ message: "Game created successfully", game: newGame });
  } catch (error) {
    console.error("Error creating game:", error.message);
    res.status(500).json({ message: "Failed to create game." });
  }
});

// Ruta para que un jugador se una a un juego
router.post("/join", async (req, res) => {
  try {
    await handleUpdatePlayer(req, res);
  } catch (error) {
    console.error("Error joining game:", error.message);
    res.status(500).json({ message: "Failed to join game." });
  }
});

// Ruta para obtener los "twists" del usuario
router.post("/twists/get", async (req, res) => {
  try {
    const user = await getUserWithToken(req, res); // Asegúrate de usar await si es necesario
    if (!user) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    const twists = await handleGetGame(user.token); // Asegúrate de que esta función sea la correcta
    res.status(200).json({ message: "Twists fetched successfully", twists });
  } catch (error) {
    console.error("Error fetching twists:", error.message);
    res.status(500).json({ message: "Failed to fetch twists." });
  }
});

export default router;
