package emilio.alzamora.exchangemoney.scene.history.model

// Modelo que representa una conversión
// guardada dentro del historial.
data class HistoryModel(

    // 1. Monto original que ingresó el usuario.
    val amount: Double,

    // 2. Moneda de origen.
    val fromCurrency: String,

    // 3. Moneda de destino.
    val toCurrency: String,

    // 4. Tipo de cambio utilizado.
    val exchangeRate: Double,

    // 5. Resultado final de la conversión.
    val result: Double
)