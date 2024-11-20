import { get, post } from '../utils/database.js';
import bcrypt from 'bcrypt';


// Obtener un usuario (lectura)
export async function getUser(userId) {
  const sql = 'SELECT * FROM users WHERE userId = ?';
  const results = await get(sql, [userId]);

  if (results.length === 0) {
    throw new Error('Usuario no encontrado');
  }

  return results[0]; // Retorna el primer usuario encontrado
}

// Crear un nuevo usuario (escritura)
export async function createUser(username, email, password) {
  // Validar que los campos necesarios estén definidos
  if (!username || !email || !password) {
    console.error("Error: Se requieren username, email y password para crear el usuario.");
    throw new Error("Faltan campos obligatorios para crear el usuario.");
  }

  console.log("Se ha solicitado crear un nuevo usuario con username:", username, "y email:", email);

  const password_hash = await bcrypt.hash(password, 10);

  // Realizar la consulta SQL con los valores correctos
  try {
    const sql = 'INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)';
    const result = await post(sql, [username, email, password_hash]);
    return result; // Retorna el resultado de la inserción
  } catch (error) {
    console.error("Error en la consulta POST:", error);
    throw new Error("Error al insertar el usuario en la base de datos.");
  }
}
