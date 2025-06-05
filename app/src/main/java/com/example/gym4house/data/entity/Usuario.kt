package com.example.gym4house.data.entity // ¡IMPORTANTE! Asegúrate de que este paquete sea correcto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id_usuario: Long = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "apellido")
    val apellido: String,

    @ColumnInfo(name = "correo_electronico")
    val correoElectronico: String,

    @ColumnInfo(name = "contrasena_hash")
    val contrasenaHash: String,

    @ColumnInfo(name = "rol")
    val rol: String,

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "ultimo_login")
    val ultimoLogin: Long? = null,

    @ColumnInfo(name = "foto_perfil_url")
    val fotoPerfilUrl: String? = null,

    @ColumnInfo(name = "idioma_preferido")
    val idiomaPreferido: String = "es",

    @ColumnInfo(name = "estado_cuenta")
    val estadoCuenta: String = "activo",

    // ¡¡¡CAMBIO AQUI!!!
    // Se añade fechaNacimiento al constructor
    @ColumnInfo(name = "fecha_nacimiento")
    val fechaNacimiento: Long
)