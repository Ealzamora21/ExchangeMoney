// Paquete al que pertenece nuestra Activity.
package emilio.alzamora.exchangemoney.scene.login.view

// Importamos Intent para navegar entre pantallas.
import android.content.Intent

// Importamos Bundle para el ciclo de vida de la Activity.
import android.os.Bundle

// Importamos Log para registrar eventos en Logcat.
import android.util.Log

// Importamos Toast para mostrar mensajes al usuario.
import android.widget.Toast

// Importamos AlertDialog para mostrar ventanas informativas.
import androidx.appcompat.app.AlertDialog

// Importamos la clase base de nuestra Activity.
import androidx.appcompat.app.AppCompatActivity

// Importamos ViewModelProvider para utilizar nuestro ViewModel.
import androidx.lifecycle.ViewModelProvider

// Importamos el Binding de la pantalla de Login.
import emilio.alzamora.exchangemoney.databinding.ActivityLoginBinding

// Importamos la pantalla del conversor.
import emilio.alzamora.exchangemoney.scene.converter.view.ConverterActivity

// Importamos el ViewModel del Login.
import emilio.alzamora.exchangemoney.scene.login.viewModel.LoginViewModel

// Importamos la pantalla de registro.
import emilio.alzamora.exchangemoney.scene.register.view.RegisterActivity

// Importamos nuestra clase de almacenamiento local.
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference

// Clase que representa la pantalla de inicio de sesión.
class LoginActivity : AppCompatActivity() {

    // 1. Identificador para los mensajes de Logcat.
    private val tag = "LoginActivity"

    // 2. Binding conecta la Activity con activity_login.xml.
    private lateinit var binding: ActivityLoginBinding

    // 3. ViewModel contiene la lógica del inicio de sesión.
    private lateinit var viewModel: LoginViewModel

    // 4. SharedPreferences permite recuperar los datos guardados.
    private lateinit var sharedPreference: ConcreteSharedPreference

    // Indicamos si estamos actualizando la contraseña antigua.
    private var isMigrating = false

    // Se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Ejecutamos el método de la clase padre.
        super.onCreate(savedInstanceState)

        // Registramos la creación de la pantalla.
        Log.d(tag, "onCreate: Se creó el Login")

        // Conectamos la Activity con su archivo XML.
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Mostramos el diseño de la pantalla.
        setContentView(binding.root)

        // Inicializamos nuestro LoginViewModel.
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Inicializamos el almacenamiento local.
        sharedPreference = ConcreteSharedPreference(this)

        // Comprobamos si el usuario tiene una sesión activa.
        if (sharedPreference.isLoggedIn()) {

            // Abrimos directamente el conversor.
            openConverter()

            // Detenemos la ejecución del Login.
            return
        }

        // Configuramos las acciones de la pantalla.
        setupListeners()

        // Observamos las respuestas del ViewModel.
        setupObservers()
    }

    // 5. Configuramos las acciones de los botones.
    private fun setupListeners() {

        // Detectamos cuando presionamos Iniciar sesión.
        binding.btnLogin.setOnClickListener {

            // Obtenemos el correo ingresado.
            val user = binding.etUser.text.toString().trim()

            // Obtenemos la contraseña ingresada.
            val password = binding.etPassword.text.toString().trim()

            // Recuperamos el correo guardado.
            val savedEmail = sharedPreference.getEmail()

            // Recuperamos la información de la contraseña guardada.
            val savedPassword = sharedPreference.getPassword()

            // Enviamos los datos al ViewModel para verificarlos.
            viewModel.login(
                user,
                password,
                savedEmail,
                savedPassword
            )
        }

        // Detectamos cuando presionamos Regístrate.
        binding.tvRegister.setOnClickListener {

            // Comprobamos si existe una cuenta registrada.
            if (sharedPreference.hasRegisteredUser()) {

                // Mostramos información sobre la cuenta existente.
                AlertDialog.Builder(this)

                    // Colocamos el título de la ventana.
                    .setTitle("Cuenta existente")

                    // Explicamos que la aplicación utiliza una cuenta local.
                    .setMessage(
                        "Ya existe una cuenta registrada en este dispositivo. " +
                                "Inicia sesión con tus credenciales."
                    )

                    // Agregamos un botón para cerrar la ventana.
                    .setPositiveButton("Entendido", null)

                    // Mostramos la ventana.
                    .show()

            } else {

                // Creamos un Intent hacia la pantalla de registro.
                val intent = Intent(this, RegisterActivity::class.java)

                // Abrimos la pantalla de registro.
                startActivity(intent)
            }
        }

        // Detectamos cuando presionamos Olvidaste tu contraseña.
        binding.tvForgotPassword.setOnClickListener {

            // Mostramos la información sobre la recuperación.
            showPasswordRecoveryInfo()
        }
    }

    // 6. Mostramos información sobre la recuperación de contraseña.
    private fun showPasswordRecoveryInfo() {

        // Creamos una ventana informativa.
        AlertDialog.Builder(this)

            // Colocamos el título.
            .setTitle("Recuperar contraseña")

            // Explicamos cómo funciona nuestra cuenta local.
            .setMessage(
                "Tu cuenta se almacena localmente en este dispositivo. " +
                        "Por el momento, Exchange Money no cuenta con un " +
                        "servicio de recuperación de contraseña por correo electrónico."
            )

            // Agregamos un botón para cerrar la ventana.
            .setPositiveButton("Entendido", null)

            // Mostramos la ventana.
            .show()
    }

    // 7. Configuramos los observadores del ViewModel.
    private fun setupObservers() {

        // Observamos los mensajes de error.
        viewModel.errorMessage.observe(this) { message ->

            // Mostramos el mensaje recibido.
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observamos el resultado del inicio de sesión.
        viewModel.loginResult.observe(this) { isValid ->

            // Comprobamos si las credenciales son correctas.
            if (isValid) {

                // Evitamos iniciar dos migraciones al mismo tiempo.
                if (!isMigrating) {

                    // Actualizamos la contraseña si es necesario.
                    migratePasswordAndLogin()
                }

            } else {

                // Informamos que las credenciales son incorrectas.
                Toast.makeText(
                    this,
                    "Correo o contraseña incorrectos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observamos el estado de carga.
        viewModel.isLoading.observe(this) {

            // Actualizamos el estado de los componentes.
            updateInputs()
        }
    }

    // 8. Actualizamos los componentes de la pantalla.
    private fun updateInputs() {

        // Comprobamos si estamos verificando o migrando la contraseña.
        val isBusy = viewModel.isLoading.value == true || isMigrating

        // Habilitamos o deshabilitamos el botón de Login.
        binding.btnLogin.isEnabled = !isBusy

        // Habilitamos o deshabilitamos el campo de correo.
        binding.etUser.isEnabled = !isBusy

        // Habilitamos o deshabilitamos el campo de contraseña.
        binding.etPassword.isEnabled = !isBusy

        // Controlamos el acceso al registro.
        binding.tvRegister.isEnabled = !isBusy

        // Controlamos el acceso a la información de recuperación.
        binding.tvForgotPassword.isEnabled = !isBusy

        // Cambiamos el texto del botón según el estado.
        binding.btnLogin.text = if (isBusy) {
            "Verificando..."
        } else {
            "Iniciar sesión"
        }
    }

    // 9. Migramos la contraseña anterior e iniciamos sesión.
    private fun migratePasswordAndLogin() {

        // Indicamos que estamos actualizando la contraseña.
        isMigrating = true

        // Deshabilitamos temporalmente los componentes.
        updateInputs()

        // Recuperamos la contraseña que acaba de ser validada.
        val password = binding.etPassword.text.toString().trim()

        // Realizamos la migración en un hilo secundario.
        Thread {

            // Intentamos actualizar la contraseña.
            try {

                // Protegemos la contraseña antigua si todavía existe.
                sharedPreference.migratePassword(password)

                // Regresamos al hilo principal de Android.
                runOnUiThread {

                    // Comprobamos que la pantalla siga disponible.
                    if (!isFinishing && !isDestroyed) {

                        // Guardamos el estado de la sesión.
                        sharedPreference.saveSession(true)

                        // Finalizamos el estado de migración.
                        isMigrating = false

                        // Limpiamos el campo de contraseña.
                        binding.etPassword.text.clear()

                        // Mostramos un mensaje de inicio correcto.
                        Toast.makeText(
                            this,
                            "Inicio de sesión correcto",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Registramos el inicio correcto sin mostrar credenciales.
                        Log.d(tag, "Inicio de sesión correcto")

                        // Abrimos el conversor.
                        openConverter()
                    }
                }

            } catch (e: Exception) {

                // Regresamos al hilo principal.
                runOnUiThread {

                    // Comprobamos que la pantalla siga disponible.
                    if (!isFinishing && !isDestroyed) {

                        // Finalizamos el estado de migración.
                        isMigrating = false

                        // Habilitamos nuevamente los componentes.
                        updateInputs()

                        // Informamos que ocurrió un error.
                        Toast.makeText(
                            this,
                            "No se pudo completar el inicio de sesión",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Registramos el error sin incluir contraseñas.
                        Log.e(tag, "Error al actualizar las credenciales")
                    }
                }
            }

        }.start()
    }

    // 10. Función encargada de abrir el conversor.
    private fun openConverter() {

        // Creamos un Intent hacia ConverterActivity.
        val intent = Intent(this, ConverterActivity::class.java)

        // Abrimos la pantalla del conversor.
        startActivity(intent)

        // Cerramos el Login para evitar regresar con el botón atrás.
        finish()
    }

    // --------------------------------------------------
    // CICLO DE VIDA DE LA ACTIVITY
    // --------------------------------------------------

    // 11. Se ejecuta cuando la pantalla se vuelve visible.
    override fun onStart() {

        // Ejecutamos el método de la clase padre.
        super.onStart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStart: Login visible")
    }

    // 12. Se ejecuta cuando podemos interactuar con la pantalla.
    override fun onResume() {

        // Ejecutamos el método de la clase padre.
        super.onResume()

        // Registramos el evento en Logcat.
        Log.d(tag, "onResume: Login activo")
    }

    // 13. Se ejecuta cuando la pantalla pierde el enfoque.
    override fun onPause() {

        // Ejecutamos el método de la clase padre.
        super.onPause()

        // Registramos el evento en Logcat.
        Log.d(tag, "onPause: Login pausado")
    }

    // 14. Se ejecuta cuando la pantalla deja de ser visible.
    override fun onStop() {

        // Ejecutamos el método de la clase padre.
        super.onStop()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStop: Login no visible")
    }

    // 15. Se ejecuta cuando regresamos a la pantalla.
    override fun onRestart() {

        // Ejecutamos el método de la clase padre.
        super.onRestart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onRestart: Regresamos al Login")
    }

    // 16. Se ejecuta cuando se destruye la pantalla.
    override fun onDestroy() {

        // Ejecutamos el método de la clase padre.
        super.onDestroy()

        // Registramos el evento en Logcat.
        Log.d(tag, "onDestroy: Login cerrado")
    }
}