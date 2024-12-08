import jwt from "jsonwebtoken";
import { getUser, getUserIdFromToken } from "../models/userModel.js";
import { get } from "../utils/database.js";

export async function verifyToken(req, res, next) {
  // Extraer el token del encabezado Authorization
  const token = req.headers.authorization?.split(" ")[1]; // Extract token from header

  console.log("Verifying token:", token);

  // Verificar si no existe un token
  if (!token) {
    return res.status(401).json({ message: "No token provided" });
  }

  try {
    // Verificar el token con la clave secreta
    const decoded = jwt.verify(token, "SECRET_KEY"); // Replace "SECRET_KEY" with your actual secret key

    // Si el token es válido, añadir el decoded info (como el user ID) a la solicitud
    req.user = decoded;

    // Continuar con la siguiente función (middleware o controlador)
    next();
  } catch (error) {
    console.error("Token verification failed:", error.message);
    return res.status(403).json({ message: "Invalid or expired token" });
  }
}

export async function getUserWithToken(req, res) {
  // Extraer el token del encabezado Authorization
    var token = null;
  // Verifica que el encabezado esté presente y tenga el formato esperado
  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith("Bearer ")
  ) {
    token = req.headers.authorization.split(" ")[1]; // Extraer el token del encabezado
  } else {
    if (!req.headers.authorization) {
      console.log("Authorization header is missing");
      // Manejar el caso en el que no se encuentra el token
      return res
        .status(400)
        .json({ message: "Authorization header is missing" });
    }
    token = req.headers.authorization?.trim();
  }

  // Verificar si no existe un token
  if (!token || token.length < 10) {
    return res.status(401).json({ message: "No token provided" });
  }

  try {
    const user = getUserIdFromToken(token);    
    // Verificar si el usuario es anónimo
    if (typeof user === "string" && user.includes("anon-")) {
        return res.status(403).json({ message: "User is anonymous" });
    }
    // Comprobar si el usuario no fue encontrado
    if (!user) {
        throw new Error("User not found");
    }
    return user;
} catch (error) {
    console.error("Token verification failed:", error.message);
    return res.status(403).json({ message: "Invalid or expired token" });
}

}
