import express from "express";
import { verifyToken } from "../services/authService.js";
import { generateAnonToken, registerUser, loginUser } from "../models/userModel.js";

const router = express.Router();

router.post("/verify", (req, res) => {
  verifyToken(req, res);
  res.status(200).json({ message: "Token valid" });
});

router.post("/login/anonymous", (req, res) => {
  try {
    console.log("Creating anonymous token...");
    const decoded = generateAnonToken();
    res.status(200).json({ message: "Token created", token: decoded });
  } catch (error) {
    res.status(401).json({ message: "Invalid token" });
  }
});

router.post("/register", async (req, res) => {
  const { username, email, password } = req.body;
  console.log("New user registration requested");

  try {
    if (!username || !email || !password) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    const token = await registerUser(username, email, password);
    res.status(201).json({ message: "User successfully registered", token });
  } catch (error) {
    console.error(error.message);
    res.status(401).json({ error: error.message });
  }
});

router.post("/login", async (req, res) => {
  const { email, password } = req.body;

  try {
    const { jwtToken, username } = await loginUser(email, password);
    res.status(200).json({
      message: "Login successful",
      token: jwtToken,
      username,
    });
  } catch (error) {
    console.log("This is the error:", error.message);
    console.error(error.code);

    switch (error.code) {
      case 1:
        res.status(400).json({ error: "Please provide both email and password." });
        break;
      case 2:
        res.status(400).json({ error: "The email format is invalid. Please check and try again." });
        break;
      case 3:
        res.status(401).json({ error: "Incorrect username or password. Please try again." });
        break;
      default:
        res.status(500).json({ error: "Error processing login request. Please try again later." });
    }
  }
});

export default router;
