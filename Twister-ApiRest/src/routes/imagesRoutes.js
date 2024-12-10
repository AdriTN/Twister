import express from "express";
import multer from "multer"; // Importa multer para manejar la carga de archivos
import { getUserWithToken } from "../services/authService.js";
import { saveImageToRedis, getImageFromRedis, getImageMetadata } from "../services/imageService.js";

const router = express.Router();
const upload = multer();

// Ruta para subir imágenes
router.post("/upload", upload.single("image"), async (req, res) => {
  try {
    console.log("Uploading image:", req.file.originalname);
    const imageBuffer = req.file.buffer; // Obtén el buffer de la imagen
    const fileName = req.file.originalname; // Obtén el nombre original del archivo

    const urlId = await saveImageToRedis(fileName, imageBuffer);

    return res.status(201).json({ message: "Image uploaded successfully", urlId: urlId });
  } catch (error) {
    console.error("Error uploading image:", error.message);
    res.status(500).json({ message: "Failed to upload image." });
  }
});

// Ruta para descargar imágenes
router.get("/download/:fileName", async (req, res) => {
  try {
    const fileName = req.params.fileName;
    console.log("Downloading image:", fileName);
    const imageBuffer = await getImageFromRedis(fileName); // Recupera la imagen desde Redis sin guardar

    if (!imageBuffer) {
      return res.status(404).json({ message: "Image not found" });
    }

    // Establece los encabezados para la respuesta
    res.set("Content-Type", "image/jpeg"); // Cambia el tipo según el formato de la imagen
    res.set("Content-Disposition", `attachment; filename="${fileName}"`);
    res.send(imageBuffer); // Envía el buffer de imagen como respuesta
  } catch (error) {
    console.error("Error downloading image:", error.message);
    res.status(500).json({ message: "Failed to download image." });
  }
});


// Ruta para verificar la actualización de una imagen con encabezados HTTP
router.head("/check/:imageUri", async (req, res) => {
  try {
    const imageUri = req.params.imageUri;
    const ifModifiedSince = req.headers["if-modified-since"];

    const metadata = await getImageMetadata(imageUri); // Obtén los metadatos, incluyendo la última modificación

    if (!metadata) {
      return res.status(404).json({ message: "Image metadata not found" });
    }

    const lastModified = new Date(metadata.lastModified).getTime();

    if (ifModifiedSince && new Date(ifModifiedSince).getTime() >= lastModified) {
      return res.status(304).end(); // Not Modified
    }

    res.set("Last-Modified", metadata.lastModified);
    res.status(200).end();
  } catch (error) {
    console.error("Error checking image update:", error.message);
    res.status(500).json({ message: "Failed to check image update." });
  }
});

export default router;
