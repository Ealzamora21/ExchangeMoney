// Paquete de nuestras pruebas instrumentadas.
package emilio.alzamora.exchangemoney

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import emilio.alzamora.exchangemoney.storage.PasswordUtils

// Pruebas del sistema de proteccion de contrasenas.
@RunWith(AndroidJUnit4::class)
class PasswordUtilsTest {

    // Utilizamos una contrasena ficticia para las pruebas.
    private val password = "PruebaSegura123"

    // 1. Comprobamos que no se almacene la contrasena original.
    @Test
    fun contrasenaProtegida() {

        val hash = PasswordUtils.hashPassword(password)

        assertNotEquals(password, hash)
        assertFalse(hash.contains(password))
    }

    // 2. Verificamos el formato del hash.
    @Test
    fun formatoPBKDF2Correcto() {

        val hash = PasswordUtils.hashPassword(password)

        assertTrue(hash.startsWith("PBKDF2:"))
        assertEquals(3, hash.split(":").size)
    }

    // 3. Verificamos una contrasena correcta.
    @Test
    fun contrasenaCorrecta() {

        val hash = PasswordUtils.hashPassword(password)

        assertTrue(
            PasswordUtils.verifyPassword(password, hash)
        )
    }

    // 4. Rechazamos una contrasena incorrecta.
    @Test
    fun contrasenaIncorrecta() {

        val hash = PasswordUtils.hashPassword(password)

        assertFalse(
            PasswordUtils.verifyPassword("Incorrecta123", hash)
        )
    }

    // 5. Verificamos que cada hash utilice un salt diferente.
    @Test
    fun saltAleatorio() {

        val hash1 = PasswordUtils.hashPassword(password)
        val hash2 = PasswordUtils.hashPassword(password)

        assertNotEquals(
            hash1.split(":")[1],
            hash2.split(":")[1]
        )
    }

    // 6. Rechazamos hashes con formatos incorrectos.
    @Test
    fun hashInvalido() {

        assertFalse(
            PasswordUtils.verifyPassword(password, "HASH_INVALIDO")
        )
    }

    // 7. Rechazamos hashes vacios.
    @Test
    fun hashVacio() {

        assertFalse(
            PasswordUtils.verifyPassword(password, "")
        )
    }

    // 8. Verificamos dos hashes diferentes de la misma contrasena.
    @Test
    fun hashesDiferentesValidos() {

        val hash1 = PasswordUtils.hashPassword(password)
        val hash2 = PasswordUtils.hashPassword(password)

        assertNotEquals(hash1, hash2)

        assertTrue(
            PasswordUtils.verifyPassword(password, hash1)
        )

        assertTrue(
            PasswordUtils.verifyPassword(password, hash2)
        )
    }
}