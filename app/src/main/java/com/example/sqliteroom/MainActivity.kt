package com.example.sqliteroom

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.sqliteroom.ui.theme.SqliteRoomTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializamos la db
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()

        // Lanzamos una corrutina.
        lifecycleScope.launch {
            val userDao = db.userDao()
            userDao.insertAll(User(1, "Pepe", "Botella"))

            val users: List<User> = userDao.getAll()

            Log.d("MainActivity", "Users: $users")
        }

        setContent {
            SqliteRoomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(name = "Android", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable

    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier,
            )
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

        SqliteRoomTheme {

            Greeting("Android")

        }
    }
}