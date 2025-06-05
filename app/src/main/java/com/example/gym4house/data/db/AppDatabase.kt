package com.example.gym4house.data.db // ¡IMPORTANTE! Asegúrate de que el paquete sea correcto

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gym4house.data.dao.UsuarioDao // Importa tu UsuarioDao
import com.example.gym4house.data.dao.PerfilClienteDao // Importa tu PerfilClienteDao
import com.example.gym4house.data.entity.Usuario // Importa tu entidad Usuario
import com.example.gym4house.data.entity.PerfilCliente // Importa tu entidad PerfilCliente

// La anotación @Database es crucial. Aquí defines:
// 1. entities: Una lista de todas las clases de entidad (tablas) que pertenecen a esta base de datos.
// 2. version: El número de versión de tu base de datos. ¡Debe incrementarse cada vez que cambies el esquema!
// 3. exportSchema: Recomendado en 'false' para proyectos pequeños/iniciales.
@Database(
    entities = [Usuario::class, PerfilCliente::class], // Lista de tus entidades
    version = 1, // La primera versión de tu base de datos
    exportSchema = false // No exporta el esquema a una carpeta, útil en producción
)
abstract class AppDatabase : RoomDatabase() {

    // Declara los métodos abstractos para obtener los DAOs.
    // Room implementará estos métodos para ti.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun perfilClienteDao(): PerfilClienteDao

    companion object {
        // La instancia de la base de datos, Singleton para evitar múltiples instancias.
        @Volatile // Hace que la instancia sea visible inmediatamente para otros hilos.
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la devuelve.
            // Si no, crea una nueva instancia de forma sincronizada para evitar condiciones de carrera.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Usa el contexto de la aplicación para evitar fugas de memoria
                    AppDatabase::class.java,
                    "lifegym_database" // Nombre del archivo de la base de datos SQLite en el dispositivo
                )
                    // .addMigrations(MIGRATION_1_2) // Si hubiera migraciones en el futuro
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}