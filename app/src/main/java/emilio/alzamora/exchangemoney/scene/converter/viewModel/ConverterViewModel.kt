// Paquete donde se encuentra nuestro ViewModel.
package emilio.alzamora.exchangemoney.scene.converter.viewModel

// Importamos BuildConfig para recuperar la API key.
import emilio.alzamora.exchangemoney.BuildConfig

// Importamos LiveData para observar los datos.
import androidx.lifecycle.LiveData

// Importamos MutableLiveData para modificar los datos.
import androidx.lifecycle.MutableLiveData

// Importamos ViewModel para manejar la lógica del conversor.
import androidx.lifecycle.ViewModel
import emilio.alzamora.exchangemoney.scene.converter.utils.CurrencyCalculator

// Importamos nuestro cliente de Retrofit.
import emilio.alzamora.exchangemoney.api.client.RetrofitClient

// Importamos el modelo de respuesta de la API.
import emilio.alzamora.exchangemoney.api.model.ExchangeRateResponse

// Importamos Call para realizar peticiones HTTP.
import retrofit2.Call

// Importamos Callback para recibir las respuestas.
import retrofit2.Callback

// Importamos Response para acceder a la respuesta HTTP.
import retrofit2.Response

// Importamos IOException para identificar errores de conexión.
import java.io.IOException

// Clase encargada de manejar la lógica del conversor.
class ConverterViewModel : ViewModel() {

    // 1. Recuperamos la clave desde BuildConfig.
    private val apiKey = BuildConfig.EXCHANGE_API_KEY

    // 2. Almacenamos la lista de monedas disponibles.
    private val _currencies = MutableLiveData<List<String>>()

    // Permitimos que la Activity observe las monedas.
    val currencies: LiveData<List<String>> = _currencies

    // 3. Almacenamos el resultado de la conversión.
    private val _conversionResult = MutableLiveData<Double>()

    // Permitimos observar el resultado.
    val conversionResult: LiveData<Double> = _conversionResult

    // 4. Almacenamos el tipo de cambio utilizado.
    private val _exchangeRate = MutableLiveData<Double>()

    // Permitimos observar el tipo de cambio.
    val exchangeRate: LiveData<Double> = _exchangeRate

    // 5. Almacenamos los mensajes de error.
    private val _errorMessage = MutableLiveData<String>()

    // Permitimos observar los mensajes de error.
    val errorMessage: LiveData<String> = _errorMessage

    // 6. Indicamos si estamos consultando la API.
    private val _isLoading = MutableLiveData<Boolean>(false)

    // Permitimos observar el estado de carga.
    val isLoading: LiveData<Boolean> = _isLoading

    // 7. Guardamos el monto de la última conversión exitosa.
    var currentAmount = 0.0
        private set

    // Guardamos la moneda de origen.
    var currentFromCurrency = ""
        private set

    // Guardamos la moneda de destino.
    var currentToCurrency = ""
        private set

    // Indicamos si existe una conversión pendiente de guardar.
    private var isWaitingForResult = false

    // 8. Cargamos las monedas disponibles.
    fun loadCurrencies() {

        // Verificamos que la lista todavía no esté cargada.
        if (_currencies.value == null) {

            // Creamos la lista de monedas disponibles.
            _currencies.value = listOf(
                "USD",
                "MXN",
                "EUR",
                "GBP",
                "JPY",
                "CAD"
            )
        }
    }

    // 9. Función encargada de realizar la conversión.
    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ) {

        // Verificamos que la API key esté configurada.
        if (apiKey.isBlank() || apiKey == "TU_API_KEY") {

            // Informamos que falta configurar la clave.
            _errorMessage.value = "La API key no está configurada"

            // Detenemos la conversión.
            return
        }

        // Verificamos que el monto sea válido y mayor a cero.
        if (!CurrencyCalculator.isValidAmount(amount)) {

            // Mostramos un mensaje de error.
            _errorMessage.value = "Ingresa un monto mayor a 0"

            // Detenemos la conversión.
            return
        }

        // Comprobamos si existe una consulta activa.
        if (_isLoading.value == true) {

            // Evitamos realizar dos consultas al mismo tiempo.
            return
        }

        // Activamos el indicador de carga.
        _isLoading.value = true

        // 10. Realizamos la petición a ExchangeRate-API.
        RetrofitClient.service.getExchangeRates(
            apiKey,
            fromCurrency
        ).enqueue(object : Callback<ExchangeRateResponse> {

            // 11. Se ejecuta cuando recibimos una respuesta.
            override fun onResponse(
                call: Call<ExchangeRateResponse>,
                response: Response<ExchangeRateResponse>
            ) {

                // Comprobamos que la respuesta HTTP sea correcta.
                if (response.isSuccessful) {

                    // Obtenemos el cuerpo de la respuesta.
                    val exchangeResponse = response.body()

                    // Buscamos el tipo de cambio de la moneda destino.
                    val rate =
                        exchangeResponse?.conversion_rates?.get(toCurrency)

                    // Verificamos que exista el tipo de cambio.
                    if (rate != null && CurrencyCalculator.isValidRate(rate)) {

                        // Calculamos el resultado de la conversión.
                        val result = try {
                            CurrencyCalculator.convert(amount, rate)
                        } catch (e: IllegalArgumentException) {

                            // Desactivamos el estado de carga.
                            _isLoading.value = false

                            // Informamos que el resultado no es valido.
                            _errorMessage.value = "El resultado de la conversion no es valido"

                            return
                        }

                        // Comprobamos que el resultado sea válido.
                        if (!result.isFinite()) {

                            // Desactivamos el estado de carga.
                            _isLoading.value = false

                            // Mostramos un mensaje de error.
                            _errorMessage.value =
                                "El resultado de la conversión no es válido"

                            // Detenemos el procesamiento.
                            return
                        }

                        // Guardamos el monto convertido.
                        currentAmount = amount

                        // Guardamos la moneda de origen.
                        currentFromCurrency = fromCurrency

                        // Guardamos la moneda de destino.
                        currentToCurrency = toCurrency

                        // Indicamos que existe un resultado nuevo.
                        isWaitingForResult = true

                        // Guardamos el tipo de cambio recibido.
                        _exchangeRate.value = rate

                        // Enviamos el resultado a la Activity.
                        _conversionResult.value = result

                        // Desactivamos el indicador de carga.
                        _isLoading.value = false

                    } else {

                        // Desactivamos el indicador de carga.
                        _isLoading.value = false

                        // Informamos que no encontramos el tipo de cambio.
                        _errorMessage.value =
                            "No se encontró el tipo de cambio"
                    }

                } else {

                    // Desactivamos el indicador de carga.
                    _isLoading.value = false

                    // Mostramos el código HTTP recibido.
                    _errorMessage.value =
                        "Error API: ${response.code()}"
                }
            }

            // 12. Se ejecuta cuando falla la comunicación con la API.
            override fun onFailure(
                call: Call<ExchangeRateResponse>,
                t: Throwable
            ) {

                // Desactivamos el indicador de carga.
                _isLoading.value = false

                // Comprobamos si ocurrió un error de conexión.
                if (t is IOException) {

                    // Informamos al usuario que revise su conexión.
                    _errorMessage.value =
                        "Error de conexión. Revisa tu Internet."

                } else {

                    // Mostramos el mensaje de error recibido.
                    _errorMessage.value =
                        "Ocurrió un error: ${t.message}"
                }
            }
        })
    }

    // 13. Comprobamos si existe una conversión pendiente de guardar.
    fun consumeConversionResult(): Boolean {

        // Verificamos si el resultado ya fue procesado.
        if (!isWaitingForResult) {

            // Evitamos guardar nuevamente el mismo resultado.
            return false
        }

        // Indicamos que el resultado ya fue utilizado.
        isWaitingForResult = false

        // Permitimos guardar la conversión en el historial.
        return true
    }
}