import { redisClient } from '../app.js'; // Importa el cliente de Redis

// Función para crear un nuevo twist
export async function createTwist(twistData) {
    const twistId = `twist:${Date.now()}`; // Generar un ID único para el twist
    const twist = {
        id: twistId,
        ...twistData,
        createdAt: Date.now() // Marca de tiempo de creación
    };

    // Guardar el twist en Redis
    await redisClient.set(twistId, JSON.stringify(twist));

    return twist; // Retorna el twist creado
}

// Función para obtener un twist por ID
export async function getTwistById(twistId) {
    const twist = await redisClient.get(twistId);
    return twist ? JSON.parse(twist) : null; // Devuelve el twist parseado o null si no existe
}

// Función para obtener todos los twists
export async function getAllTwists() {
    const keys = await redisClient.keys('twist:*'); // Obtiene todas las claves de twists
    const twists = await Promise.all(keys.map(key => redisClient.get(key))); // Obtiene todos los twists
    return twists.map(twist => JSON.parse(twist)); // Devuelve la lista de twists parseados
}

// Función para actualizar un twist
export async function updateTwist(twistId, updatedData) {
    const twist = await getTwistById(twistId);
    if (!twist) {
        throw new Error('Twist not found');
    }

    // Actualiza los datos del twist
    const updatedTwist = {
        ...twist,
        ...updatedData, // Merge existing twist data with updated data
        updatedAt: Date.now() // Actualiza la marca de tiempo
    };

    // Guarda el twist actualizado en Redis
    await redisClient.set(twistId, JSON.stringify(updatedTwist));

    return updatedTwist; // Retorna el twist actualizado
}

// Función para eliminar un twist
export async function deleteTwist(twistId) {
    const twist = await getTwistById(twistId);
    if (!twist) {
        throw new Error('Twist not found');
    }

    // Elimina el twist de Redis
    await redisClient.del(twistId);

    return { message: 'Twist deleted successfully' }; // Retorna un mensaje de confirmación
}
