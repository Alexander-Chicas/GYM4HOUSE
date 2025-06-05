package com.example.gym4house.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfiles_clientes")
data class PerfilCliente( // <--- ¡Asegúrate de que diga 'data class' aquí!
    @PrimaryKey(autoGenerate = true)
    val id_perfil_cliente: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long, // FK a Usuarios.id_usuario

    @ColumnInfo(name = "peso_inicial_kg")
    val pesoInicialKg: Double,

    @ColumnInfo(name = "altura_cm")
    val alturaCm: Int,

    @ColumnInfo(name = "tipo_cuerpo")
    val tipoCuerpo: String,

    @ColumnInfo(name = "nivel_experiencia")
    val nivelExperiencia: String,

    @ColumnInfo(name = "genero")
    val genero: String,

    @ColumnInfo(name = "fecha_nacimiento")
    val fechaNacimiento: Long,

    @ColumnInfo(name = "preferencias_ejercicio")
    val preferenciasEjercicio: String? = null,

    @ColumnInfo(name = "preferencias_notificacion")
    val preferenciasNotificacion: Boolean = true,

    @ColumnInfo(name = "dias_entrenamiento_semana")
    val diasEntrenamientoSemana: Int,

    @ColumnInfo(name = "horas_entrenamiento_preferidas")
    val horasEntrenamientoPreferidas: String? = null
)