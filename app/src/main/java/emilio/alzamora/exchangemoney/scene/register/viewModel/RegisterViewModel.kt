// Paquete al que pertenece nuestro ViewModel.
package emilio.alzamora.exchangemoney.scene.register.viewModel

// Importamos Patterns para validar el formato del correo.
import android.util.Patterns

// Importamos LiveData para observar los resultados.
import androidx.lifecycle.LiveData

// Importamos MutableLiveData para modificar los resultados.
import androidx.lifecycle.MutableLiveData

// Importamos ViewModel para manejar la lógica del registro.
import androidx.lifecycle.ViewModel

// Importamos nuestro modelo de registro.
import emilio.alzamora.exchangemoney.scene.register.model.RegisterModel

// Clase que contiene la lógica del registro.
class RegisterViewModel : ViewModel() {

    // 1. Almacena internamente el resultado de la validación.
    private val _registerResult = MutableLiveData<Boolean>()

    // 2. La Activity puede observar el resultado.
    val registerResult: LiveData<Boolean> = _registerResult

    // 3. Almacenamos los mensajes de error.
    private val _errorMessage = MutableLiveData<String>()

    // 4. Permitimos que la Activity observe los mensajes.
    val errorMessage: LiveData<String> = _errorMessage

    // 5. Recibimos los datos ingresados por el usuario.
    fun register(name: String, email: String, password: String) {

        // Creamos el modelo con la información recibida.
        val registerModel = RegisterModel(

            // Guardamos el nombre ingresado.
            name = name,

            // Guardamos el correo ingresado.
            email = email,

            // Guardamos la contraseña ingresada.
            password = password
        )

        // 6. Verificamos que ningún campo esté vacío.
        if (registerModel.name.isBlank() ||
            registerModel.email.isBlank() ||
            registerModel.password.isBlank()
        ) {

            // Enviamos un mensaje indicando que faltan datos.
            _errorMessage.value = "Completa todos los campos"

            // Detenemos la validación.
            return
        }

        // 7. Verificamos que el correo tenga un formato válido.
        if (!Patterns.EMAIL_ADDRESS.matcher(registerModel.email).matches()) {

            // Enviamos un mensaje si el correo es incorrecto.
            _errorMessage.value = "Ingresa un correo válido"

            // Detenemos la validación.
            return
        }

        // 8. Verificamos que la contraseña tenga mínimo 6 caracteres.
        if (registerModel.password.length < 6) {

            // Informamos al usuario sobre la longitud requerida.
            _errorMessage.value =
                "La contraseña debe tener al menos 6 caracteres"

            // Detenemos la validación.
            return
        }

        // 9. Si todas las validaciones son correctas,
        // enviamos un resultado positivo a la Activity.
        _registerResult.value = true
    }
}