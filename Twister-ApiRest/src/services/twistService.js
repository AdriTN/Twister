import { redisClient } from '../app.js';
import { createTwist, updateTwist, deleteTwist, getTwistsByUserId, getGameById, getTwistById } from '../models/twistModel.js';

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
export async function handleupdateTwist(userId, twistData) {

    try {
        // Asegúrate de que el userId y twistId sean válidos
        if (!userId || !twistData) {
            throw new Error("Invalid input: userId, twistId, and twistData are required.");
        }
        const updatedTwist = await updateTwist(userId, twistData);
        if (updatedTwist === -1) {
            throw new Error("You are not the owner of this twist.");
        }
        return { message: 'Twist updated successfully', twist: updatedTwist }; // Retorna el mensaje y el twist actualizado
    } catch (error) {
        console.error("Error editing twist:", error.message);
        throw new Error(error.message); // Lanza el error para que se maneje más arriba en la cadena de llamadas
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

export async function handleGetTwistSocket(newtwistId) {
    try {
        const twist = await getGameById(newtwistId);
        if (!twist) {
            return -1
        }
        console.log("Twist fetched:", twist);
        const adminId = twist.adminId;
        const twistId1 = twist.twistId;
        console.log("Twist ID:", twistId1);
        const questions = getTwistById(twistId1, adminId);
        return questions;

    } catch (error) {
        return -1;
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
        //res.status(500).json({ error: error.message });
    }
}

export async function handleGetUserTwists(userId) {
    try {
        const twists = await getTwistsByUserId(userId);
        return twists;
    } catch (error) {
        console.error("Error fetching twists:", error.message);
        return null;
    }
}

export async function handleDeleteTwist(userId, twistId) {
    try {
        const deleted = await deleteTwist(userId, twistId);
        return deleted;
    } catch (error) {
        console.error("Error deleting twist:", error.message);
        return false;
    }
}
