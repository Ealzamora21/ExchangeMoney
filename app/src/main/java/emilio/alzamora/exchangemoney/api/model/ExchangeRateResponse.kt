package emilio.alzamora.exchangemoney.api.model

// Modelo que representa la respuesta que recibimos
// desde ExchangeRate-API.
data class ExchangeRateResponse(

    // Indica si la petición fue exitosa.
    val result: String,

    // Moneda que utilizamos como base.
    val base_code: String,

    // Lista de monedas con sus respectivos
    // tipos de cambio.
    val conversion_rates: Map<String, Double>
)