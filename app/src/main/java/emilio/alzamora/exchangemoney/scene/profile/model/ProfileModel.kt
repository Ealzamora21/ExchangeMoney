// Paquete donde se encuentra nuestro modelo de perfil.
package emilio.alzamora.exchangemoney.scene.profile.model

// 1. Creamos el modelo con los datos del usuario.
data class ProfileModel(

    // Almacena el nombre del usuario registrado.
    val name: String,

    // Almacena el correo electrónico del usuario.
    val email: String
)