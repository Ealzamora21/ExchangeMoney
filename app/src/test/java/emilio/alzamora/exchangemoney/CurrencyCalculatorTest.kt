// Paquete de nuestras pruebas unitarias.
package emilio.alzamora.exchangemoney

import org.junit.Test
import org.junit.Assert.*
import emilio.alzamora.exchangemoney.scene.converter.utils.CurrencyCalculator

// Pruebas unitarias del calculador de monedas.
class CurrencyCalculatorTest {

    // 1. Verificamos una conversion de USD a MXN.
    @Test
    fun conversionUsdMxn() {
        assertEquals(
            1964.08,
            CurrencyCalculator.convert(100.0, 19.6408),
            0.0001
        )
    }

    // 2. Verificamos una conversion de USD a EUR.
    @Test
    fun conversionUsdEur() {
        assertEquals(
            456.05,
            CurrencyCalculator.convert(500.0, 0.9121),
            0.0001
        )
    }

    // 3. Verificamos una conversion con la misma moneda.
    @Test
    fun conversionMismaMoneda() {
        assertEquals(
            100.0,
            CurrencyCalculator.convert(100.0, 1.0),
            0.0001
        )
    }

    // 4. Verificamos un monto positivo.
    @Test
    fun montoPositivoValido() {
        assertTrue(CurrencyCalculator.isValidAmount(100.0))
    }

    // 5. Rechazamos un monto igual a cero.
    @Test
    fun montoCeroInvalido() {
        assertFalse(CurrencyCalculator.isValidAmount(0.0))
    }

    // 6. Rechazamos un monto negativo.
    @Test
    fun montoNegativoInvalido() {
        assertFalse(CurrencyCalculator.isValidAmount(-100.0))
    }

    // 7. Rechazamos valores infinitos.
    @Test
    fun montoInfinitoInvalido() {
        assertFalse(
            CurrencyCalculator.isValidAmount(Double.POSITIVE_INFINITY)
        )
    }

    // 8. Rechazamos valores que no representan numeros.
    @Test
    fun montoNaNInvalido() {
        assertFalse(CurrencyCalculator.isValidAmount(Double.NaN))
    }

    // 9. Verificamos un tipo de cambio positivo.
    @Test
    fun tipoCambioValido() {
        assertTrue(CurrencyCalculator.isValidRate(19.64))
    }

    // 10. Rechazamos un tipo de cambio igual a cero.
    @Test
    fun tipoCambioCeroInvalido() {
        assertFalse(CurrencyCalculator.isValidRate(0.0))
    }

    // 11. Rechazamos un tipo de cambio negativo.
    @Test
    fun tipoCambioNegativoInvalido() {
        assertFalse(CurrencyCalculator.isValidRate(-1.0))
    }

    // 12. Verificamos el manejo de montos incorrectos.
    @Test(expected = IllegalArgumentException::class)
    fun conversionMontoInvalido() {
        CurrencyCalculator.convert(-100.0, 19.64)
    }

    // 13. Verificamos el manejo de tasas incorrectas.
    @Test(expected = IllegalArgumentException::class)
    fun conversionTasaInvalida() {
        CurrencyCalculator.convert(100.0, 0.0)
    }

    // 14. Verificamos resultados demasiado grandes.
    @Test(expected = IllegalArgumentException::class)
    fun conversionResultadoInfinito() {
        CurrencyCalculator.convert(Double.MAX_VALUE, 2.0)
    }
}