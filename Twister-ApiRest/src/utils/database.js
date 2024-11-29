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

    // Insertar datos iniciales
    await seedData();

  } catch (error) {
    console.error('Error al inicializar la base de datos:', error);
    process.exit(1); // Detener la aplicación si falla la conexión
  }
};

const createTables = async () => {
  try {
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

    await connection.query(`
      CREATE TABLE IF NOT EXISTS options (
        id INT AUTO_INCREMENT PRIMARY KEY,
        question_id INT NOT NULL,
        option_text TEXT NOT NULL,
        is_correct TINYINT(1) NOT NULL DEFAULT 0,
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
      )
    `);

    console.log('Tablas verificadas/creadas correctamente.');
  } catch (error) {
    console.error('Error al crear las tablas:', error);
    throw error;
  }
};

const seedData = async () => {
  try {
    // Crear un usuario inicial
    const [userResult] = await connection.query(
      `INSERT IGNORE INTO users (username, email, password_hash) VALUES (?, ?, ?)`,
      ['admin', 'admin@example.com', 'hashed_password']
    );
    const userId = userResult.insertId || 1; // Por defecto, usar ID 1 si ya existe

    // Crear un quiz inicial
    const [quizResult] = await connection.query(
      `INSERT IGNORE INTO quizzes (title, description, creator_id) VALUES (?, ?, ?)`,
      ['Lengua Castellana y Literatura', 'Preguntas sobre gramática y literatura española.', userId]
    );
    const quizId = quizResult.insertId || 1;

    // Añadir preguntas al quiz
    const [question1] = await connection.query(
      `INSERT IGNORE INTO questions (quiz_id, question_text, question_type) VALUES (?, ?, ?)`,
      [quizId, '¿Quién escribió *Don Quijote de la Mancha*?', 'multiple_choice']
    );
    const question1Id = question1.insertId;

    await connection.query(
      `INSERT IGNORE INTO options (question_id, option_text, is_correct) VALUES 
        (?, 'Miguel de Cervantes', 1),
        (?, 'Federico García Lorca', 0),
        (?, 'Gabriel García Márquez', 0),
        (?, 'Pablo Neruda', 0)`,
      [question1Id, question1Id, question1Id, question1Id]
    );

    const [question2] = await connection.query(
      `INSERT IGNORE INTO questions (quiz_id, question_text, question_type) VALUES (?, ?, ?)`,
      [quizId, 'El verbo en "Ellos *habían hablado*" está en:', 'multiple_choice']
    );
    const question2Id = question2.insertId;

    await connection.query(
      `INSERT IGNORE INTO options (question_id, option_text, is_correct) VALUES 
        (?, 'Pretérito perfecto compuesto', 0),
        (?, 'Pretérito pluscuamperfecto', 1),
        (?, 'Pretérito imperfecto', 0),
        (?, 'Futuro perfecto', 0)`,
      [question2Id, question2Id, question2Id, question2Id]
    );

    console.log('Datos iniciales insertados correctamente.');
  } catch (error) {
    console.error('Error al insertar los datos iniciales:', error);
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
