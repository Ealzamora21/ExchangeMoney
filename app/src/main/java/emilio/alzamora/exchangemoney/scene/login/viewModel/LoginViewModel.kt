// Indicamos el paquete al que pertenece nuestro ViewModel.
package emilio.alzamora.exchangemoney.scene.login.viewModel

// Importamos LiveData para observar los datos.
import androidx.lifecycle.LiveData

// Importamos MutableLiveData para modificar los datos.
import androidx.lifecycle.MutableLiveData

// Importamos ViewModel para manejar la lógica del Login.
import androidx.lifecycle.ViewModel

// Importamos nuestro modelo con los datos del usuario.
import emilio.alzamora.exchangemoney.scene.login.model.LoginModel

// Importamos la herramienta para verificar las contraseñas.
import emilio.alzamora.exchangemoney.storage.PasswordUtils

// Importamos Patterns para validar el correo electrónico.
import android.util.Patterns

// Clase que contiene la lógica del inicio de sesión.
class LoginViewModel : ViewModel() {

    // 1. Almacenamos el resultado del inicio de sesión.
    private val _loginResult = MutableLiveData<Boolean>()

    // Permitimos que la Activity observe el resultado.
    val loginResult: LiveData<Boolean> = _loginResult

    // 2. Almacenamos los mensajes de error.
    private val _errorMessage = MutableLiveData<String>()

    // Permitimos que la Activity observe los errores.
    val errorMessage: LiveData<String> = _errorMessage

    // 3. Indicamos si estamos verificando las credenciales.
    private val _isLoading = MutableLiveData<Boolean>(false)

    // Permitimos observar el estado de carga.
    val isLoading: LiveData<Boolean> = _isLoading

    // 4. Recibimos los datos ingresados y los datos guardados.
    fun login(
        user: String,
        password: String,
        savedEmail: String,
        savedPassword: String
    ) {

        // Evitamos iniciar otra validación al mismo tiempo.
        if (_isLoading.value == true) {
            return
        }

        // 5. Creamos nuestro modelo con los datos ingresados.
        val loginModel = LoginModel(
            user,
            password
        )

        // 6. Verificamos que los campos no estén vacíos.
        if (loginModel.user.isBlank() ||
            loginModel.password.isBlank()
        ) {

            // Informamos que faltan datos.
            _errorMessage.value = "Completa todos los campos"

            // Detenemos el inicio de sesión.
            return
        }

        // 7. Verificamos el formato del correo electrónico.
        if (!Patterns.EMAIL_ADDRESS.matcher(loginModel.user).matches()) {

            // Informamos que el correo no es válido.
            _errorMessage.value = "Ingresa un correo electrónico válido"

            return
        }

        // 8. Comprobamos la longitud de la contraseña.
        if (loginModel.password.length < 6) {

            // Informamos la longitud mínima.
            _errorMessage.value =
                "La contraseña debe tener al menos 6 caracteres"

            return
        }

        // 9. Verificamos si existe una cuenta registrada.
        if (savedEmail.isBlank() || savedPassword.isBlank()) {

            // Informamos que primero debe registrarse.
            _errorMessage.value = "No existe una cuenta registrada"

            return
        }

        // 10. Comparamos el correo ingresado con el registrado.
        if (!loginModel.user.equals(savedEmail, ignoreCase = true)) {

            // Indicamos que las credenciales no son correctas.
            _loginResult.value = false

            return
        }

        // Activamos el estado de carga.
        _isLoading.value = true

        // 11. Verificamos la contraseña en un hilo secundario.
        // Esto evita bloquear la interfaz durante el cálculo del hash.
        Thread {

            val isValid = try {

                // Comprobamos si la contraseña utiliza PBKDF2.
                if (savedPassword.startsWith("PBKDF2:")) {

                    // Verificamos la contraseña protegida.
                    PasswordUtils.verifyPassword(
                        loginModel.password,
                        savedPassword
                    )

                } else {

                    // Mantenemos la compatibilidad con cuentas antiguas.
                    loginModel.password == savedPassword
                }

            } catch (e: Exception) {

                // Si ocurre un error, rechazamos el inicio de sesión.
                false
            }

            // 12. Enviamos el resultado a la Activity.
            _loginResult.postValue(isValid)

            // Desactivamos el estado de carga.
            _isLoading.postValue(false)

        }.start()
    }
}