import express, { json } from "express";
import serverless from "serverless-http";
import { getUser, registerUser, loginUser } from "./models/userModel.js";
import { joinGameHandler } from "./handlers/game_handler.js";
import { initDB } from "./utils/database.js";
import { generateAnonToken } from "./models/userModel.js";

const app = express();
const PORT = process.env.PORT || 3000;

app.use(json());

// Inicializar la base de datos
(async () => {
  try {
    await initDB();
  } catch (error) {
    console.error("Error al inicializar la base de datos:", error);
    process.exit(1); // Detener la aplicación si algo falla
  }
})();

// Rutas y manejo de errores

app.post("/users/verify", (req, res) => {
  console.log("Verificando token...");
  const token = req.headers.authorization?.split(" ")[1]; // Extrae el token del encabezado
  console.log("Verificando token:", token);
  if (!token) {
      return res.status(401).json({ message: "No token provided" });
  }

  try {
      const decoded = jwt.verify(token, "tuClaveSecreta"); // Valida el token con tu clave secreta
      res.status(200).json({ message: "Token válido", decoded });
  } catch (error) {
      res.status(401).json({ message: "Token inválido" });
  }
});

app.post("/users/login/anonymous", (req, res) => {
  try {
      const decoded = generateAnonToken()
        res.status(200).json({ message: "Token creado", token: decoded });
  } catch (error) {
      res.status(401).json({ message: "Token inválido" });
  }
});

app.post("/users/register", async (req, res) => {
  const { username, email, password } = req.body;
  console.log("Se ha solicitado el registro de un nuevo usuario");

  try {
    // Validar que todos los campos sean proporcionados
    if (!username || !email || !password) {
      return res.status(400).json({ error: "Faltan campos obligatorios" });
    }

    // Crear el nuevo usuario
    const token = await registerUser(username, email, password);
    res
      .status(201)
      .json({ message: "Usuario registrado con éxito", token: token }); // Incluir el token en la respuesta
  } catch (error) {
    console.error(error.message);
    res.status(401).json({ error: error.message });
  }
});

// Iniciar sesión
app.post("/users/login", async (req, res) => {
  const { email, password } = req.body;

  try {
    const { jwtToken, username} = await loginUser(email, password);
    console.log("Inicio de sesión exitoso del usuario:", email);
    res.status(201).json({
      message: "Inicio de sesión exitoso",
      jwtToken,
      username: username,
    });
  } catch (error) {
    console.error(error.message);
    res.status(401).json({ error: error.message })
  }
});

// Unirse a un juego
app.post("/games/join", async (req, res) => {
  try {
    const result = await joinGameHandler(req.body);
    res.json(result);
  } catch (error) {
    console.error(error.message);
    res.status(500).json({ error: error.message }); // Responder con 500 para errores del servidor
  }
});

app.use((err, req, res, next) => {
  console.error("Error no manejado:", err.message);
  res.status(500).json({ error: "Error interno del servidor" });
});

// Iniciar el servidor
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Servidor escuchando en el puerto ${PORT}`);
});

export const handler = serverless(app);
