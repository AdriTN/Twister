const jwt = require('jsonwebtoken');

export async function verifyToken(req, res, next) {
    // Extraer el token del encabezado Authorization
    const token = req.headers.authorization?.split(" ")[1]; // Extract token from header

    console.log("Verifying token:", token);

    // Verificar si no existe un token
    if (!token) {
        return res.status(401).json({ message: "No token provided" });
    }

    try {
        // Verificar el token con la clave secreta
        const decoded = jwt.verify(token, "SECRET_KEY"); // Replace "SECRET_KEY" with your actual secret key
        
        // Si el token es válido, añadir el decoded info (como el user ID) a la solicitud
        req.user = decoded;
        
        // Continuar con la siguiente función (middleware o controlador)
        next();

    } catch (error) {
        console.error("Token verification failed:", error.message);
        return res.status(403).json({ message: "Invalid or expired token" });
    }
}
const jwt = require('jsonwebtoken');

export async function getUserWithToken(req, res) {
    // Extraer el token del encabezado Authorization
    const token = req.headers.authorization?.split(" ")[1]; // Extract token from header

    console.log("Verifying token:", token);

    // Verificar si no existe un token
    if (!token) {
        return res.status(401).json({ message: "No token provided" });
    }

    try {
        if (checkUser(token)) return true;
        throw new Error("User not found");
    } catch (error) {
        console.error("Token verification failed:", error.message);
        return res.status(403).json({ message: "Invalid or expired token" });
    }
}

