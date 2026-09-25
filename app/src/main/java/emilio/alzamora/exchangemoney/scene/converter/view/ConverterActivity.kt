// Paquete donde se encuentra nuestra pantalla del conversor.
package emilio.alzamora.exchangemoney.scene.converter.view

// Importamos Context para acceder a los servicios del dispositivo.
import android.content.Context

// Importamos Intent para navegar entre pantallas.
import android.content.Intent

// Importamos Sensor para utilizar el acelerómetro.
import android.hardware.Sensor

// Importamos SensorEvent para recibir los datos del sensor.
import android.hardware.SensorEvent

// Importamos SensorEventListener para detectar movimientos.
import android.hardware.SensorEventListener

// Importamos SensorManager para administrar los sensores.
import android.hardware.SensorManager

// Importamos Bundle para el ciclo de vida de la Activity.
import android.os.Bundle

// Importamos Log para mostrar mensajes en Logcat.
import android.util.Log

// Importamos View para controlar la visibilidad de los elementos.
import android.view.View

// Importamos ArrayAdapter para mostrar las monedas disponibles.
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged

// Importamos Toast para mostrar mensajes al usuario.
import android.widget.Toast

// Importamos la clase base de nuestra Activity.
import androidx.appcompat.app.AppCompatActivity

// Importamos ViewModelProvider para utilizar nuestro ViewModel.
import androidx.lifecycle.ViewModelProvider

// Importamos los recursos de nuestra aplicación.
import emilio.alzamora.exchangemoney.R

// Importamos el Binding de nuestra pantalla.
import emilio.alzamora.exchangemoney.databinding.ActivityConverterBinding

// Importamos el ViewModel del conversor.
import emilio.alzamora.exchangemoney.scene.converter.viewModel.ConverterViewModel

// Importamos la pantalla del historial.
import emilio.alzamora.exchangemoney.scene.history.view.HistoryActivity

// Importamos la pantalla del perfil.
import emilio.alzamora.exchangemoney.scene.profile.view.ProfileActivity

// Importamos nuestra clase de almacenamiento local.
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference

// Importamos Locale para mostrar los decimales.
import java.util.Locale

// Importamos sqrt para calcular la aceleración del dispositivo.
import kotlin.math.sqrt

// Clase que representa la pantalla del conversor de monedas.
class ConverterActivity : AppCompatActivity(), SensorEventListener {

    // 1. Identificador para los mensajes de Logcat.
    private val TAG = "ConverterActivity"

    // 2. Binding para acceder a los elementos del XML.
    private lateinit var binding: ActivityConverterBinding

    // 3. ViewModel encargado de la lógica del conversor.
    private lateinit var viewModel: ConverterViewModel

    // 4. Almacenamiento local de las conversiones.
    private lateinit var sharedPreference: ConcreteSharedPreference

    // 5. Administrador de los sensores del dispositivo.
    private lateinit var sensorManager: SensorManager

    // Almacenamos el acelerómetro si está disponible.
    private var accelerometer: Sensor? = null

    // Guardamos el tiempo de la última sacudida.
    private var lastShakeTime = 0L

    // Se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Ejecutamos el método de la clase padre.
        super.onCreate(savedInstanceState)

        // Registramos la creación de la pantalla.
        Log.d(TAG, "onCreate: Se creó el conversor")

        // Conectamos la Activity con el archivo XML.
        binding = ActivityConverterBinding.inflate(layoutInflater)

        // Mostramos el diseño de la pantalla.
        setContentView(binding.root)

        // Inicializamos nuestro ViewModel.
        viewModel = ViewModelProvider(this)[ConverterViewModel::class.java]

        // Inicializamos el almacenamiento local.
        sharedPreference = ConcreteSharedPreference(this)

        // Obtenemos el administrador de sensores.
        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Buscamos el acelerómetro del dispositivo.
        accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Verificamos si existe un acelerómetro.
        if (accelerometer != null) {

            // Mostramos las instrucciones para agitar el dispositivo.
            binding.tvShakeHint.visibility = View.VISIBLE
        }

        // Configuramos las acciones de los botones.
        setupListeners()

        // Configuramos los observadores del ViewModel.
        setupObservers()

        // Configuramos la navegación inferior.
        setupBottomNavigation()

        // Cargamos las monedas disponibles.
        viewModel.loadCurrencies()
    }

    // 6. Configuramos las acciones de los botones.
    private fun setupListeners() {

        // Actualizamos el resultado cuando cambia el monto.
        binding.etAmount.doAfterTextChanged {
            updateConversionDisplay()
        }

        // Detectamos cambios en las monedas seleccionadas.
        val currencyListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateConversionDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                updateConversionDisplay()
            }
        }

        // Asignamos el detector a ambos selectores.
        binding.spinnerFrom.onItemSelectedListener = currencyListener
        binding.spinnerTo.onItemSelectedListener = currencyListener


        // Detectamos cuando presionamos Convertir.
        binding.btnConvert.setOnClickListener {

            // Obtenemos el monto ingresado por el usuario.
            val amountText = binding.etAmount.text.toString().trim()

            // Convertimos el texto a un número decimal.
            val amount = amountText.toDoubleOrNull()

            // Verificamos que el monto sea válido y mayor a cero.
            if (amount == null || !amount.isFinite() || amount <= 0) {

                // Mostramos un mensaje de error.
                Toast.makeText(
                    this,
                    "Ingresa un monto mayor a 0",
                    Toast.LENGTH_SHORT
                ).show()

                // Detenemos la conversión.
                return@setOnClickListener
            }

            // Obtenemos la moneda de origen seleccionada.
            val fromCurrency =
                binding.spinnerFrom.selectedItem?.toString()

            // Obtenemos la moneda de destino seleccionada.
            val toCurrency =
                binding.spinnerTo.selectedItem?.toString()

            // Comprobamos que ambas monedas estén seleccionadas.
            if (fromCurrency == null || toCurrency == null) {

                // Informamos que deben seleccionarse las monedas.
                Toast.makeText(
                    this,
                    "Selecciona las monedas",
                    Toast.LENGTH_SHORT
                ).show()

                // Detenemos la conversión.
                return@setOnClickListener
            }

            // Enviamos los datos al ViewModel para realizar la conversión.
            viewModel.convertCurrency(
                amount,
                fromCurrency,
                toCurrency
            )
        }

        // Detectamos cuando presionamos Intercambiar.
        binding.btnSwap.setOnClickListener {

            // Intercambiamos las monedas seleccionadas.
            swapCurrencies()
        }
    }

    // 7. Observamos los datos enviados por el ViewModel.
    private fun setupObservers() {

        // Observamos la lista de monedas disponibles.
        viewModel.currencies.observe(this) { currencies ->

            // Creamos el adaptador para mostrar las monedas.
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                currencies
            )

            // Configuramos el diseño de la lista desplegable.
            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            // Colocamos las monedas en el Spinner de origen.
            binding.spinnerFrom.adapter = adapter

            // Colocamos las monedas en el Spinner de destino.
            binding.spinnerTo.adapter = adapter

            // Seleccionamos USD como moneda inicial.
            binding.spinnerFrom.setSelection(0)

            // Seleccionamos MXN como moneda de destino.
            binding.spinnerTo.setSelection(1)
        }

        // Observamos el tipo de cambio recibido de la API.
        viewModel.exchangeRate.observe(this) { rate ->

            // Mostramos el tipo de cambio con cuatro decimales.
            binding.tvExchangeRate.text = String.format(
                Locale.US,
                "Tipo de cambio: %.4f",
                rate
            )
        }

        // Observamos el resultado de la conversión.
        viewModel.conversionResult.observe(this) { result ->

            // Mostramos el resultado con dos decimales.
            binding.tvResult.text = String.format(
                Locale.US,
                "%.2f %s",
                result,
                viewModel.currentToCurrency
            )

            // Comprobamos que el resultado no se haya guardado antes.
            updateConversionDisplay()

            if (viewModel.consumeConversionResult()) {

                // Creamos el texto que guardaremos en el historial.
                val conversion = String.format(
                    Locale.US,
                    "%.2f %s → %.2f %s",
                    viewModel.currentAmount,
                    viewModel.currentFromCurrency,
                    result,
                    viewModel.currentToCurrency
                )

                // Guardamos la conversión en SharedPreferences.
                sharedPreference.saveConversion(conversion)

                // Registramos la conversión en Logcat.
                Log.d(TAG, "Conversión guardada en el historial")
            }
        }

        // Observamos los mensajes de error.
        viewModel.errorMessage.observe(this) { message ->

            // Mostramos el mensaje enviado por el ViewModel.
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        }

        // Observamos el estado de carga de la API.
        viewModel.isLoading.observe(this) { isLoading ->

            // Verificamos si estamos realizando una consulta.
            if (isLoading) {

                // Mostramos la barra de carga.
                binding.progressBar.visibility = View.VISIBLE

                // Ocultamos temporalmente el resultado.
                binding.tvResult.visibility = View.INVISIBLE

                // Desactivamos el botón Convertir.
                binding.btnConvert.isEnabled = false

                // Desactivamos el botón Intercambiar.
                binding.btnSwap.isEnabled = false

                // Desactivamos la selección de la moneda de origen.
                binding.spinnerFrom.isEnabled = false

                // Desactivamos la selección de la moneda de destino.
                binding.spinnerTo.isEnabled = false

                // Evitamos modificar el monto durante la consulta.
                binding.etAmount.isEnabled = false

            } else {

                // Ocultamos la barra de carga.
                binding.progressBar.visibility = View.GONE

                // Mostramos nuevamente el resultado.
                binding.tvResult.visibility = View.VISIBLE

                // Habilitamos el botón Convertir.
                binding.btnConvert.isEnabled = true

                // Habilitamos el botón Intercambiar.
                binding.btnSwap.isEnabled = true

                // Habilitamos la selección de la moneda de origen.
                binding.spinnerFrom.isEnabled = true

                // Habilitamos la selección de la moneda de destino.
                binding.spinnerTo.isEnabled = true

                // Permitimos modificar el monto.
                binding.etAmount.isEnabled = true
                updateConversionDisplay()
            }
        }
    }

    // 8. Función encargada de intercambiar las monedas.
    private fun updateConversionDisplay() {

        // Recuperamos los datos actuales del formulario.
        val amount = binding.etAmount.text.toString().trim().toDoubleOrNull()
        val from = binding.spinnerFrom.selectedItem?.toString()
        val to = binding.spinnerTo.selectedItem?.toString()

        // Comprobamos si el resultado corresponde a los datos actuales.
        val matches = amount == viewModel.currentAmount &&
                from == viewModel.currentFromCurrency &&
                to == viewModel.currentToCurrency &&
                viewModel.conversionResult.value != null

        // Mostramos solamente resultados que correspondan al formulario.
        if (matches) {

            binding.tvResult.text = String.format(
                Locale.US,
                "%.2f %s",
                viewModel.conversionResult.value ?: 0.0,
                viewModel.currentToCurrency
            )

            binding.tvExchangeRate.text = String.format(
                Locale.US,
                "Tipo de cambio: %.4f",
                viewModel.exchangeRate.value ?: 0.0
            )

        } else {

            // Evitamos mostrar resultados de conversiones anteriores.
            binding.tvResult.text = "--"
            binding.tvExchangeRate.setText(R.string.exchange_rate_empty)
        }
    }

    // Funcion encargada de intercambiar las monedas.
    private fun swapCurrencies() {

        // Verificamos que no exista una consulta activa.
        if (viewModel.isLoading.value == true) {

            // Detenemos el intercambio.
            return
        }

        // Obtenemos la posición de la moneda de origen.
        val fromPosition =
            binding.spinnerFrom.selectedItemPosition

        // Obtenemos la posición de la moneda de destino.
        val toPosition =
            binding.spinnerTo.selectedItemPosition

        // Comprobamos que ambas posiciones sean válidas.
        if (fromPosition < 0 || toPosition < 0) {

            // Detenemos la función.
            return
        }

        // Colocamos la moneda de destino como moneda de origen.
        binding.spinnerFrom.setSelection(toPosition)

        // Colocamos la moneda de origen como moneda de destino.
        binding.spinnerTo.setSelection(fromPosition)
        updateConversionDisplay()

        // Mostramos un mensaje de confirmación.
        Toast.makeText(
            this,
            "Monedas intercambiadas",
            Toast.LENGTH_SHORT
        ).show()

        // Registramos el intercambio en Logcat.
        Log.d(TAG, "Monedas intercambiadas")
    }

    // 9. Configuramos el menú de navegación inferior.
    private fun setupBottomNavigation() {

        // Marcamos Conversor como la pantalla seleccionada.
        binding.bottomNavigation.selectedItemId =
            R.id.nav_converter

        // Detectamos la opción seleccionada por el usuario.
        binding.bottomNavigation.setOnItemSelectedListener { item ->

            // Comprobamos qué opción seleccionó el usuario.
            when (item.itemId) {

                // Opción correspondiente al conversor.
                R.id.nav_converter -> {

                    // Permanecemos en la pantalla actual.
                    true
                }

                // Opción correspondiente al historial.
                R.id.nav_history -> {

                    // Creamos un Intent hacia el historial.
                    val intent = Intent(
                        this,
                        HistoryActivity::class.java
                    )

                    // Reutilizamos la pantalla si ya existe.
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                    // Abrimos la pantalla del historial.
                    startActivity(intent)

                    // Conservamos la selección del conversor.
                    false
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

                    // Abrimos la pantalla de perfil.
                    startActivity(intent)

                    // Conservamos la selección del conversor.
                    false
                }

                // Controlamos cualquier opción desconocida.
                else -> false
            }
        }
    }

    // --------------------------------------------------
    // ACELERÓMETRO
    // --------------------------------------------------

    // 10. Detectamos los movimientos del dispositivo.
    override fun onSensorChanged(event: SensorEvent?) {

        // Comprobamos que exista información del sensor.
        if (event == null) {

            // Detenemos la función si no hay información.
            return
        }

        // Verificamos que los datos sean del acelerómetro.
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) {

            // Ignoramos los datos de otros sensores.
            return
        }

        // Obtenemos la aceleración del eje X.
        val x = event.values[0]

        // Obtenemos la aceleración del eje Y.
        val y = event.values[1]

        // Obtenemos la aceleración del eje Z.
        val z = event.values[2]

        // Calculamos la aceleración total del dispositivo.
        // Restamos la gravedad para detectar movimientos fuertes.
        val acceleration = sqrt(
            x * x + y * y + z * z
        ) - SensorManager.GRAVITY_EARTH

        // Obtenemos el tiempo actual en milisegundos.
        val currentTime = System.currentTimeMillis()

        // Verificamos que el movimiento supere el límite establecido.
        // También evitamos detectar varias sacudidas consecutivas.
        if (acceleration > 12 &&
            currentTime - lastShakeTime > 1000 &&
            viewModel.isLoading.value != true
        ) {

            // Guardamos el momento de la última sacudida.
            lastShakeTime = currentTime

            // Intercambiamos las monedas seleccionadas.
            swapCurrencies()

            // Registramos el movimiento detectado en Logcat.
            Log.d(TAG, "Sacudida detectada")
        }
    }

    // 11. Se ejecuta cuando cambia la precisión del sensor.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        // No necesitamos realizar ninguna acción en este proyecto.
    }

    // --------------------------------------------------
    // CICLO DE VIDA DE LA ACTIVITY
    // --------------------------------------------------

    // 12. Se ejecuta cuando la pantalla se vuelve visible.
    override fun onStart() {

        // Ejecutamos el método de la clase padre.
        super.onStart()

        // Registramos el evento en Logcat.
        Log.d(TAG, "onStart: Conversor visible")
    }

    // 13. Se ejecuta cuando podemos interactuar con la pantalla.
    override fun onResume() {

        // Ejecutamos el método de la clase padre.
        super.onResume()

        // Obtenemos el acelerómetro disponible.
        val sensor = accelerometer

        // Verificamos que exista el sensor.
        if (sensor != null) {

            // Activamos la detección de movimientos.
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        // Registramos el evento en Logcat.
        Log.d(TAG, "onResume: Conversor activo")
    }

    // 14. Se ejecuta cuando la pantalla pierde el enfoque.
    override fun onPause() {

        // Desactivamos la detección de movimientos.
        sensorManager.unregisterListener(this)

        // Ejecutamos el método de la clase padre.
        super.onPause()

        // Registramos el evento en Logcat.
        Log.d(TAG, "onPause: Conversor pausado")
    }

    // 15. Se ejecuta cuando la pantalla deja de ser visible.
    override fun onStop() {

        // Ejecutamos el método de la clase padre.
        super.onStop()

        // Registramos el evento en Logcat.
        Log.d(TAG, "onStop: Conversor no visible")
    }

    // 16. Se ejecuta cuando regresamos a una pantalla detenida.
    override fun onRestart() {

        // Ejecutamos el método de la clase padre.
        super.onRestart()

        // Registramos el evento en Logcat.
        Log.d(TAG, "onRestart: Regresamos al conversor")
    }

    // 17. Se ejecuta cuando se destruye la pantalla.
    override fun onDestroy() {

        // Ejecutamos el método de la clase padre.
        super.onDestroy()

        // Registramos el evento en Logcat.
        Log.d(TAG, "onDestroy: Conversor cerrado")
    }
}