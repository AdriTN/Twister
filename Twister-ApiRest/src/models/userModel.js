import { get, post } from "../utils/database.js";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

const SECRET_KEY = "twistertokenkey";

// Función para validar el formato del email
const validateEmail = (email) => {
  return email.match(
    /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  );
};

// Función para generar el token
const generateToken = (userId, email) => {
  return jwt.sign(
    { id: userId, email }, // Datos a incluir en el token
    SECRET_KEY, // Clave secreta
    { expiresIn: "1h" } // Expiración del token
  );
};
// Función para generar el token
export const generateAnonToken = () => {
  const anonId = `anon-${Date.now()}`;

  return jwt.sign(
    { id: anonId }, // Usamos un ID único para el usuario anónimo
    SECRET_KEY, // Clave secreta
    { expiresIn: "1h" } // Expiración del token
  );
};



// Obtener un usuario (lectura)
export async function getUser(userId) {
  const sql = "SELECT * FROM users WHERE userId = ?";
  const user = await get(sql, [userId]);

  if (!user) {
    throw new Error("Usuario no encontrado");
  }

  return user; // Retorna el usuario encontrado
}

// Registro de usuario
export async function registerUser(username, email, password) {
  if (!username || !email || !password) {
    console.error(
      "Error: Se requieren username, email y password para registrarse."
    );
    throw new Error("Faltan campos obligatorios para registrarse.");
  }

  if (!validateEmail(email)) {
    console.error("Error: El email no es válido.");
    throw new Error("El email no es válido.");
  }

  try {
    // Verificar si el correo ya existe en la base de datos
    const sqlCheck = "SELECT * FROM users WHERE email = ?";
    const existingUser = await get(sqlCheck, [email]);

    if (existingUser && existingUser.length > 0) {
      console.error("Error: El correo ya está registrado.");
      throw new Error("El correo ya está registrado.");
    }

    // Encriptar la contraseña
    const password_hash = await bcrypt.hash(password, 10);

    // Insertar el nuevo usuario en la base de datos
    const sqlInsert =
      "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
    const result = await post(sqlInsert, [username, email, password_hash]);

    console.log("Usuario registrado exitosamente:", result);

    // Generar token para el nuevo usuario
    const token = generateToken(result.insertId, email);

    return token;
  } catch (error) {
    console.error("Error al registrar el usuario:", error);
    throw new Error("Error al procesar el registro.");
  }
}

// Inicio de sesión de usuario
export async function loginUser(email, password) {
  if (!email || !password) {
    console.error("Error: Se requieren email y password para iniciar sesión.");
    throw new Error("Faltan campos obligatorios para iniciar sesión.");
  }

  if (!validateEmail(email)) {
    console.error("Error: El email no es válido.");
    throw new Error("El email no es válido.");
  }

  try {
    const sql = "SELECT * FROM users WHERE email = ?";
    const user = await get(sql, [email]);

    if (!user || user.length === 0) {
      console.error("Error: Usuario no encontrado.");
      throw new Error("Usuario o contraseña incorrectos.");
    }

    const isPasswordValid = await bcrypt.compare(password, user[0].password_hash);
    if (!isPasswordValid) {
      console.error("Error: Contraseña incorrecta.");
      throw new Error("Usuario o contraseña incorrectos.");
    }

    // Generar token
    const token = generateToken(user[0].id, user[0].email);

    return { token, username: user[0].username };
  } catch (error) {
    console.error("Error al intentar iniciar sesión:", error);
    throw new Error("Error al procesar la solicitud de inicio de sesión.");
  }
}
