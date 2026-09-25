// Paquete donde se encuentra nuestra pantalla de perfil.
package emilio.alzamora.exchangemoney.scene.profile.view

// Importamos Manifest para utilizar los permisos.
import android.Manifest

// Importamos Intent para navegar entre pantallas.
import android.content.Intent

// Importamos la excepción para controlar errores de la cámara.
import android.content.ActivityNotFoundException

// Importamos PackageManager para verificar permisos.
import android.content.pm.PackageManager

// Importamos Bitmap para trabajar con fotografías.
import android.graphics.Bitmap

// Importamos BitmapFactory para recuperar fotografías guardadas.
import android.graphics.BitmapFactory

// Importamos Bundle para el ciclo de vida de la Activity.
import android.os.Bundle

// Importamos Log para mostrar mensajes en Logcat.
import android.util.Log

// Importamos Toast para mostrar mensajes al usuario.
import android.widget.Toast

// Importamos los contratos para utilizar la cámara.
import androidx.activity.result.contract.ActivityResultContracts

// Importamos la clase base de nuestra Activity.
import androidx.appcompat.app.AppCompatActivity

// Importamos ContextCompat para verificar permisos.
import androidx.core.content.ContextCompat

// Importamos ViewModelProvider para utilizar el ViewModel.
import androidx.lifecycle.ViewModelProvider

// Importamos los recursos de nuestra aplicación.
import emilio.alzamora.exchangemoney.R

// Importamos el Binding de la pantalla de perfil.
import emilio.alzamora.exchangemoney.databinding.ActivityProfileBinding

// Importamos la pantalla del conversor.
import emilio.alzamora.exchangemoney.scene.converter.view.ConverterActivity

// Importamos la pantalla del historial.
import emilio.alzamora.exchangemoney.scene.history.view.HistoryActivity

// Importamos la pantalla de inicio de sesión.
import emilio.alzamora.exchangemoney.scene.login.view.LoginActivity

// Importamos nuestro ViewModel del perfil.
import emilio.alzamora.exchangemoney.scene.profile.viewModel.ProfileViewModel

// Importamos nuestra clase de almacenamiento local.
import emilio.alzamora.exchangemoney.storage.ConcreteSharedPreference

// Importamos File para trabajar con archivos internos.
import java.io.File

// Importamos FileOutputStream para guardar la fotografía.
import java.io.FileOutputStream

// Importamos MessageDigest para generar un identificador del usuario.
import java.security.MessageDigest

// Clase encargada de mostrar la pantalla de perfil.
class ProfileActivity : AppCompatActivity() {

    // 1. Identificador para los mensajes de Logcat.
    private val tag = "ProfileActivity"

    // 2. Binding para acceder a los elementos del XML.
    private lateinit var binding: ActivityProfileBinding

    // 3. ViewModel encargado de manejar los datos del perfil.
    private lateinit var viewModel: ProfileViewModel

    // 4. Almacenamiento local de los datos del usuario.
    private lateinit var sharedPreference: ConcreteSharedPreference

    // 5. Registramos el resultado de la cámara.
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        // Comprobamos que el usuario haya tomado una fotografía.
        if (bitmap != null) {

            // Enviamos la fotografía al ViewModel.
            viewModel.savePhoto(bitmap)

            // Guardamos la fotografía en el almacenamiento interno.
            savePhotoToStorage(bitmap)

        } else {

            // Informamos que no se tomó ninguna fotografía.
            Toast.makeText(
                this,
                "No se tomó ninguna foto",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 6. Registramos la respuesta del permiso de cámara.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        // Comprobamos si el usuario aceptó el permiso.
        if (isGranted) {

            // Abrimos la cámara.
            openCamera()

        } else {

            // Informamos que necesitamos el permiso.
            Toast.makeText(
                this,
                "Necesitamos el permiso de cámara",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 7. Se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Ejecutamos el método de la clase padre.
        super.onCreate(savedInstanceState)

        // Registramos la creación de la pantalla.
        Log.d(tag, "onCreate: Se creó el perfil")

        // Conectamos la Activity con su archivo XML.
        binding = ActivityProfileBinding.inflate(layoutInflater)

        // Mostramos el diseño de la pantalla.
        setContentView(binding.root)

        // Inicializamos nuestro ViewModel.
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Inicializamos el almacenamiento local.
        sharedPreference = ConcreteSharedPreference(this)

        // Configuramos las acciones de los botones.
        setupListeners()

        // Configuramos los observadores del ViewModel.
        setupObservers()

        // Configuramos el menú inferior.
        setupBottomNavigation()

        // Recuperamos los datos del usuario.
        loadUser()

        // Recuperamos la fotografía guardada anteriormente.
        loadSavedPhoto()
    }

    // 8. Recuperamos los datos del usuario registrado.
    private fun loadUser() {

        // Recuperamos el nombre del usuario.
        val name = sharedPreference.getName()

        // Recuperamos el correo electrónico.
        val email = sharedPreference.getEmail()

        // Enviamos los datos al ViewModel.
        viewModel.loadProfile(name, email)
    }

    // 9. Configuramos las acciones de los botones.
    private fun setupListeners() {

        // Detectamos cuando presionamos Tomar foto.
        binding.btnTakePhoto.setOnClickListener {

            // Comprobamos el permiso de cámara.
            checkCameraPermission()
        }

        // Detectamos cuando presionamos Cerrar sesión.
        binding.btnLogout.setOnClickListener {

            // Cerramos la sesión del usuario.
            logout()
        }
    }

    // 10. Observamos los datos enviados por el ViewModel.
    private fun setupObservers() {

        // Observamos los datos del perfil.
        viewModel.profile.observe(this) { profile ->

            // Mostramos el nombre del usuario.
            binding.tvName.text = profile.name

            // Mostramos el correo electrónico.
            binding.tvEmail.text = profile.email
        }

        // Observamos los cambios en la fotografía.
        viewModel.photo.observe(this) { bitmap ->

            // Eliminamos el espacio del icono original.
            binding.ivProfilePhoto.setPadding(0, 0, 0, 0)

            // Mostramos la fotografía del usuario.
            binding.ivProfilePhoto.setImageBitmap(bitmap)
        }
    }

    // --------------------------------------------------
    // ALMACENAMIENTO DE LA FOTOGRAFÍA
    // --------------------------------------------------

    // 11. Obtenemos el archivo de fotografía del usuario.
    private fun getPhotoFile(): File {

        // Recuperamos el correo electrónico registrado.
        val email = sharedPreference.getEmail().trim().lowercase()

        // Generamos un identificador a partir del correo.
        val digest = MessageDigest.getInstance("SHA-256")

        // Convertimos el correo en una secuencia de bytes.
        val hash = digest.digest(email.toByteArray(Charsets.UTF_8))

        // Convertimos los bytes en un identificador de texto.
        val userId = hash.joinToString("") { "%02x".format(it) }

        // Creamos un nombre de archivo para cada usuario.
        val fileName = "profile_$userId.jpg"

        // Devolvemos la ubicación dentro del almacenamiento interno.
        return File(filesDir, fileName)
    }

    // 12. Guardamos la fotografía en el almacenamiento interno.
    private fun savePhotoToStorage(bitmap: Bitmap) {

        // Intentamos guardar la fotografía.
        try {

            // Obtenemos el archivo correspondiente al usuario.
            val photoFile = getPhotoFile()

            // Abrimos el archivo para guardar la imagen.
            FileOutputStream(photoFile).use { output ->

                // Guardamos la fotografía en formato JPG.
                val saved = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    output
                )

                // Verificamos que la imagen se haya guardado.
                if (!saved) {

                    // Generamos un error si no se pudo guardar.
                    throw java.io.IOException("No se pudo guardar la foto")
                }
            }

            // Informamos que la fotografía fue guardada.
            Toast.makeText(
                this,
                "Fotografía guardada",
                Toast.LENGTH_SHORT
            ).show()

            // Registramos el resultado en Logcat.
            Log.d(tag, "Fotografía guardada correctamente")

        } catch (e: Exception) {

            // Informamos que ocurrió un error.
            Toast.makeText(
                this,
                "No se pudo guardar la fotografía",
                Toast.LENGTH_SHORT
            ).show()

            // Registramos el error en Logcat.
            Log.e(tag, "Error al guardar la fotografía", e)
        }
    }

    // 13. Recuperamos la fotografía guardada.
    private fun loadSavedPhoto() {

        // Intentamos recuperar la fotografía.
        try {

            // Obtenemos el archivo del usuario.
            val photoFile = getPhotoFile()

            // Comprobamos que exista una fotografía guardada.
            if (photoFile.exists()) {

                // Convertimos el archivo en una imagen Bitmap.
                val bitmap = BitmapFactory.decodeFile(
                    photoFile.absolutePath
                )

                // Verificamos que la fotografía sea válida.
                if (bitmap != null) {

                    // Enviamos la fotografía recuperada al ViewModel.
                    viewModel.savePhoto(bitmap)

                    // Registramos el resultado en Logcat.
                    Log.d(tag, "Fotografía recuperada correctamente")
                }
            }

        } catch (e: Exception) {

            // Registramos el error al recuperar la fotografía.
            Log.e(tag, "Error al recuperar la fotografía", e)
        }
    }

    // --------------------------------------------------
    // CÁMARA Y PERMISOS
    // --------------------------------------------------

    // 14. Comprobamos el permiso de cámara.
    private fun checkCameraPermission() {

        // Consultamos el estado actual del permiso.
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        // Verificamos si el permiso fue concedido.
        if (permission == PackageManager.PERMISSION_GRANTED) {

            // Abrimos directamente la cámara.
            openCamera()

        } else {

            // Solicitamos el permiso al usuario.
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 15. Función encargada de abrir la cámara.
    private fun openCamera() {

        // Intentamos abrir la cámara del dispositivo.
        try {

            // Iniciamos la captura de una fotografía.
            cameraLauncher.launch(null)

        } catch (e: ActivityNotFoundException) {

            // Informamos si no existe una cámara disponible.
            Toast.makeText(
                this,
                "No se encontró una cámara disponible",
                Toast.LENGTH_SHORT
            ).show()

            // Registramos el error en Logcat.
            Log.e(tag, "No se pudo abrir la cámara", e)
        }
    }

    // --------------------------------------------------
    // CIERRE DE SESIÓN
    // --------------------------------------------------

    // 16. Función encargada de cerrar la sesión.
    private fun logout() {

        // Desactivamos la sesión guardada.
        sharedPreference.logout()

        // Creamos un Intent hacia la pantalla de Login.
        val intent = Intent(this, LoginActivity::class.java)

        // Eliminamos las pantallas anteriores.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Informamos que la sesión fue cerrada.
        Toast.makeText(
            this,
            "Sesión cerrada correctamente",
            Toast.LENGTH_SHORT
        ).show()

        // Abrimos la pantalla de inicio de sesión.
        startActivity(intent)

        // Cerramos la pantalla actual.
        finish()
    }

    // --------------------------------------------------
    // NAVEGACIÓN INFERIOR
    // --------------------------------------------------

    // 17. Configuramos el menú inferior.
    private fun setupBottomNavigation() {

        // Marcamos Perfil como la opción seleccionada.
        binding.bottomNavigation.selectedItemId = R.id.nav_profile

        // Detectamos la opción seleccionada.
        binding.bottomNavigation.setOnItemSelectedListener { item ->

            // Comprobamos qué opción seleccionó el usuario.
            when (item.itemId) {

                // Opción correspondiente al conversor.
                R.id.nav_converter -> {

                    // Creamos un Intent hacia el conversor.
                    val intent = Intent(
                        this,
                        ConverterActivity::class.java
                    )

                    // Recuperamos el conversor si ya está abierto.
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                    // Abrimos la pantalla del conversor.
                    startActivity(intent)

                    // Cerramos la pantalla de perfil.
                    finish()

                    // Conservamos la selección antes de salir.
                    false
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

                    // Cerramos la pantalla de perfil.
                    finish()

                    // Conservamos la selección antes de salir.
                    false
                }

                // Opción correspondiente al perfil.
                R.id.nav_profile -> {

                    // Permanecemos en la pantalla actual.
                    true
                }

                // Controlamos cualquier opción desconocida.
                else -> false
            }
        }
    }

    // --------------------------------------------------
    // CICLO DE VIDA DE LA ACTIVITY
    // --------------------------------------------------

    // 18. Se ejecuta cuando la pantalla se vuelve visible.
    override fun onStart() {

        // Ejecutamos el método de la clase padre.
        super.onStart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStart: Perfil visible")
    }

    // 19. Se ejecuta cuando podemos interactuar con la pantalla.
    override fun onResume() {

        // Ejecutamos el método de la clase padre.
        super.onResume()

        // Registramos el evento en Logcat.
        Log.d(tag, "onResume: Perfil activo")
    }

    // 20. Se ejecuta cuando la pantalla pierde el enfoque.
    override fun onPause() {

        // Ejecutamos el método de la clase padre.
        super.onPause()

        // Registramos el evento en Logcat.
        Log.d(tag, "onPause: Perfil pausado")
    }

    // 21. Se ejecuta cuando la pantalla deja de ser visible.
    override fun onStop() {

        // Ejecutamos el método de la clase padre.
        super.onStop()

        // Registramos el evento en Logcat.
        Log.d(tag, "onStop: Perfil no visible")
    }

    // 22. Se ejecuta cuando regresamos a la pantalla.
    override fun onRestart() {

        // Ejecutamos el método de la clase padre.
        super.onRestart()

        // Registramos el evento en Logcat.
        Log.d(tag, "onRestart: Regresamos al perfil")
    }

    // 23. Se ejecuta cuando se destruye la pantalla.
    override fun onDestroy() {

        // Ejecutamos el método de la clase padre.
        super.onDestroy()

        // Registramos el evento en Logcat.
        Log.d(tag, "onDestroy: Perfil cerrado")
    }
}