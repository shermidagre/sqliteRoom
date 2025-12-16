package com.example.sqliteroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Necesario
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // <--- IMPORTANTE: Necesitas este import para que funcione 'items'
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.sqliteroom.ui.theme.SqliteRoomTheme
import com.example.sqliteroom.controller.UserController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: UserController by viewModels()

        enableEdgeToEdge()
        setContent {
            SqliteRoomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaUsuario(viewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// En la función Composable, también debes especificar el tipo correcto
@Composable
fun PantallaUsuario(
    viewModel: UserController,
    modifier: Modifier = Modifier
) {

    val userList by viewModel.users.collectAsState()

    Column(modifier = modifier) {
        Button(onClick = {
            viewModel.addUser("el diablo", "mami")
        }) {
            Text("Añadir Usuario")
        }

        LazyColumn {
            items(userList) { user ->
                Text("Usuario: ${user.firstName} ${user.lastName}")
            }
        }
    }
}