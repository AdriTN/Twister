import { promises as fs } from 'fs';
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

        await redisClient.set(key, imageBuffer.toString('base64'));
        console.log('Imagen guardada con éxito:', key);
    } catch (error) {
        console.error('Error al guardar la imagen:', error);
    }
}

// Recuperar imagen de Redis
export async function getImageFromRedis(key, outputPath) {
    try {
        await initRedisClient(); // Asegurarse de que Redis está inicializado

        const data = await redisClient.get(key); // Usar promesas para obtener la imagen

        if (data) {
            const buffer = Buffer.from(data, 'base64');
            await fs.writeFile(outputPath, buffer);
            console.log('Imagen recuperada y guardada en:', outputPath);
        } else {
            console.log('No se encontró ninguna imagen con la clave:', key);
        }
    } catch (error) {
        console.error('Error al recuperar la imagen:', error);
    }
}
