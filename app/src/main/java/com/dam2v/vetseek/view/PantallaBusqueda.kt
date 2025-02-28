package com.dam2v.vetseek.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dam2v.vetseek.R
import com.dam2v.vetseek.model.data.VeterinarioData
import com.dam2v.vetseek.view.ui.theme.Marilloso
import com.dam2v.vetseek.viewmodel.BusquedaViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PantallaBusqueda(navController: NavController, viewModel: BusquedaViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val backgroundImage = ImageBitmap.imageResource(id = R.drawable.fondo)

    // Inicializar el cliente de ubicación
    LaunchedEffect(Unit) {
        viewModel.inicializarLocationClient(context)
    }

    // Estado de permisos
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

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
                            text = stringResource(R.string.buscarcerca),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
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
                        text = stringResource(R.string.textobottom),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Opciones de búsqueda
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // TextField para buscar por ubicación
                        OutlinedTextField(
                            value = uiState.ubicacionBusqueda,
                            onValueChange = { viewModel.actualizarUbicacionBusqueda(it) },
                            label = { Text("Buscar veterinarios en...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Marilloso,
                                focusedLabelColor = Marilloso,
                                cursorColor = Marilloso
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para buscar con ubicación actual
                        Button(
                            onClick = {
                                when (locationPermissionState.status) {
                                    is PermissionStatus.Granted -> {
                                        viewModel.toggleUsarUbicacionActual(true)
                                        viewModel.buscarVeterinarios(context)
                                    }
                                    is PermissionStatus.Denied -> {
                                        locationPermissionState.launchPermissionRequest()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Marilloso)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación actual",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Buscar con mi ubicación actual",
                                style = TextStyle(Color.White),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para buscar con la ubicación ingresada
                        Button(
                            onClick = {
                                viewModel.toggleUsarUbicacionActual(false)
                                viewModel.buscarVeterinarios(context)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Marilloso),
                            enabled = uiState.ubicacionBusqueda.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Buscar en esta ubicación",
                                style = TextStyle(Color.White),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Estado de carga
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Marilloso
                    )
                }

                // Mensaje de error
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Lista de resultados
                if (uiState.veterinarios.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(uiState.veterinarios) { index, veterinario ->
                            VeterinarioItem(veterinario = veterinario)
                        }
                    }
                } else if (!uiState.isLoading && uiState.error == null) {
                    Text(
                        text = "Busca veterinarios cercanos usando los botones de arriba",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun VeterinarioItem(veterinario: VeterinarioData) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                // Abrir en Google Maps
                val gmmIntentUri = Uri.parse("geo:${veterinario.latitud},${veterinario.longitud}?q=${Uri.encode(veterinario.nombre)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                }
            },
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

            Text(
                text = veterinario.direccion,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

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
                    text = if (veterinario.rating > 0) "${veterinario.rating}" else "Sin valoraciones",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (veterinario.abierto) "Abierto" else "Cerrado",
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
}

