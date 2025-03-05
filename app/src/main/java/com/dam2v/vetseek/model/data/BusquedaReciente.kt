package com.dam2v.vetseek.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "busquedas_recientes")
data class BusquedaReciente(
    @PrimaryKey val texto: String,
    val timestamp: Date = Date() // Para ordenar por más reciente
)
