import mysql from 'mysql2/promise';

let connection;

const initDB = async () => {
  try {
    connection = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: '123patata',  // Asegúrate de usar tu contraseña real
      database: 'TwisterDB',
    });
    console.log('Base de datos conectada correctamente');
  } catch (error) {
    console.error('Error al conectar con la base de datos:', error);
    process.exit(1); // Detener la app si falla la conexión
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

// Exportar la conexión para usarla en otros módulos
export { initDB, connection };
