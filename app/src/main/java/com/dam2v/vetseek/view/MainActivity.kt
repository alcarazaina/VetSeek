package com.dam2v.vetseek.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dam2v.vetseek.view.ui.theme.VetSeekTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dam2v.vetseek.viewmodel.BusquedaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetSeekTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val busquedaViewModel: BusquedaViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "inicio") {
                        composable("inicio") { PantallaInicio(navController) }
                        composable("menu") { PantallaMenu(navController) }
                        composable("busqueda") { PantallaBusqueda(navController, busquedaViewModel) }
                        composable("detalle_veterinario/{veterinarioId}") { backStackEntry ->
                            val veterinarioId = backStackEntry.arguments?.getString("veterinarioId") ?: ""
                            PantallaDetalleVeterinario(
                                navController = navController,
                                veterinarioId = veterinarioId,
                                viewModel = busquedaViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

