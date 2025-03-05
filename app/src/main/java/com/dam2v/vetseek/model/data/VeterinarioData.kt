package com.dam2v.vetseek.model.data

data class VeterinarioData(
    val id: String,
    val nombre: String,
    val direccion: String?,
    val rating: Double,
    val abierto: Boolean,
    val telefono: String,
    val latitud: Double,
    val longitud: Double,
    val distancia: Double = 0.0,
    val fotosUrl: List<String> = emptyList()
)

