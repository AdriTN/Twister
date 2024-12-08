import express from "express";
import multer from "multer"; // Importa multer para manejar la carga de archivos
import { getUserWithToken } from "../services/authService.js";
import { saveImageToRedis, getImageFromRedis } from "../services/imageService.js";

const router = express.Router();
const upload = multer();

// Ruta para subir imágenes
router.post("/upload", upload.single("image"), async (req, res) => {
  try {
    console.log("Uploading image:", req.file.originalname);
    const imageBuffer = req.file.buffer; // Obtén el buffer de la imagen
    const fileName = req.file.originalname; // Obtén el nombre original del archivo

    await saveImageToRedis(fileName, imageBuffer);

    //TODO: Guardar la imagen en S3

    return res.status(201).json({ message: "Image uploaded successfully", fileName });
  } catch (error) {
    console.error("Error uploading image:", error.message);
    res.status(500).json({ message: "Failed to upload image." });
  }
});

// Ruta para descargar imágenes
router.get("/download/:fileName", async (req, res) => {
  try {
    const fileName = req.params.fileName;
    const imageBuffer = await getImageFromRedis(fileName); // Recupera la imagen desde Redis

    if (!imageBuffer) {
      return res.status(404).json({ message: "Image not found" });
    }

    // Establece los encabezados para la respuesta
    res.set("Content-Type", "image/jpeg"); // Cambia el tipo según el formato de la imagen
    res.set("Content-Disposition", `attachment; filename="${fileName}"`);
    res.send(imageBuffer);
  } catch (error) {
    console.error("Error downloading image:", error.message);
    res.status(500).json({ message: "Failed to download image." });
  }
});

export default router;
