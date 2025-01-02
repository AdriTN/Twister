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
    throw new Error("userId is required to create a twist");
  }

  await ensureRedisClient();

  const twistId = `twist:${Date.now()}`;
  const twist = {
    id: twistId,
    ...twistData,
    imageUrl: twistData.imageUrl
      ? cleanImageUri(twistData.imageUrl)
      : undefined,
    createdAt: Date.now(),
    userId: userId, // Asegúrate de que userId sea válido
  };
  const key = `${userId}-${twist.id}`; // Crea la clave en el formato userId-twistId
  // Guarda el twist en Redis
  await redisClient.set(twistId, JSON.stringify(twist));
  await redisClient.sAdd(`user:${userId}:twists`, key); // Agrega el twist a la lista del usuario

  // Si el twist es público, agrégalo a la lista de públicos
  if (twistData.isPublic) {
    // Asumiendo que isPublic es un campo en twistData
    await redisClient.sAdd("public_twists", key);
  }

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
    console.error(
      `Error fetching twist with ID ${twistId} for user ${userId}:`,
      error.message
    );
    throw new Error(
      `Could not fetch twist with ID ${twistId} for user ${userId}`
    );
  }
}

// Función para actualizar un twist
export async function updateTwist(userId, twistData) {
  await ensureRedisClient();
  const key = `${userId}-${twistData.id}`;
  // Obtiene el twist existente
  let twist = await getTwistById(twistData.id, userId); // Asegúrate de que esta función use la nueva clave
  console.log("Old twist: ", twist);

  if (!twist) {
    console.log("Twist not found, creating new twist...");
    twist = { id: twistData.id, userId, ...twistData, createdAt: Date.now(), updatedAt: Date.now() };
  } else if (twist.userId !== userId) {
    return -1; // Permiso denegado
  } else {
    console.log("Twist found, updating twist...");
    twist = { ...twist, ...twistData, updatedAt: Date.now() }; // Actualiza los datos
  }

  // Guarda el twist y maneja el conjunto público
  await redisClient.set(key, JSON.stringify(twist));
  const isPublic = twist.isPublic;

  if (isPublic) {
    await redisClient.sAdd("public_twists", key);
  } else {
    await redisClient.sRem("public_twists", key);
    console.log(isPublic ? "" : "Twist is private, not saving.");
  }

  return twist; // Retorna el twist actualizado o creado
}



// Función para eliminar un twist
export async function deleteTwist(userId, twistId) {
  const key = `${userId}-${twistId}`; // Crea la clave en el formato userId-twistId
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
  console.log(`Deleting key`);
  const twist = await getTwistById(twistId, userId); // Obtén el twist
  if (!twist) {
    throw new Error("Twist not found"); // Si no se encuentra, lanza un error
  }

  // Verifica que el usuario tenga permiso para eliminar el twist
  if (twist.userId !== userId) {
    throw new Error(
      "Unauthorized: You do not have permission to delete this twist."
    ); // Manejo de autorización
  }

  // Si el twist era público, elimínalo de la lista de públicos
  if (twist.isPublic) {
    await redisClient.sRem("public_twists", key);
  }

  // Elimina el twist de Redis
  const result = await redisClient.del(key);

  // Verifica si la eliminación fue exitosa
  if (result === 0) {
    throw new Error("Failed to delete twist."); // Maneja el caso en que la eliminación no fue exitosa
  }

  return { message: "Twist deleted successfully" }; // Retorna un mensaje de confirmación
}

// Función para obtener un twist por su ID
export async function getTwistById(twistId1, userId) {
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado

  try {
    // Obtiene el twist desde Redis por su ID
    const twist = await redisClient.get(`${userId}-${twistId1}`);

    console.log(
      "Fetching twist with ID:",
      twistId1,
      "from Redis:",
      twist,
      "for user:",
      userId
    );

    // Verifica si se encontró el twist
    if (!twist) {
      return null; // Retorna null si no existe el twist
    }

    // Retorna el twist parseado
    return JSON.parse(twist);
  } catch (error) {
    console.error(
      `Error al obtener el twist con ID ${twistId1}:`,
      error.message
    );
    throw new Error(`No se pudo obtener el twist con ID ${twistId1}`);
  }
}

export async function getPublicTwists(){
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
  try {
    // Obtiene todas las claves de los twists públicos
    const keys = await redisClient.sMembers("public_twists");
    console.log("Public Twist IDs:", keys);

    // Obtiene todos los twists públicos
    const twists = await Promise.all(
      keys.map(async (key) => {
        const twist = await redisClient.get(key); // Obtiene el twist por su clave
        return twist ? JSON.parse(twist) : null; // Retorna el twist parseado o null si no existe
      })
    );

    // Filtra los resultados para eliminar los nulls
    const publicTwists = twists.filter((twist) => twist !== null);

    return publicTwists; // Retorna los twists públicos
  } catch (error) {
    console.error("Error fetching public twists:", error.message);
    throw new Error("Could not fetch public twists");
  }
}

export async function getTwistsFiltered(search){
  await ensureRedisClient(); // Asegúrate de que el cliente de Redis esté conectado
  try {
    // Obtiene todas las claves de los twists públicos
    const keys = await redisClient.keys("*");
    console.log("All Twist IDs:", keys);

    // Obtiene todos los twists públicos
    const twists = await Promise.all(
      keys.map(async (key) => {
        const twist = await redisClient.get(key); // Obtiene el twist por su clave
        return twist ? JSON.parse(twist) : null; // Retorna el twist parseado o null si no existe
      })
    );

    // Filtra los resultados para eliminar los nulls
    const filteredTwists = twists.filter((twist) => twist !== null);

    return filteredTwists; // Retorna los twists públicos
  } catch (error) {
    console.error("Error fetching public twists:", error.message);
    throw new Error("Could not fetch public twists");
  }
}
