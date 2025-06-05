package com.example.gym4house.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gym4house.data.entity.PerfilCliente

@Dao
interface PerfilClienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfilCliente(perfilCliente: PerfilCliente): Long

    @Update
    suspend fun updatePerfilCliente(perfilCliente: PerfilCliente)

    @Query("SELECT * FROM perfiles_clientes WHERE id_usuario = :idUsuario")
    suspend fun getPerfilClienteByUserId(idUsuario: Long): PerfilCliente?

    @Query("SELECT * FROM perfiles_clientes WHERE id_perfil_cliente = :idPerfilCliente")
    suspend fun getPerfilClienteById(idPerfilCliente: Long): PerfilCliente?
}