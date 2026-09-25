// Paquete donde manejamos el almacenamiento local.
package emilio.alzamora.exchangemoney.storage

// Importamos Context para acceder a los recursos de Android.
import android.content.Context

// Importamos SharedPreferences para guardar los datos.
import android.content.SharedPreferences
import androidx.core.content.edit

// Importamos Gson para convertir listas a formato JSON.
import com.google.gson.Gson

// Importamos TypeToken para recuperar listas desde JSON.
import com.google.gson.reflect.TypeToken

// Clase encargada de guardar y recuperar la información.
class ConcreteSharedPreference(
    context: Context,
    preferencesName: String = "ExchangeMoneyPreferences"
) {

    // 1. Creamos el archivo de almacenamiento de nuestra aplicación.
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

    // 2. Inicializamos Gson para manejar los datos del historial.
    private val gson = Gson()

    // --------------------------------------------------
    // DATOS DEL USUARIO
    // --------------------------------------------------

    // 3. Guardamos los datos del usuario registrado.
    fun saveUser(name: String, email: String, password: String) {

        // Protegemos la contraseña antes de guardarla.
        val passwordHash = PasswordUtils.hashPassword(password)

        // Creamos un editor para modificar los datos.
        sharedPreferences.edit {

            // Guardamos el nombre del usuario.
            putString("USER_NAME", name)

            // Guardamos el correo electrónico.
            putString("USER_EMAIL", email)

            // Guardamos el hash en lugar de la contraseña original.
            putString("USER_PASSWORD_HASH", passwordHash)

            // Eliminamos cualquier contraseña anterior en texto plano.
            remove("USER_PASSWORD")
        }
    }

    // 4. Recuperamos el nombre del usuario.
    fun getName(): String {

        // Devolvemos el nombre guardado o un texto vacío.
        return sharedPreferences.getString(
            "USER_NAME",
            ""
        ) ?: ""
    }

    // 5. Recuperamos el correo electrónico.
    fun getEmail(): String {

        // Devolvemos el correo guardado o un texto vacío.
        return sharedPreferences.getString(
            "USER_EMAIL",
            ""
        ) ?: ""
    }

    // 6. Recuperamos la información de la contraseña guardada.
    fun getPassword(): String {

        // Buscamos primero la contraseña protegida.
        val passwordHash = sharedPreferences.getString(
            "USER_PASSWORD_HASH",
            null
        )

        // Si existe un hash, devolvemos ese valor.
        if (passwordHash != null) {
            return passwordHash
        }

        // Recuperamos la contraseña anterior para permitir su migración.
        return sharedPreferences.getString(
            "USER_PASSWORD",
            ""
        ) ?: ""
    }

    // 7. Comprobamos si existe una cuenta registrada.
    fun hasRegisteredUser(): Boolean {

        // Verificamos si tenemos un correo guardado.
        return getEmail().isNotBlank()
    }

    // 8. Migramos las contraseñas antiguas al formato protegido.
    fun migratePassword(password: String) {

        // Comprobamos si ya existe una contraseña protegida.
        if (sharedPreferences.contains("USER_PASSWORD_HASH")) {

            // Detenemos la función si ya fue migrada.
            return
        }

        // Recuperamos la contraseña antigua.
        val oldPassword = sharedPreferences.getString(
            "USER_PASSWORD",
            ""
        ) ?: ""

        // Verificamos que la contraseña ingresada sea correcta.
        if (oldPassword.isBlank() || oldPassword != password) {

            // Evitamos modificar la contraseña si no coincide.
            return
        }

        // Generamos el hash de la contraseña existente.
        val passwordHash = PasswordUtils.hashPassword(password)

        // Creamos un editor para actualizar el almacenamiento.
        sharedPreferences.edit {

            // Guardamos la contraseña protegida.
            putString("USER_PASSWORD_HASH", passwordHash)

            // Eliminamos la contraseña almacenada en texto plano.
            remove("USER_PASSWORD")
        }
    }

    // --------------------------------------------------
    // SESIÓN DEL USUARIO
    // --------------------------------------------------

    // 9. Guardamos el estado de la sesión.
    fun saveSession(isLoggedIn: Boolean) {

        // Creamos un editor para modificar los datos.
        sharedPreferences.edit {

            // Guardamos el estado actual de la sesión.
            putBoolean("IS_LOGGED_IN", isLoggedIn)
        }
    }

    // 10. Comprobamos si existe una sesión activa.
    fun isLoggedIn(): Boolean {

        // Devolvemos false si no existe una sesión guardada.
        return sharedPreferences.getBoolean(
            "IS_LOGGED_IN",
            false
        )
    }

    // 11. Cerramos la sesión del usuario.
    fun logout() {

        // Desactivamos la sesión sin eliminar la cuenta.
        saveSession(false)
    }

    // --------------------------------------------------
    // HISTORIAL DE CONVERSIONES
    // --------------------------------------------------

    // 12. Guardamos una nueva conversión en el historial.
    fun saveConversion(conversion: String) {

        // Recuperamos las conversiones guardadas anteriormente.
        val currentHistory =
            getConversionHistory().toMutableList()

        // Agregamos la nueva conversión al inicio de la lista.
        currentHistory.add(0, conversion)

        // Convertimos la lista de conversiones a formato JSON.
        val historyJson = gson.toJson(currentHistory)

        // Creamos un editor para modificar SharedPreferences.
        sharedPreferences.edit {

            // Guardamos el historial actualizado.
            putString(
                "CONVERSION_HISTORY_JSON",
                historyJson
            )

            // Eliminamos el formato anterior del historial.
            remove("CONVERSION_HISTORY")
        }
    }

    // 13. Recuperamos las conversiones guardadas.
    fun getConversionHistory(): List<String> {

        // Recuperamos el historial almacenado en formato JSON.
        val historyJson = sharedPreferences.getString(
            "CONVERSION_HISTORY_JSON",
            null
        )

        // Comprobamos si existe un historial en formato JSON.
        if (historyJson != null) {

            // Intentamos recuperar la lista guardada.
            try {

                // Indicamos a Gson el tipo de información esperado.
                val type =
                    object : TypeToken<List<String>>() {}.type

                // Convertimos el JSON nuevamente en una lista.
                return gson.fromJson<List<String>>(
                    historyJson,
                    type
                ) ?: emptyList()

            } catch (e: Exception) {

                // Devolvemos una lista vacía si los datos son incorrectos.
                return emptyList()
            }
        }

        // Recuperamos las conversiones del formato anterior.
        val oldHistory = sharedPreferences.getStringSet(
            "CONVERSION_HISTORY",
            emptySet()
        ) ?: emptySet()

        // Convertimos el conjunto anterior en una lista.
        return oldHistory.toList()
    }

    // 14. Eliminamos todas las conversiones guardadas.
    fun clearConversionHistory() {

        // Creamos un editor para modificar SharedPreferences.
        sharedPreferences.edit {

            // Eliminamos el historial almacenado en formato JSON.
            remove("CONVERSION_HISTORY_JSON")

            // Eliminamos también el formato anterior del historial.
            remove("CONVERSION_HISTORY")
        }
    }
}