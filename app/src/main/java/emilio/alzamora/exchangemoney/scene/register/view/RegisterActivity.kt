// Paquete al que pertenece nuestra Activity.
package emilio.alzamora.exchangemoney.scene.register.view

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

// Importamos ViewModelProvider para obtener nuestro ViewModel.
import androidx.lifecycle.ViewModelProvider

// Importamos el Binding de la pantalla de registro.
import emilio.alzamora.exchangemoney.databinding.ActivityRegisterBinding

// Importamos el ViewModel del registro.
import emilio.alzamora.exchangemoney.scene.register.viewModel.RegisterViewModel

// Importamos nuestra clase de almacenamiento local.
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference

// Clase que representa la pantalla de registro.
class RegisterActivity : AppCompatActivity() {

    // 1. Identificador para los mensajes de Logcat.
    private val tag = "RegisterActivity"

    // 2. Binding conecta esta Activity con activity_register.xml.
    private lateinit var binding: ActivityRegisterBinding

    // 3. ViewModel contiene la lógica del registro.
    private lateinit var viewModel: RegisterViewModel

    // 4. SharedPreferences permite guardar los datos del usuario.
    private lateinit var sharedPreference: ConcreteSharedPreference

    // Indicamos si estamos guardando una nueva cuenta.
    private var isRegistering = false

    // Este método se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Ejecutamos el onCreate de la clase padre.
        super.onCreate(savedInstanceState)

        // Registramos la creación de la pantalla.
        Log.d(tag, "onCreate: Se creó el Registro")

        // Conectamos la Activity con su archivo XML.
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        // Mostramos el diseño de la pantalla.
        setContentView(binding.root)

        // Inicializamos nuestro RegisterViewModel.
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // Inicializamos el almacenamiento local.
        sharedPreference = ConcreteSharedPreference(applicationContext)

        // Comprobamos si ya existe una cuenta registrada.
        if (sharedPreference.hasRegisteredUser()) {

            // Informamos que no se puede sobrescribir la cuenta.
            showExistingAccountDialog()

            // Detenemos la configuración del registro.
            return
        }

        // Configuramos las acciones de la pantalla.
        setupListeners()

        // Observamos las respuestas del ViewModel.
        setupObservers()
    }

    // 5. Configuramos las acciones de los botones.
    private fun setupListeners() {

        // Se ejecuta cuando presionamos Crear cuenta.
        binding.btnRegister.setOnClickListener {

            // Evitamos iniciar varios registros al mismo tiempo.
            if (isRegistering) {
                return@setOnClickListener
            }

            // Comprobamos nuevamente si existe una cuenta.
            if (sharedPreference.hasRegisteredUser()) {

                // Mostramos la ventana informativa.
                showExistingAccountDialog()

                return@setOnClickListener
            }

            // Obtenemos el nombre ingresado.
            val name = binding.etName.text.toString().trim()

            // Obtenemos el correo ingresado.
            val email = binding.etEmail.text.toString().trim()

            // Obtenemos la contraseña ingresada.
            val password = binding.etPassword.text.toString().trim()

            // Mandamos los datos al ViewModel para validarlos.
            viewModel.register(
                name,
                email,
                password
            )
        }

        // Se ejecuta cuando presionamos Inicia sesión.
        binding.tvLogin.setOnClickListener {

            // Evitamos salir mientras se guarda la cuenta.
            if (!isRegistering) {

                // Cerramos el registro y regresamos al Login.
                finish()
            }
        }
    }

    // 6. Observamos los resultados del ViewModel.
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

        // Observamos el resultado de la validación.
        viewModel.registerResult.observe(this) { isValid ->

            // Verificamos si los datos son correctos.
            if (isValid && !isRegistering) {

                // Guardamos la cuenta de forma protegida.
                saveRegisteredUser()
            }
        }
    }

    // 7. Guardamos los datos del usuario.
    private fun saveRegisteredUser() {

        // Indicamos que el registro está en proceso.
        isRegistering = true

        // Deshabilitamos temporalmente los componentes.
        updateInputs()

        // Recuperamos el nombre validado.
        val name = binding.etName.text.toString().trim()

        // Recuperamos el correo validado.
        val email = binding.etEmail.text.toString().trim()

        // Recuperamos la contraseña validada.
        val password = binding.etPassword.text.toString().trim()

        // Ejecutamos el almacenamiento en un hilo secundario.
        Thread {

            try {

                // Sincronizamos el acceso para evitar registros simultáneos.
                val accountCreated = synchronized(ConcreteSharedPreference::class.java) {

                    // Comprobamos si ya existe una cuenta.
                    if (sharedPreference.hasRegisteredUser()) {

                        // No permitimos sobrescribir la cuenta.
                        false

                    } else {

                        // Guardamos los datos mediante SharedPreferences.
                        // La contraseña se protege mediante PBKDF2.
                        sharedPreference.saveUser(
                            name,
                            email,
                            password
                        )

                        // Indicamos que la cuenta fue creada.
                        true
                    }
                }

                // Regresamos al hilo principal de Android.
                runOnUiThread {

                    // Comprobamos que la pantalla siga disponible.
                    if (!isFinishing && !isDestroyed) {

                        // Finalizamos el estado de registro.
                        isRegistering = false

                        // Actualizamos los componentes.
                        updateInputs()

                        // Verificamos si se creó la cuenta.
                        if (accountCreated) {

                            // Limpiamos el campo de contraseña.
                            binding.etPassword.text.clear()

                            // Mostramos un mensaje de registro exitoso.
                            Toast.makeText(
                                this,
                                "Cuenta creada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Registramos el evento sin mostrar credenciales.
                            Log.d(tag, "Cuenta creada correctamente")

                            // Cerramos el registro y regresamos al Login.
                            finish()

                        } else {

                            // Informamos que ya existe una cuenta.
                            showExistingAccountDialog()
                        }
                    }
                }

            } catch (e: Exception) {

                // Regresamos al hilo principal.
                runOnUiThread {

                    // Comprobamos que la pantalla siga disponible.
                    if (!isFinishing && !isDestroyed) {

                        // Finalizamos el estado de registro.
                        isRegistering = false

                        // Habilitamos nuevamente los componentes.
                        updateInputs()

                        // Informamos que ocurrió un error.
                        Toast.makeText(
                            this,
                            "No se pudo crear la cuenta",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Registramos el error sin mostrar credenciales.
                        Log.e(tag, "Error al guardar la cuenta")
                    }
                }
            }

        }.start()
    }

    // 8. Actualizamos los componentes de la pantalla.
    private fun updateInputs() {

        // Controlamos el acceso al botón de registro.
        binding.btnRegister.isEnabled = !isRegistering

        // Controlamos el campo del nombre.
        binding.etName.isEnabled = !isRegistering

        // Controlamos el campo del correo electrónico.
        binding.etEmail.isEnabled = !isRegistering

        // Controlamos el campo de la contraseña.
        binding.etPassword.isEnabled = !isRegistering

        // Controlamos el acceso al inicio de sesión.
        binding.tvLogin.isEnabled = !isRegistering

        // Cambiamos el texto del botón según el estado.
        binding.btnRegister.text = if (isRegistering) {
            "Creando cuenta..."
        } else {
            "Crear cuenta"
        }
    }

    // 9. Informamos que existe una cuenta registrada.
    private fun showExistingAccountDialog() {

        // Creamos una ventana informativa.
        AlertDialog.Builder(this)

            // Colocamos el título de la ventana.
            .setTitle("Cuenta existente")

            // Explicamos la limitación del almacenamiento local.
            .setMessage(
                "Ya existe una cuenta registrada en este dispositivo. " +
                        "Inicia sesión con tus credenciales para continuar."
            )

            // Agregamos un botón para regresar al Login.
            .setPositiveButton("Iniciar sesión") { _, _ ->

                // Cerramos la pantalla de registro.
                finish()
            }

            // Evitamos cerrar accidentalmente la ventana.
            .setCancelable(false)

            // Mostramos la ventana.
            .show()
    }
}