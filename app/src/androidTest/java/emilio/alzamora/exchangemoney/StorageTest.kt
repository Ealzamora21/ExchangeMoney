// Paquete de nuestras pruebas instrumentadas.
package emilio.alzamora.exchangemoney

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import org.junit.runner.RunWith
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference
import emilio.alzamora.exchangemoney.storage.PasswordUtils

// Pruebas del almacenamiento local de Exchange Money.
@RunWith(AndroidJUnit4::class)
class StorageTest {

    private lateinit var context: Context
    private lateinit var storage: ConcreteSharedPreference

    // Preparamos un almacenamiento independiente de la app.
    @Before
    fun prepararPrueba() {

        // Utilizamos el contexto del APK de pruebas.
        context = InstrumentationRegistry
            .getInstrumentation().context

        limpiarDatos()

        storage = ConcreteSharedPreference(context, "ExchangeMoneyTestPreferences")
    }

    // Eliminamos solamente los datos del entorno de pruebas.
    @After
    fun finalizarPrueba() {
        limpiarDatos()
    }

    private fun limpiarDatos() {

        context.getSharedPreferences(
            "ExchangeMoneyTestPreferences",
            Context.MODE_PRIVATE
        ).edit().clear().commit()
    }

    // 1. Comprobamos el almacenamiento del nombre y correo.
    @Test
    fun guardarDatosUsuario() {

        storage.saveUser(
            "Usuario Prueba",
            "test@example.com",
            "Prueba123"
        )

        assertEquals("Usuario Prueba", storage.getName())
        assertEquals("test@example.com", storage.getEmail())
    }

    // 2. Verificamos que la contrasena se almacene protegida.
    @Test
    fun guardarContrasenaProtegida() {

        storage.saveUser(
            "Usuario Prueba",
            "test@example.com",
            "Prueba123"
        )

        assertNotEquals("Prueba123", storage.getPassword())

        assertTrue(
            PasswordUtils.verifyPassword(
                "Prueba123",
                storage.getPassword()
            )
        )
    }

    // 3. Comprobamos que se elimine la contrasena en texto plano.
    @Test
    fun contrasenaTextoPlanoEliminada() {

        storage.saveUser(
            "Usuario Prueba",
            "test@example.com",
            "Prueba123"
        )

        val preferences = context.getSharedPreferences(
            "ExchangeMoneyTestPreferences",
            Context.MODE_PRIVATE
        )

        assertFalse(preferences.contains("USER_PASSWORD"))
        assertTrue(preferences.contains("USER_PASSWORD_HASH"))
    }

    // 4. Verificamos la deteccion de una cuenta registrada.
    @Test
    fun detectarCuentaRegistrada() {

        assertFalse(storage.hasRegisteredUser())

        storage.saveUser(
            "Usuario Prueba",
            "test@example.com",
            "Prueba123"
        )

        assertTrue(storage.hasRegisteredUser())
    }

    // 5. Comprobamos que inicialmente no exista una sesion.
    @Test
    fun sesionInicialInactiva() {
        assertFalse(storage.isLoggedIn())
    }

    // 6. Verificamos la persistencia de la sesion.
    @Test
    fun sesionPersistente() {

        storage.saveSession(true)

        val newStorage = ConcreteSharedPreference(context, "ExchangeMoneyTestPreferences")

        assertTrue(newStorage.isLoggedIn())
    }

    // 7. Comprobamos el cierre de sesion.
    @Test
    fun cerrarSesion() {

        storage.saveSession(true)
        storage.logout()

        assertFalse(storage.isLoggedIn())
    }

    // 8. Comprobamos el historial inicialmente vacio.
    @Test
    fun historialVacio() {
        assertTrue(storage.getConversionHistory().isEmpty())
    }

    // 9. Verificamos que una conversion se almacene.
    @Test
    fun guardarConversion() {

        val conversion = "100 USD a 1900 MXN"

        storage.saveConversion(conversion)

        assertEquals(
            listOf(conversion),
            storage.getConversionHistory()
        )
    }

    // 10. Verificamos que aparezcan primero las conversiones recientes.
    @Test
    fun historialOrdenado() {

        storage.saveConversion("Conversion 1")
        storage.saveConversion("Conversion 2")
        storage.saveConversion("Conversion 3")

        assertEquals(
            listOf(
                "Conversion 3",
                "Conversion 2",
                "Conversion 1"
            ),
            storage.getConversionHistory()
        )
    }

    // 11. Comprobamos que se conserven las conversiones repetidas.
    @Test
    fun historialConDuplicados() {

        storage.saveConversion("Conversion repetida")
        storage.saveConversion("Conversion repetida")

        assertEquals(
            2,
            storage.getConversionHistory().size
        )
    }

    // 12. Verificamos la persistencia del historial.
    @Test
    fun historialPersistente() {

        storage.saveConversion("Conversion guardada")

        val newStorage = ConcreteSharedPreference(context, "ExchangeMoneyTestPreferences")

        assertEquals(
            listOf("Conversion guardada"),
            newStorage.getConversionHistory()
        )
    }

    // 13. Comprobamos la eliminacion del historial.
    @Test
    fun eliminarHistorial() {

        storage.saveConversion("Conversion 1")
        storage.saveConversion("Conversion 2")

        storage.clearConversionHistory()

        assertTrue(storage.getConversionHistory().isEmpty())
    }

    // 14. Verificamos la migracion de contrasenas antiguas.
    @Test
    fun migrarContrasenaAntigua() {

        val preferences = context.getSharedPreferences(
            "ExchangeMoneyTestPreferences",
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putString("USER_PASSWORD", "Antigua123")
            .commit()

        storage.migratePassword("Antigua123")

        assertFalse(preferences.contains("USER_PASSWORD"))
        assertTrue(preferences.contains("USER_PASSWORD_HASH"))

        assertTrue(
            PasswordUtils.verifyPassword(
                "Antigua123",
                storage.getPassword()
            )
        )
    }
}