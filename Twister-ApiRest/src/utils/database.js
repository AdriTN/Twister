import mysql from 'mysql2/promise';

let connection;

const initDB = async () => {
  try {
    // Crear conexión a MySQL
    connection = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: '123patata',
    });

    console.log('Conexión establecida correctamente.');

    // Comprobar si la base de datos existe, si no, crearla
    await connection.query(`CREATE DATABASE IF NOT EXISTS TwisterDB`);
    console.log('Base de datos verificada/creada.');

    // Usar la base de datos
    await connection.changeUser({ database: 'TwisterDB' });

    // Crear tablas si no existen
    await createTables();

  } catch (error) {
    console.error('Error al inicializar la base de datos:', error);
    process.exit(1); // Detener la aplicación si falla la conexión
  }
};

const createTables = async () => {
  try {
    // Crear tabla `users`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      )
    `);

    // Crear tabla `quizzes`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS quizzes (
        id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(100) NOT NULL,
        description TEXT,
        creator_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
      )
    `);

    // Crear tabla `questions`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS questions (
        id INT AUTO_INCREMENT PRIMARY KEY,
        quiz_id INT NOT NULL,
        question_text TEXT NOT NULL,
        question_type ENUM('multiple_choice', 'true_false', 'short_answer') NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
      )
    `);

    // Crear tabla `options`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS options (
        id INT AUTO_INCREMENT PRIMARY KEY,
        question_id INT NOT NULL,
        option_text TEXT NOT NULL,
        is_correct TINYINT(1) NOT NULL DEFAULT 0,
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
      )
    `);

    // Crear tabla `results`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS results (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        quiz_id INT NOT NULL,
        score FLOAT NOT NULL,
        completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
      )
    `);

    console.log('Tablas verificadas/creadas correctamente.');
  } catch (error) {
    console.error('Error al crear las tablas:', error);
    throw error;
  }
};

// Función GET (Lectura)
export const get = async (sql, params = []) => {
  try {
    const [rows] = await connection.execute(sql, params);
    return rows;
  } catch (error) {
    console.error('Error en la consulta GET:', error);
    throw error;
  }
};

// Función POST (Escritura)
export const post = async (sql, params = []) => {
  try {
    const [result] = await connection.execute(sql, params);
    return result;
  } catch (error) {
    console.error('Error en la consulta POST:', error);
    throw error;
  }
};

// Exportar la inicialización y conexión para otros módulos
export { initDB, connection };
