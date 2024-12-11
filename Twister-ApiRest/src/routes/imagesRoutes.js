import express from "express";
import multer from "multer"; // Importa multer para manejar la carga de archivos
import { saveImageToRedis, getImageFromRedis, getImageMetadata, deleteImageFromRedis } from "../services/imageService.js";
import { handleupdateTwist } from "../services/twistService.js";
import { getUserWithToken } from "../services/authService.js";



const router = express.Router();
const upload = multer();

// Ruta para subir imágenes
router.post("/upload", upload.single("image"), async (req, res) => {
  try {
    const userId = await getUserWithToken(req, res);

    if (!userId || userId === -1) {
        return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
    }
    
    console.log("Uploading image:", req.file.originalname);
    const imageBuffer = req.file.buffer; // Obtén el buffer de la imagen
    const fileName = req.file.originalname; // Obtén el nombre original del archivo

    const urlId = await saveImageToRedis(fileName, imageBuffer);

    console.log("Image uploaded successfully, you should change it:", urlId);

    return res.status(201).json({ message: "Image uploaded successfully", urlId: urlId });
  } catch (error) {
    console.error("Error uploading image:", error.message);
    //res.status(500).json({ message: "Failed to upload image." });
  }
});

// Ruta para descargar imágenes
router.get("/download/:fileName", async (req, res) => {
  const userId = await getUserWithToken(req, res);

    if (!userId || userId === -1) {
        return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
    }
    
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
    //res.status(500).json({ message: "Failed to download image." });
  }
});


// Ruta para verificar la actualización de una imagen con encabezados HTTP
router.head("/check/:imageUri", async (req, res) => {
  try {
    const userId = await getUserWithToken(req, res);

    if (!userId || userId === -1) {
        return res.status(401).json({ message: "Unauthorized" }); // Respuesta para usuario no autenticado
    }

    console.log("Checking image update:", req.params.imageUri);
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
    //res.status(500).json({ message: "Failed to check image update." });
  }
});


router.delete("/delete", async (req, res) => {
  try {
    console.log("Received DELETE request"); // Agregar este log para verificar que la solicitud se está recibiendo

    const userId = await getUserWithToken(req, res);
    console.log("User ID after token verification:", userId); // Verifica el userId

    if (!userId || userId === -1) {
      console.log("Unauthorized access"); // Log para acceso no autorizado
      return res.status(401).json({ message: "Unauthorized" });
    }

    const twist = req.body;
    const imageId = twist.imageUri; 
    console.log("Deleting image:", imageId);

    // Aquí deberías tener la lógica para eliminar la imagen de Redis, pero ahora la omites
    // const result = await deleteImageFromRedis(imageId); // Si decides usar esto, asegúrate de logear el resultado

    const twistData = {
      ...twist,
      imageUri: null,
    };
    console.log("Updating twist with new data:", twistData);
    
    await handleupdateTwist(userId, twistData);
    
    return res.status(200).json({ message: "Image deleted successfully" });
  } catch (error) {
    console.error("Error deleting image:", error.message);
    res.status(500).json({ message: "Failed to delete image." });
  }
});



export default router;
