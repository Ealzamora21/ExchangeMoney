package emilio.alzamora.exchangemoney.scene.register.model

// Modelo que representa los datos de un usuario
// al momento de crear una cuenta.
data class RegisterModel(
    val name: String,
    val email: String,
    val password: String
)