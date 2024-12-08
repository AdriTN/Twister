import jwt from "jsonwebtoken";
import { getUser, getUserIdFromToken } from "../models/userModel.js";

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
  console.log("req.headers.authorization = ", req.headers.authorization);
    var token = null;
  // Verifica que el encabezado esté presente y tenga el formato esperado
  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith("Bearer ")
  ) {
    token = req.headers.authorization.split(" ")[1]; // Extraer el token del encabezado
    console.log("Verifying token:", token);
  } else {

    if (!req.headers.authorization) {
      console.log("Authorization header is missing");
      // Manejar el caso en el que no se encuentra el token
      return res
        .status(400)
        .json({ message: "Authorization header is missing" });
    }
    token = req.headers.authorization?.trim().split(" ")[1];
  }

  // Verificar si no existe un token
  if (!token) {
    return res.status(401).json({ message: "No token provided" });
  }

  try {
    if (getUser(getUserIdFromToken(token))) return true;
    throw new Error("User not found");
  } catch (error) {
    console.error("Token verification failed:", error.message);
    return res.status(403).json({ message: "Invalid or expired token" });
  }
}
