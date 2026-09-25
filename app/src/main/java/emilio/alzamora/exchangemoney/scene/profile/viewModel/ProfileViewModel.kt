// Paquete donde se encuentra nuestro ViewModel.
package emilio.alzamora.exchangemoney.scene.profile.viewModel

// Importamos Bitmap para manejar la fotografía del usuario.
import android.graphics.Bitmap

// Importamos LiveData para observar los datos.
import androidx.lifecycle.LiveData

// Importamos MutableLiveData para modificar los datos.
import androidx.lifecycle.MutableLiveData

// Importamos ViewModel para manejar la lógica del perfil.
import androidx.lifecycle.ViewModel

// Importamos nuestro modelo de perfil.
import emilio.alzamora.exchangemoney.scene.profile.model.ProfileModel

// Clase que contiene la lógica de la pantalla de perfil.
class ProfileViewModel : ViewModel() {

    // 1. Almacenamos internamente los datos del usuario.
    private val _profile = MutableLiveData<ProfileModel>()

    // 2. Permitimos que la Activity observe los datos.
    val profile: LiveData<ProfileModel> = _profile

    // 3. Almacenamos internamente la fotografía del usuario.
    private val _photo = MutableLiveData<Bitmap>()

    // 4. Permitimos que la Activity observe la fotografía.
    val photo: LiveData<Bitmap> = _photo

    // 5. Recibimos el nombre y correo del usuario registrado.
    fun loadProfile(name: String, email: String) {

        // Creamos el modelo con los datos recibidos.
        val profileModel = ProfileModel(name, email)

        // Enviamos los datos a la Activity mediante LiveData.
        _profile.value = profileModel
    }

    // 6. Recibimos la fotografía tomada con la cámara.
    fun savePhoto(bitmap: Bitmap) {

        // Actualizamos la fotografía para mostrarla en la pantalla.
        _photo.value = bitmap
    }
}