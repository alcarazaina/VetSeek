package com.dam2v.vetseek.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dam2v.vetseek.view.ui.theme.Marilloso

@Composable
fun PantallaInicio(navController: NavController, viewModel: InicioViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.shouldNavigateToMenu) {
        navController.navigate("menu")
        viewModel.onNavigatedToMenu()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de la aplicación",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.onComenzarClick() },
            modifier = Modifier,
            colors = ButtonDefaults.buttonColors(Marilloso)
        ) {
            Text(stringResource(R.string.comenzar),
                style = TextStyle(Color.White)
            )
        }
    }
}
