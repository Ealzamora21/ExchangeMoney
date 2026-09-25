// Paquete donde se encuentra nuestra pantalla de historial.
package emilio.alzamora.exchangemoney.scene.history.view

// Importamos Intent para navegar entre pantallas.
import android.content.Intent

// Importamos Bundle para el ciclo de vida de la Activity.
import android.os.Bundle

// Importamos Log para mostrar mensajes en Logcat.
import android.util.Log

// Importamos View para controlar la visibilidad.
import android.view.View

// Importamos ArrayAdapter para mostrar las conversiones.
import android.widget.ArrayAdapter

// Importamos Toast para mostrar mensajes al usuario.
import android.widget.Toast

// Importamos AlertDialog para mostrar una confirmación.
import androidx.appcompat.app.AlertDialog

// Importamos la clase base de nuestra Activity.
import androidx.appcompat.app.AppCompatActivity

// Importamos ViewModelProvider para inicializar el ViewModel.
import androidx.lifecycle.ViewModelProvider

// Importamos los recursos de nuestra aplicación.
import emilio.alzamora.exchangemoney.R

// Importamos el Binding de la pantalla de historial.
import emilio.alzamora.exchangemoney.databinding.ActivityHistoryBinding

// Importamos la pantalla del conversor.
import emilio.alzamora.exchangemoney.scene.converter.view.ConverterActivity

// Importamos nuestro ViewModel del historial.
import emilio.alzamora.exchangemoney.scene.history.viewModel.HistoryViewModel

// Importamos la pantalla de perfil.
import emilio.alzamora.exchangemoney.scene.profile.view.ProfileActivity

// Importamos nuestra clase de almacenamiento local.
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference

// Clase encargada de mostrar el historial de conversiones.
class HistoryActivity : AppCompatActivity() {

    // 1. Identificador para los mensajes de Logcat.
    private val tag = "HistoryActivity"

    // 2. Binding conecta la Activity con el archivo XML.
    private lateinit var binding: ActivityHistoryBinding

    // 3. ViewModel contiene la lógica del historial.
    private lateinit var viewModel: HistoryViewModel

    // 4. SharedPreferences permite recuperar las conversiones.
    private lateinit var sharedPreference: ConcreteSharedPreference

    // Se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Ejecutamos el método de la clase padre.
        super.onCreate(savedInstanceState)

        // Registramos la creación de la pantalla.
        Log.d(tag, "onCreate: Se creó el historial")

        // Conectamos la Activity con su archivo XML.
        binding = ActivityHistoryBinding.inflate(layoutInflater)

        // Mostramos el diseño de la pantalla.
        setContentView(binding.root)

        // Inicializamos nuestro HistoryViewModel.
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        // Inicializamos el almacenamiento local.
        sharedPreference = ConcreteSharedPreference(this)

        // Configuramos las acciones de los botones.
        setupListeners()

        // Configuramos los observadores del ViewModel.
        setupObservers()

        // Configuramos la navegación inferior.
        setupBottomNavigation()
    }

    // 5. Función para recuperar las conversiones guardadas.
    private fun loadHistory() {

        // Obtenemos el historial desde SharedPreferences.
        val savedHistory =
            sharedPreference.getConversionHistory()

        // Enviamos las conversiones al ViewModel.
        viewModel.loadHistory(savedHistory)
    }

    // 6. Configuramos las acciones de los botones.
    private fun setupListeners() {

        // Detectamos cuando presionamos Borrar historial.
        binding.btnClearHistory.setOnClickListener {

            // Mostramos una ventana de confirmación.
            showDeleteConfirmation()
        }
    }

    // 7. Solicitamos una confirmación antes de borrar el historial.
    private fun showDeleteConfirmation() {

        // Creamos una ventana de confirmación.
        AlertDialog.Builder(this)

            // Colocamos el título de la ventana.
            .setTitle("Borrar historial")

            // Mostramos el mensaje de confirmación.
            .setMessage("¿Deseas eliminar todas tus conversiones?")

            // Configuramos el botón para confirmar.
            .setPositiveButton("Eliminar") { _, _ ->

                // Eliminamos todas las conversiones.
                clearHistory()
            }

            // Configuramos el botón para cancelar.
            .setNegativeButton("Cancelar", null)

            // Mostramos la ventana.
            .show()
    }

    // 8. Función encargada de eliminar el historial.
    private fun clearHistory() {

        // Eliminamos las conversiones de SharedPreferences.
        sharedPreference.clearConversionHistory()

        // Actualizamos la lista del ViewModel.
        viewModel.clearHistory()

        // Informamos al usuario que el historial fue eliminado.
        Toast.makeText(
            this,
            "Historial eliminado",
            Toast.LENGTH_SHORT
        ).show()

        // Registramos la eliminación en Logcat.
        Log.d(tag, "Historial eliminado correctamente")
    }

    // 9. Observamos los datos enviados por el ViewModel.
    private fun setupObservers() {

        // Observamos los cambios en el historial.
        viewModel.history.observe(this) { history ->

            // Creamos un adaptador para mostrar las conversiones.
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                history
            )

            // Colocamos el adaptador en nuestro ListView.
            binding.listHistory.adapter = adapter

            // Verificamos si el historial está vacío.
            if (history.isEmpty()) {

                // Ocultamos la lista de conversiones.
                binding.listHistory.visibility = View.INVISIBLE

                // Mostramos el mensaje de historial vacío.
                binding.tvEmptyHistory.visibility = View.VISIBLE

                // Desactivamos el botón de eliminar.
                binding.btnClearHistory.isEnabled = false

            } else {

                // Mostramos las conversiones guardadas.
                binding.listHistory.visibility = View.VISIBLE

                // Ocultamos el mensaje de historial vacío.
                binding.tvEmptyHistory.visibility = View.GONE

                // Habilitamos el botón de eliminar.
                binding.btnClearHistory.isEnabled = true
            }
        }
    }

    // 10. Configuramos el menú de navegación inferior.
    private fun setupBottomNavigation() {

        // Marcamos Historial como la pantalla seleccionada.
        binding.bottomNavigation.selectedItemId = R.id.nav_history

        // Detectamos la opción seleccionada por el usuario.
        binding.bottomNavigation.setOnItemSelectedListener { item ->

            // Comprobamos qué opción fue seleccionada.
            when (item.itemId) {

                // Opción correspondiente al conversor.
                R.id.nav_converter -> {

                    // Creamos un Intent hacia el conversor.
                    val intent = Intent(
                        this,
                        ConverterActivity::class.java
                    )

                    // Recuperamos el conversor si ya está abierto.
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                    // Abrimos la pantalla del conversor.
                    startActivity(intent)

                    // Cerramos la pantalla de historial.
                    finish()

                    // Conservamos la selección antes de salir.
                    false
                }

                // Opción correspondiente al historial.
                R.id.nav_history -> {

                    // Permanecemos en la pantalla actual.
                    true
                }

                // Opción correspondiente al perfil.
                R.id.nav_profile -> {

                    // Creamos un Intent hacia el perfil.
                    val intent = Intent(
                        this,
                        ProfileActivity::class.java
                    )

                    // Reutilizamos la pantalla si ya existe.
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                    // Abrimos la pantalla del perfil.
                    startActivity(intent)

                    // Cerramos la pantalla de historial.
                    finish()

                    // Conservamos la selección antes de salir.
                    false
                }

                // Controlamos cualquier opción desconocida.
                else -> false
            }
        }
    }

    // --------------------------------------------------
    // CICLO DE VIDA DE LA ACTIVITY
    // --------------------------------------------------

    // 11. Se ejecuta cuando la pantalla se vuelve visible.
    override fun onStart() {

        // Ejecutamos el método de la clase padre.
        super.onStart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStart: Historial visible")
    }

    // 12. Se ejecuta cuando podemos interactuar con la pantalla.
    override fun onResume() {

        // Ejecutamos el método de la clase padre.
        super.onResume()

        // Actualizamos el historial cada vez que regresamos.
        loadHistory()

        // Registramos el evento en Logcat.
        Log.d(tag, "onResume: Historial actualizado")
    }

    // 13. Se ejecuta cuando la pantalla pierde el enfoque.
    override fun onPause() {

        // Ejecutamos el método de la clase padre.
        super.onPause()

        // Registramos el evento en Logcat.
        Log.d(tag, "onPause: Historial pausado")
    }

    // 14. Se ejecuta cuando la pantalla deja de ser visible.
    override fun onStop() {

        // Ejecutamos el método de la clase padre.
        super.onStop()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStop: Historial no visible")
    }

    // 15. Se ejecuta cuando regresamos a la pantalla.
    override fun onRestart() {

        // Ejecutamos el método de la clase padre.
        super.onRestart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onRestart: Regresamos al historial")
    }

    // 16. Se ejecuta cuando se destruye la pantalla.
    override fun onDestroy() {

        // Ejecutamos el método de la clase padre.
        super.onDestroy()

        // Registramos el evento en Logcat.
        Log.d(tag, "onDestroy: Historial cerrado")
    }
}