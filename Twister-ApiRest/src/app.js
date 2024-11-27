import express, { json } from "express";
import serverless from "serverless-http";
import { getUser, registerUser, loginUser } from "./models/userModel.js";
import { joinGameHandler } from "./handlers/game_handler.js";
import { initDB } from "./utils/database.js";
import { generateAnonToken } from "./models/userModel.js";
import { startGameHandler } from "./services/gameService.js";
import { verifyToken } from "./services/index.js";

const app = express();
const PORT = process.env.PORT || 3000;

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

// Routes and error handling

app.post("/users/verify", (req, res) => {
  console.log("Verifying token...");
  verifyToken(req, res);
  res.status(200).json({ message: "Token valid", decoded });

});

app.post("/users/login/anonymous", (req, res) => {
  try {
      const decoded = generateAnonToken()
        res.status(200).json({ message: "Token created", token: decoded });
  } catch (error) {
      res.status(401).json({ message: "Invalid token" });
  }
});

app.post("/users/register", async (req, res) => {
  const { username, email, password } = req.body;
  console.log("New user registration requested");

  try {
    // Validate that all required fields are provided
    if (!username || !email || !password) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    // Create new user
    const token = await registerUser(username, email, password);
    res
      .status(201)
      .json({ message: "User successfully registered", token: token }); // Include token in response
  } catch (error) {
    console.error(error.message);
    res.status(401).json({ error: error.message });
  }
});

// Login
app.post("/users/login", async (req, res) => {
  const { email, password } = req.body;

  try {
    // Attempt login
    const { jwtToken, username } = await loginUser(email, password);
    console.log("User login successful:", email);

    // Respond with token and username on success
    res.status(200).json({
      message: "Login successful",
      jwtToken,
      username: username,
    });
  } catch (error) {
    console.log("This is the error:", error.message);
    console.error(error.code);

    // Depending on the error code, return appropriate status code
    switch (error.code) {
      case 1:
        // Status code 400 for bad request
        res.status(400).json({ error: "Please provide both email and password." });
        break;
      case 2:
        // Status code 400 for bad request
        res.status(400).json({ error: "The email format is invalid. Please check and try again." });
        break;
      case 3:
        // Status code 401 for unauthorized (incorrect credentials)
        res.status(401).json({ error: "Incorrect username or password. Please try again." });
        break;
      default:
        // Status code 500 for server errors
        res.status(500).json({ error: "Error processing login request. Please try again later." });
    }
  }
});


// Ruta para crear un nuevo juego (anfitrión)
app.post('/games/create', (req, res) => {
  getUserWithToken(req, res);

  if (!gameId) {
      return res.status(400).json({ message: 'Twist not found, please try later.' });
  }

  // Crear una nueva sala de juego
  startGameHandler(gameId);

  // Enviar respuesta con la nueva sala
  return res.status(201).json({ message: 'Game created successfully', game: newGame });
});

// Ruta para que un invitado se una al juego (unirse por QR o código de sala)
app.post('/games/join', async (req, res) => {
  joinGame(req, res);
});


app.use((err, req, res, next) => {
  console.error("Unhandled error:", err.message);
  res.status(500).json({ error: "Internal server error" });
});

// Start the server
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server listening on port ${PORT}`);
});

export const handler = serverless(app);
