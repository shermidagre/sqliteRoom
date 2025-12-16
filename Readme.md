
---
## 📚 **Documentación del Código: Implementación de Room en Android (Kotlin)**

**Aplicación:** `com.example.sqliteroom`

**Objetivo:** Demostrar la persistencia de datos moderna usando la librería **Jetpack Room**, eliminando el código repetitivo de Java y gestionando operaciones asíncronas con **Kotlin Coroutines** y **StateFlow**.

---

### 🗂️ **1. La Entidad (`User`)**

**Clase:** `User.kt` (data class anotada con `@Entity`)

**Ubicación:** `com.example.sqliteroom.entity`

**Propósito:** Definir la estructura de la tabla y el objeto de datos simultáneamente. Sustituye al antiguo patrón "Contract".

#### 📐 Estructura:
```kotlin
@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)
```

#### ✅ Características clave:
*   **Concisión:** En una sola línea (`data class`) definimos la tabla, constructor, getters, setters, `toString` y `equals`.
*   **Anotaciones:** `@Entity` define la tabla SQL. `@PrimaryKey` define la clave única.
*   **Null Safety:** Kotlin maneja tipos nulos (`String?`) directamente en el esquema de la BD.

---

### ⚙️ **2. El DAO (Data Access Object) (`UserDao`)**

**Interfaz:** `UserDao` (anotada con `@Dao`)

**Ubicación:** `com.example.sqliteroom.interfaces`

**Propósito:** Abstraer las consultas SQL. Es el punto de entrada para interactuar con la base de datos.

#### 📐 Estructura:
```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}
```

#### ✅ Características clave:
| Característica | Función | Ventaja sobre Java |
| --- | --- | --- |
| **SQL en tiempo de compilación** | Verificación automática del SQL. | Si escribes mal la consulta, el compilador (KSP) te avisa *antes* de ejecutar. |
| **`vararg`** | Argumentos variables. | Permite insertar 1 usuario o 500 en la misma llamada de forma nativa. |

---

### 🏢 **3. La Base de Datos (`AppDatabase`)**

**Clase:** `AppDatabase` (clase abstracta que extiende `RoomDatabase`)

**Ubicación:** `com.example.sqliteroom.db`

**Propósito:** Punto de acceso principal y singleton. Gestiona la conexión y sirve las instancias de los DAOs.

#### 📐 Estructura:
```kotlin
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                ).allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### ✅ Características clave:
*   **Patrón Singleton:** Gestiona una única instancia de la base de datos para toda la app.
*   **Configuración KSP:** Requiere el plugin `ksp` en `build.gradle` para generar la implementación (`AppDatabase_Impl`) automáticamente.
*   **`.allowMainThreadQueries()`:** **Solo para desarrollo/demos.** En una app real, todas las operaciones de BD deben hacerse en un hilo de fondo.

---

### 🧠 **4. La Lógica de Negocio (`UserController`)**

**Clase:** `UserController.kt` (extiende `AndroidViewModel`)

**Ubicación:** `com.example.sqliteroom.controller`

**Propósito:** Actuar como intermediario entre la UI y la base de datos. Gestiona el estado de la lista de usuarios y la lógica para cargar, añadir y eliminar usuarios.

#### 📐 Estructura Clave:
```kotlin
class UserController(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = userDao.getAll()
        }
    }

    fun addUser(firstName: String, lastName: String) {
        viewModelScope.launch {
            val newUser = User(uid = System.currentTimeMillis().toInt(), firstName, lastName)
            userDao.insertAll(newUser)
            loadUsers()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.delete(user)
            loadUsers()
        }
    }
}
```

#### ✅ Características clave:
*   **`AndroidViewModel`:** Sobrevive a rotaciones de pantalla.
*   **`viewModelScope`:** Lanza corrutinas que se cancelan automáticamente cuando el ViewModel es destruido, evitando fugas de memoria.
*   **`StateFlow`:** Expone el estado (lista de usuarios) a la UI de forma reactiva y segura para hilos.

---

### 🖥️ **5. La Interfaz de Usuario (`MainActivity`)**

**Archivo:** `MainActivity.kt`

**Ubicación:** `com.example.sqliteroom` (raíz del módulo)

**Propósito:** Consumir el estado del `UserController` y renderizar la UI.

#### 📐 Flujo implementado:
1.  **Inyección del ViewModel:** Se obtiene la instancia del `UserController` usando `by viewModels()`.
2.  **Observación del Estado:** La lista de usuarios se observa mediante `collectAsState()`, que actualiza la UI de forma automática cuando cambia la lista.
3.  **Interacción del Usuario:** Los botones para añadir y eliminar usuarios llaman a métodos del ViewModel.

#### ✅ Características clave:
*   **Arquitectura Moderna:** Sigue un patrón **Unidireccional (Unidirectional Data Flow)** y **Separación de Concerns**. La UI no conoce la base de datos, solo interactúa con el ViewModel.
*   **Composibilidad:** Se basa en Jetpack Compose para una construcción de UI declarativa y eficiente.

---

### ⚠️ **Advertencias y Solución de Errores**

#### 🛑 **Error Crítico Resuelto: `AppDatabase_Impl does not exist`**
Este proyecto fallaba inicialmente porque se usaba `annotationProcessor` (Java) en lugar de `ksp` (Kotlin).

**Solución aplicada en `build.gradle.kts`:**
```kotlin
plugins {
    id("com.google.devtools.ksp") // Plugin obligatorio
}

dependencies {
    ksp("androidx.room:room-compiler:2.8.4") // Usar KSP, no annotationProcessor
}
```

#### 🛠️ **Recomendaciones:**
| Tema | Recomendación |
| --- | --- |
| **🧹 Clean & Rebuild** | Si cambias el esquema de la BD (clase `User`), recuerda hacer `Build > Clean Project` para que KSP regenere el código. |
```

Esta actualización corrige la sección 4, que ya no trata sobre `MainActivity` como lugar donde se ejecuta directamente la lógica de la BD, sino que refleja correctamente tu arquitectura en capas con un controlador (`UserController`) que actúa como ViewModel. También se han ajustado las descripciones y las ubicaciones de las clases para que coincidan al 100% con tu estructura de proyecto.