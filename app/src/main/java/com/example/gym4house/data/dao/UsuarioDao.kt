package com.example.gym4house.data.dao //

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gym4house.data.entity.Usuario
@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo_electronico = :correoElectronico")
    suspend fun getUsuarioByEmail(correoElectronico: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :idUsuario")
    suspend fun getUsuarioById(idUsuario: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE correo_electronico = :correo AND contrasena_hash = :contrasenaHash")
    suspend fun loginUsuario(correo: String, contrasenaHash: String): Usuario?
}