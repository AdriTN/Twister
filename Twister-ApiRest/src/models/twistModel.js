import { redisClient } from "../app.js"; // Importa el cliente de Redis

function cleanImageUri(url) {
  // Busca el patrón "ImageUri()" y lo elimina si está presente
  return url.replace(/ImageUri\(\)/g, "").trim();
}

// Función para asegurarse de que el cliente de Redis esté conectado
async function ensureRedisClient() {
  if (!redisClient.isOpen) {
    try {
      await redisClient.connect(); // Intenta conectar si no está conectado
      console.log("Conexión a Redis establecida.");
    } catch (error) {
      console.error("Error al conectar a Redis:", error);
      throw new Error("Could not connect to Redis");
    }
  }
}

// Función para crear un nuevo twist
export async function createTwist(twistData, userId) {
    if (!userId) {
        throw new Error('userId is required to create a twist');
    }

    await ensureRedisClient();

    const twistId = `twist:${Date.now()}`;
    const twist = {
        id: twistId,
        ...twistData,
        imageUrl: twistData.imageUrl ? cleanImageUri(twistData.imageUrl) : undefined,
        createdAt: Date.now(),
        userId: userId // Asegúrate de que userId sea válido
    };

    await redisClient.set(twistId, JSON.stringify(twist));
    await redisClient.sAdd(`user:${userId}:twists`, twistId); // Cambia a sAdd

    return twist;
}



// Función para obtener todos los twists de un usuario específico
export async function getTwistsByUserId(userId) {
    await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
    try {
        console.log("Fetching twists for user:", userId);
        // Obtener todas las claves que coincidan con el patrón userId-*
        const allTwistIds = await redisClient.keys(`${userId}-*`); // Busca todas las claves que comienzan con userId-
        console.log("All Twist IDs for user:", allTwistIds);

        // Obtener todos los twists
        const twists = await Promise.all(
            allTwistIds.map(async (twistId) => {
                const twist = await redisClient.get(twistId); // Usa la clave directamente
                return twist ? JSON.parse(twist) : null; // Devuelve el twist parseado o null si no existe
            })
        );

        // Filtrar los resultados para eliminar los nulls
        const userTwists = twists.filter((twist) => twist !== null);

        return userTwists; // Devuelve los twists del usuario
    } catch (error) {
        console.error("Error fetching twists:", error.message);
        throw new Error("Could not fetch twists");
    }
}

  

// Función para obtener todos los twists
export async function getAllTwists() {
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado

  const keys = await redisClient.keys("twist:*"); // Obtiene todas las claves de twists
  const twists = await Promise.all(keys.map((key) => redisClient.get(key))); // Obtiene todos los twists
  return twists.map((twist) => JSON.parse(twist)); // Devuelve la lista de twists parseados
}

// Función para obtener un twist por su clave en el formato userId-twistId
export async function getGameById(key) {
    await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
  
    try {
      // Crea la clave en el formato userId-twistId
      console.log("Fetching twist with ID:", key);
  
      // Obtener el twist desde Redis
      const twist = await redisClient.get(key);
      // Verificar si se encontró el twist
      if (!twist) {
        return null; // Retorna null si no se encuentra el twist
      }
  
      // Retornar el twist parseado
      return JSON.parse(twist);
    } catch (error) {
      console.error(`Error fetching twist with ID ${twistId} for user ${userId}:`, error.message);
      throw new Error(`Could not fetch twist with ID ${twistId} for user ${userId}`);
    }
  }
  

// Función para actualizar un twist
export async function updateTwist(userId, twistData) {
    await ensureRedisClient();
  
    // Crea la clave en el formato userId-twistId
    const key = `${userId}-${twistData.id}`;
  
    // Intenta obtener el twist existente
    let twist = await getTwistById(key); // Asegúrate de que esta función use la nueva clave
  
    // Si no se encuentra el twist, crea uno nuevo
    console.log("Old twist: ", twist)
    if (!twist) {
        console.log("Twist not found, creating new twist...");
        twist = {
            id: twistData.id,
            userId: userId,
            ...twistData, // Usa los datos proporcionados
            createdAt: Date.now(),
            updatedAt: Date.now(),
        };
    } else {
        // Verifica que el usuario tenga permiso para editar
        if (twist.userId !== userId) return -1;
        console.log("Twist found, updating twist...");
        console.log("New twist: ", twist)
        // Actualiza los datos del twist existente
        twist = {
            ...twist,
            ...twistData, // Combina los datos existentes con los datos actualizados
            updatedAt: Date.now(), // Actualiza la marca de tiempo
        };
    }
  
    // Guarda el twist (nuevo o actualizado) en Redis con la nueva clave
    await redisClient.set(key, JSON.stringify(twist));
  
    return twist; // Retorna el twist actualizado o creado
}

  

// Función para eliminar un twist
export async function deleteTwist(userId, twistId) {
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
  const twist = await getTwistById(twistId, userId); // Obtén el twist
  if (!twist) {
      throw new Error("Twist not found"); // Si no se encuentra, lanza un error
  }

  // Verifica que el usuario tenga permiso para eliminar el twist
  if (twist.userId !== userId) {
      throw new Error("Unauthorized: You do not have permission to delete this twist."); // Manejo de autorización
  }

  // Elimina el twist de Redis
  const result = await redisClient.del(`${userId}-${twistId}`);

  // Verifica si la eliminación fue exitosa
  if (result === 0) {
      throw new Error("Failed to delete twist."); // Maneja el caso en que la eliminación no fue exitosa
  }

  return { message: "Twist deleted successfully" }; // Retorna un mensaje de confirmación
}

// Función para obtener un twist por su ID
export async function getTwistById(twistId, userId) {
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado

  try {
    // Obtiene el twist desde Redis por su ID
    const twist = await redisClient.get(`${userId}-${twistId}`);

    console.log("Fetching twist with ID:", twistId, "from Redis:", twist, "for user:", userId);

    // Verifica si se encontró el twist
    if (!twist) {
      return null; // Retorna null si no existe el twist
    }

    // Retorna el twist parseado
    return JSON.parse(twist);
  } catch (error) {
    console.error(`Error al obtener el twist con ID ${twistId}:`, error.message);
    throw new Error(`No se pudo obtener el twist con ID ${twistId}`);
  }
}
