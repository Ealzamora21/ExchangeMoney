// Paquete donde manejamos los calculos del conversor.
package emilio.alzamora.exchangemoney.scene.converter.utils

// Objeto encargado de validar y calcular conversiones.
object CurrencyCalculator {

    // Verificamos que el monto sea valido.
    fun isValidAmount(amount: Double): Boolean {
        return amount.isFinite() && amount > 0
    }

    // Verificamos que el tipo de cambio sea valido.
    fun isValidRate(rate: Double): Boolean {
        return rate.isFinite() && rate > 0
    }

    // Calculamos el resultado de una conversion.
    fun convert(amount: Double, rate: Double): Double {

        // Validamos los datos antes del calculo.
        require(isValidAmount(amount)) {
            "Monto no valido"
        }

        require(isValidRate(rate)) {
            "Tipo de cambio no valido"
        }

        // Realizamos la conversion.
        val result = amount * rate

        // Comprobamos que el resultado sea valido.
        require(result.isFinite()) {
            "Resultado no valido"
        }

        return result
    }
}