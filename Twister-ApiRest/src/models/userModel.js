import { get, post } from "../utils/database.js";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

const SECRET_KEY = "twistertokenkey";

class CustomError extends Error {
  constructor(message, code) {
    super(message);
    this.code = code; // Añadimos el código de error
    this.name = this.constructor.name; // Establecemos el nombre del error
  }
}

// Función para validar el formato del email
const validateEmail = (email) => {
  return email.match(
    /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  );
};

// Función para generar el token
const generateToken = (userId) => {
  return jwt.sign(
    { id: userId }, // Datos a incluir en el token
    SECRET_KEY, // Clave secreta
    { expiresIn: "2h" } // Expiración del token
  );
};

// Función para generar el token
export const generateAnonToken = () => {
  const anonId = `anon-${Date.now()}`;

  return jwt.sign(
    { id: anonId }, // Usamos un ID único para el usuario anónimo
    SECRET_KEY, // Clave secreta
    { expiresIn: "2h" } // Expiración del token
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

export const getUserIdFromToken = (token) => {
  try {
    // Verifica y decodifica el token
    const decoded = jwt.verify(token, SECRET_KEY);

    // Retorna el userId del payload decodificado
    return decoded.id;
  } catch (error) {
    console.error("Error verifying token:", error.message);
    return null; // Retorna null si el token es inválido o ha expirado
  }
};

export async function checkUser(userId) {
  const sql = "SELECT * FROM users WHERE userId = ?";
  const user = await get(sql, [userId]);

  if (!user) {
    return false;
  }

  return true;
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
  // Validar que el email y la contraseña sean proporcionados
  if (!email || !password) {
    console.error("Error: Se requieren email y password para iniciar sesión.");
    throw new CustomError("Faltan campos obligatorios para iniciar sesión.", 1); // Código de error 1
  }

  // Validar que el email tenga un formato correcto
  if (!validateEmail(email)) {
    console.error("Error: El email no es válido.");
    throw new CustomError("El email no es válido.", 2); // Código de error 2
  }

    // Buscar al usuario en la base de datos
    const sql = "SELECT * FROM users WHERE email = ?";
    const user = await get(sql, [email]);

    if (!user || user.length === 0) {
      console.error("Error: Usuario no encontrado.");
      throw new CustomError("Usuario o contraseña incorrectos.", 3); // Código de error 3
    }

    // Comparar la contraseña con la almacenada en la base de datos
    const isPasswordValid = await bcrypt.compare(password, user[0].password_hash);
    if (!isPasswordValid) {
      console.error("Error: Contraseña incorrecta.");
      throw new CustomError("Usuario o contraseña incorrectos.", 3); // Código de error 3
    }

    // Generar y devolver el token de autenticación
    const jwtToken = generateToken(user[0].id, user[0].email);
    return { jwtToken, username: user[0].username };

}

export async function getUserTwists(userId) {
  const sql = "SELECT * FROM twists WHERE userId = ?";
  const twists = await get(sql, [userId]);

  return twists;
}
