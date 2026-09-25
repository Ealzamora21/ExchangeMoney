package emilio.alzamora.exchangemoney.scene.converter.model

// Modelo que representa los datos necesarios
// para realizar una conversión de moneda.
data class ConverterModel(
    val amount: Double,
    val fromCurrency: String,
    val toCurrency: String
)