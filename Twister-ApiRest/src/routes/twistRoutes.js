import express from "express"; 
import { handleGetUserTwists, handleupdateTwist, handleDeleteTwist } from "../services/twistService.js";
import { getUserWithToken } from "../services/authService.js";

const router = express.Router();

// Ruta para crear un nuevo twist
router.post("/create", async (req, res) => {

});

// Ruta para editar un twist existente
router.put("/edit", async (req, res) => {
    try {
        const userId = await getUserWithToken(req, res);
        if (!userId || userId === -1) {
            return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
        }
        const twistData = req.body;
        const updatedTwist = await handleupdateTwist(userId, twistData);
        console.log("Se va a editar un twist", JSON.stringify(twistData, null, 2));
        
        if (!updatedTwist) {
            return res.status(404).json({ message: "Twist not found." }); // Respuesta si no se encuentra el twist
        }

        return res.status(200).json({ message: "Twist updated successfully", twist: updatedTwist });
    } catch (error) {
        console.error("Error updating twist:", error.message);
        if (!res.headersSent) {
            return res.status(500).json({ message: "Failed to update twist." }); // Respuesta en caso de error
        }
    }
});


// Ruta para obtener los "twists" del usuario
router.get("/get", async (req, res) => {
    try {
        console.log("Fetching questions...");
        const user = await getUserWithToken(req, res);
        if (!user || user == -1) {
            return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
        }

        const twists = await handleGetUserTwists(user);
        return res.status(200).json({ message: "Twists fetched successfully", twists });
    } catch (error) {
        console.error("Error fetching twists:", error.message);
        if (!res.headersSent) {
            return res.status(500).json({ message: "Failed to fetch twists." }); // Respuesta en caso de error
        }
    }
});

// Ruta para eliminar un twist existente
router.delete("/delete/:id", async (req, res) => {
    try {
        const userId = await getUserWithToken(req, res);
        if (!userId || userId === -1) {
            return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
        }

        const twistId = req.params.id; // Obtener el ID del twist desde los parámetros de la solicitud
        console.log("Se va a eliminar un twist con ID:", twistId, "por el usuario:", userId);
        const deleted = await handleDeleteTwist(userId, twistId); // Llamar a la función que maneja la eliminación

        if (!deleted) {
            return res.status(404).json({ message: "Twist not found." }); // Respuesta si no se encuentra el twist
        }

        return res.status(200).json({ message: "Twist deleted successfully." }); // Respuesta exitosa
    } catch (error) {
        console.error("Error deleting twist:", error.message);
        if (!res.headersSent) {
            return res.status(500).json({ message: "Failed to delete twist." }); // Respuesta en caso de error
        }
    }
});




export default router;
