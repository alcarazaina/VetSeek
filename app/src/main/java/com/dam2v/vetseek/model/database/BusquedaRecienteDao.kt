package com.dam2v.vetseek.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dam2v.vetseek.model.data.BusquedaReciente
import kotlinx.coroutines.flow.Flow

@Dao
interface BusquedaRecienteDao {
    @Query("SELECT * FROM busquedas_recientes ORDER BY timestamp DESC LIMIT 10")
    fun obtenerBusquedasRecientes(): Flow<List<BusquedaReciente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarBusqueda(busqueda: BusquedaReciente)

    @Query("DELETE FROM busquedas_recientes WHERE texto = :texto")
    suspend fun eliminarBusqueda(texto: String)

    @Query("DELETE FROM busquedas_recientes")
    suspend fun eliminarTodasLasBusquedas()
}

