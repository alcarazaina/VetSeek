package com.dam2v.vetseek.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.dam2v.vetseek.R
import com.dam2v.vetseek.model.data.VeterinarioData
import com.dam2v.vetseek.view.ui.theme.Marilloso
import com.dam2v.vetseek.viewmodel.BusquedaViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleVeterinario(
    navController: NavController,
    veterinarioId: String,
    viewModel: BusquedaViewModel
) {
    // Recolectar el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Encontrar el veterinario por ID
    val veterinario = uiState.veterinarios.find { it.id == veterinarioId }
    val backgroundImage = ImageBitmap.imageResource(id = R.drawable.fondo)
    val context = LocalContext.current

    // Cargar detalles adicionales cuando se abre la pantalla
    LaunchedEffect(veterinarioId) {
        viewModel.obtenerDetallesVeterinario(veterinarioId)
    }

    // Crear un ImageLoader optimizado
    val imageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25) // Usar 25% de la memoria disponible
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02) // 2% del espacio en disco
                .build()
        }
        .respectCacheHeaders(false) // Ignorar cabeceras de caché del servidor
        .build()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val imageWidth = backgroundImage.width.toFloat()
            val imageHeight = backgroundImage.height.toFloat()

            for (x in 0..(size.width / imageWidth).toInt()) {
                for (y in 0..(size.height / imageHeight).toInt()) {
                    drawImage(
                        image = backgroundImage,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x * imageWidth,
                            y * imageHeight
                        )
                    )
                }
            }
        }

        // Scaffold y contenido
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = veterinario?.nombre ?: stringResource(R.string.detalles),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Marilloso,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Marilloso,
                    contentColor = Color.White
                ) {
                    Text(
                        text = stringResource(R.string.textobottom3),
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(color = Color.White),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (veterinario != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Tarjeta de información
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = veterinario.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Marilloso
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            veterinario.direccion?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val gmmIntentUri = Uri.parse("geo:${veterinario.latitud},${veterinario.longitud}?q=${Uri.encode(veterinario.nombre)}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Dirección",
                                        tint = Marilloso,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = it,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = if (veterinario.rating > 0) "${veterinario.rating}" else stringResource(R.string.sinvaloraciones),
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = if (veterinario.abierto) stringResource(R.string.abierto) else stringResource(
                                        R.string.cerrado
                                    ),
                                    fontSize = 14.sp,
                                    color = if (veterinario.abierto) Color.Green else Color.Red
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (veterinario.telefono.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${veterinario.telefono}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Llamar",
                                        tint = Marilloso,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = veterinario.telefono,
                                        fontSize = 14.sp,
                                        color = Marilloso
                                    )
                                }
                            }
                        }
                    }

                    // Título para las imágenes
                    Text(
                        text = stringResource(R.string.imagenes_disponibles),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Marilloso,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Grid de imágenes
                    if (veterinario.fotosUrl.isNotEmpty()) {
                        Text(
                            text = "${veterinario.fotosUrl.size} ${stringResource(R.string.imagenes_encontradas)}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(veterinario.fotosUrl) { fotoUrl ->
                                Card(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .aspectRatio(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.9f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Indicador de carga
                                        CircularProgressIndicator(
                                            color = Marilloso,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        // Imagen
                                        AsyncImage(
                                            model = fotoUrl,
                                            contentDescription = "Foto de ${veterinario.nombre}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            imageLoader = imageLoader,
                                            onLoading = { /* Mostrar indicador de carga */ },
                                            onSuccess = { /* Ocultar indicador de carga */ },
                                            onError = { /* Mostrar icono de error */ }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.no_hay_imagenes),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                // Mensaje de error si no se encuentra el veterinario
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.veterinario_no_encontrado),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

