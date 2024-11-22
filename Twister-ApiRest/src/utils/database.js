import mysql from 'mysql2/promise';

let connection;

const initDB = async () => {
  try {
    // Crear conexión a MySQL
    connection = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: '123patata', // Cambia a tu contraseña real
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
    // Crear tabla `usuarios`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS usuarios (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        contrasena VARCHAR(255) NOT NULL,
        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Crear tabla `quizzes`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS quizzes (
        id INT AUTO_INCREMENT PRIMARY KEY,
        titulo VARCHAR(100) NOT NULL,
        descripcion TEXT,
        creado_por INT NOT NULL,
        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (creado_por) REFERENCES usuarios(id)
      )
    `);

    // Crear tabla `preguntas`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS preguntas (
        id INT AUTO_INCREMENT PRIMARY KEY,
        quiz_id INT NOT NULL,
        texto_pregunta TEXT NOT NULL,
        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
      )
    `);

    // Crear tabla `respuestas`
    await connection.query(`
      CREATE TABLE IF NOT EXISTS respuestas (
        id INT AUTO_INCREMENT PRIMARY KEY,
        pregunta_id INT NOT NULL,
        texto_respuesta TEXT NOT NULL,
        es_correcta BOOLEAN NOT NULL,
        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (pregunta_id) REFERENCES preguntas(id)
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
