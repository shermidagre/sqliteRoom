package com.example.sqliteroom.controller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sqliteroom.db.AppDatabase
import com.example.sqliteroom.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserController(application: Application) : AndroidViewModel(application) {

    // Usamos el Singleton que creamos antes.
    // Al ser 'private', nadie fuera sabe de dónde sacamos los datos. Encapsulación pura.
    private val database = AppDatabase.Companion.getDatabase(application)
    private val userDao = database.userDao()

    // 3. El Estado (StateFlow) - Igual que antes
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = userDao.getAll()
            Log.d("USUARIO", "Usuarios en lista: ${_users.value}")
        }
    }

    fun addUser(firstName: String, lastName: String) {
        viewModelScope.launch {
            val newUser = User(uid = System.currentTimeMillis().toInt(), firstName, lastName)
            userDao.insertAll(newUser)
            loadUsers()
            Log.d("USUARIO", "Usuario insertado: $newUser")
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.delete(user)
            loadUsers()
            Log.d("USUARIO", "Usuario eliminado: $user")

        }
    }
}