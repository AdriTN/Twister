import express from "express"; 
import { handleGetUserTwists } from "../services/twistService.js";
import { getUserWithToken } from "../services/authService.js";

const router = express.Router();

// Ruta para crear un nuevo twist
router.post("/create", async (req, res) => {

});

// Ruta para editar un twist existente
router.put("/edit/:id", async (req, res) => {

});


// Ruta para obtener los "twists" del usuario
router.get("/get", async (req, res) => {
    try {
        console.log("Fetching questions...");
        const user = await getUserWithToken(req, res);
        if (!user) {
            return res.status(401).json({ message: "Unauthorized" });
        }

        const twists = await handleGetUserTwists(user.token);
        res.status(200).json({ message: "Twists fetched successfully", twists });
    } catch (error) {
        console.error("Error fetching twists:", error.message);
        res.status(500).json({ message: "Failed to fetch twists." });
    }
});


export default router;
