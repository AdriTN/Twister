import { promises as fs } from 'fs';
import path from 'path';
import { createClient } from 'redis';

// Declarar redisClient aquí como una variable que se inicializa más tarde
let redisClient;
let clientInitialized = false;

// Inicializar Redis si no está hecho
async function initRedisClient() {
    if (!clientInitialized) {
        try {
            redisClient = createClient({
                host: '127.0.0.1',
                port: 6379,
            });

            redisClient.on('error', (err) => {
                console.error('Error de conexión a Redis:', err);
            });

            await redisClient.connect();
            clientInitialized = true; // Marcar como inicializado
            console.log('Conectado a Redis');
        } catch (error) {
            console.error('Error al inicializar el cliente de Redis:', error);
        }
    }
}

// Guardar imagen en Redis
export async function saveImageToRedis(key, imageBuffer) {
    try {
        await initRedisClient(); // Asegurarse de que Redis está inicializado

        // Guarda la imagen en formato Base64
        await redisClient.set(key, imageBuffer.toString('base64'));
        console.log('Imagen guardada con éxito:', key);

        // Genera y guarda los metadatos
        const metadataKey = `${key}:metadata`;
        const metadata = {
            lastModified: new Date().toISOString(), // Fecha actual
            size: imageBuffer.length, // Tamaño de la imagen en bytes
        };
        await redisClient.set(metadataKey, JSON.stringify(metadata));
        console.log('Metadatos guardados con éxito:', metadataKey);

        return key;
    } catch (error) {
        console.error('Error al guardar la imagen y metadatos:', error);
    }
}


// Recuperar imagen de Redis
export async function getImageFromRedis(key) {
    try {
        await initRedisClient(); // Asegurarse de que Redis está inicializado
        console.log('Recuperando imagen con clave:', key);
        const data = await redisClient.get(key); // Usar promesas para obtener la imagen

        if (data) {
            const buffer = Buffer.from(data, 'base64'); // Convierte los datos a un buffer
            console.log('Imagen recuperada.');
            return buffer; // Devuelve el buffer de la imagen
        } else {
            console.log('No se encontró ninguna imagen con la clave:', key);
            return null; // Devuelve null si no se encontró la imagen
        }
    } catch (error) {
        console.error('Error al recuperar la imagen:', error);
        throw error; // Lanza el error para que se maneje en el router
    }
}

export const deleteImageFromRedis = async (imageId) => {
    try {
      await initRedisClient();
      const result = await redisClient.del(imageId);
      return result > 0; // Retorna true si se eliminó con éxito
    } catch (error) {
      console.error("Error deleting image from Redis:", error.message);
      throw error;
    }
  };


// Recuperar metadatos de imagen desde Redis
export async function getImageMetadata(key) {
    try {
        await initRedisClient(); // Asegurarse de que Redis está inicializado

        // Supongamos que los metadatos están almacenados con la clave `${key}:metadata`
        const metadataKey = `${key}:metadata`;
        const metadata = await redisClient.get(metadataKey);

        if (metadata) {
            return JSON.parse(metadata); // Devolver los metadatos como objeto
        } else {
            console.log('No se encontraron metadatos para la clave:', key);
            return null;
        }
    } catch (error) {
        console.error('Error al obtener los metadatos:', error);
        throw error;
    }
}
