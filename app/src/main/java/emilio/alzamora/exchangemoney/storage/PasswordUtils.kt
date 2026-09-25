// Paquete donde manejamos el almacenamiento y seguridad.
package emilio.alzamora.exchangemoney.storage

// Importamos Base64 para convertir los bytes en texto.
import android.util.Base64

// Importamos SecureRandom para generar valores aleatorios.
import java.security.SecureRandom

// Importamos MessageDigest para comparar los hashes.
import java.security.MessageDigest

// Importamos las herramientas para proteger contraseñas.
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Objeto encargado de proteger y verificar las contraseñas.
object PasswordUtils {

    // Número de iteraciones utilizadas para generar el hash.
    private const val ITERATIONS = 600000

    // Tamaño del salt en bytes.
    private const val SALT_LENGTH = 16

    // Tamaño del hash en bits.
    private const val KEY_LENGTH = 256

    // Generamos un salt aleatorio para cada contraseña.
    private fun generateSalt(): ByteArray {

        // Creamos un arreglo de bytes.
        val salt = ByteArray(SALT_LENGTH)

        // Llenamos el arreglo con valores aleatorios seguros.
        SecureRandom().nextBytes(salt)

        // Devolvemos el salt generado.
        return salt
    }

    // Generamos el hash utilizando PBKDF2.
    private fun generateHash(
        password: String,
        salt: ByteArray
    ): ByteArray {

        // Configuramos la contraseña y los parámetros del algoritmo.
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )

        // Seleccionamos el algoritmo compatible con Android API 24.
        val factory = SecretKeyFactory.getInstance(
            "PBKDF2WithHmacSHA1"
        )

        // Generamos y devolvemos el hash.
        return try {
            factory.generateSecret(spec).encoded
        } finally {

            // Limpiamos la contraseña utilizada por PBEKeySpec.
            spec.clearPassword()
        }
    }

    // Protegemos una contraseña antes de almacenarla.
    fun hashPassword(password: String): String {

        // Generamos un salt aleatorio.
        val salt = generateSalt()

        // Generamos el hash de la contraseña.
        val hash = generateHash(password, salt)

        // Convertimos el salt en texto.
        val saltText = Base64.encodeToString(
            salt,
            Base64.NO_WRAP
        )

        // Convertimos el hash en texto.
        val hashText = Base64.encodeToString(
            hash,
            Base64.NO_WRAP
        )

        // Devolvemos el salt y el hash en un solo texto.
        return "PBKDF2:$saltText:$hashText"
    }

    // Verificamos si una contraseña coincide con el hash guardado.
    fun verifyPassword(
        password: String,
        storedPassword: String
    ): Boolean {

        // Intentamos verificar la contraseña.
        return try {

            // Separamos el identificador, el salt y el hash.
            val parts = storedPassword.split(":")

            // Verificamos que el formato sea correcto.
            if (parts.size != 3 || parts[0] != "PBKDF2") {
                return false
            }

            // Recuperamos el salt original.
            val salt = Base64.decode(
                parts[1],
                Base64.NO_WRAP
            )

            // Recuperamos el hash almacenado.
            val storedHash = Base64.decode(
                parts[2],
                Base64.NO_WRAP
            )

            // Generamos el hash de la contraseña ingresada.
            val newHash = generateHash(password, salt)

            // Comparamos ambos hashes de forma segura.
            MessageDigest.isEqual(storedHash, newHash)

        } catch (e: Exception) {

            // Devolvemos false si ocurre algún error.
            false
        }
    }
}