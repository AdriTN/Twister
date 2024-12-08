import { redisClient } from '../app.js'; // Asegúrate de exportar el cliente de Redis desde aquí
import { createTwist, updateTwist, deleteTwist, getTwistById } from '../models/twistModel.js';

// Función para crear un nuevo twist
export async function handleCreateTwist(req, res) {
    const { token, question } = req.body; // Recibe el token y el modelo de pregunta

    if (!token || !question) {
        return res.status(400).json({ message: 'Authorization token and question are required.' });
    }

    try {
        const newTwist = await createTwist(question);
        res.status(201).json({ message: 'Twist created successfully', twist: newTwist });
    } catch (error) {
        console.error("Error creating twist:", error.message);
        res.status(500).json({ error: error.message });
    }
}

// Función para editar un twist existente
export async function handleupdateTwist(req, res) {
    const { token, id, question } = req.body; // Recibe el token, ID y modelo de pregunta

    if (!token || !id || !question) {
        return res.status(400).json({ message: 'Authorization token, ID and question are required.' });
    }

    try {
        const updatedTwist = await updateTwist(id, question);
        res.json({ message: 'Twist updated successfully', twist: updatedTwist });
    } catch (error) {
        console.error("Error editing twist:", error.message);
        res.status(500).json({ error: error.message });
    }
}

// Función para eliminar un twist
export async function handleDeleteTwist(req, res) {
    const { token, id } = req.body; // Recibe el token y ID del twist

    if (!token || !id) {
        return res.status(400).json({ message: 'Authorization token and ID are required.' });
    }

    try {
        await deleteTwist(id);
        res.json({ message: 'Twist deleted successfully.' });
    } catch (error) {
        console.error("Error deleting twist:", error.message);
        res.status(500).json({ error: error.message });
    }
}

// Función para obtener un twist por ID
export async function handleGetTwist(req, res) {
    const { id } = req.params; // Recibe el ID del twist desde los parámetros

    try {
        const twist = await getTwistById(id);
        if (!twist) {
            return res.status(404).json({ message: 'Twist not found' });
        }
        res.status(200).json(twist);
    } catch (error) {
        console.error("Error fetching twist:", error.message);
        res.status(500).json({ error: error.message });
    }
}

// Función para cargar una imagen asociada a un twist
export async function handleUploadImage(req, res) {
    const { quizId } = req.params; // Recibe el ID del quiz
    const { token } = req.body; // Recibe el token de autorización
    const image = req.file; // Obtén la imagen del archivo

    if (!token || !quizId || !image) {
        return res.status(400).json({ message: 'Authorization token, quiz ID, and image are required.' });
    }

    try {
        const imageBuffer = image.buffer; // Obtén el buffer de la imagen
        const fileName = image.originalname; // Obtén el nombre original del archivo

        await redisClient.set(`quiz:${quizId}:image`, imageBuffer); // Guarda la imagen en Redis

        res.status(201).json({ message: "Image uploaded successfully", fileName });
    } catch (error) {
        console.error("Error uploading image:", error.message);
        res.status(500).json({ error: error.message });
    }
}

export async function handleGetUserTwists(userId) {
    try {
        const twists = await getTwistById(userId);
        return twists;
    } catch (error) {
        console.error("Error fetching twists:", error.message);
        return null;
    }
}
