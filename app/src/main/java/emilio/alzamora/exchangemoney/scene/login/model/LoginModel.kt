package emilio.alzamora.exchangemoney.scene.login.model

// Modelo que representa los datos necesarios
// para iniciar sesión en Exchange Money.
data class LoginModel(
    val user: String,
    val password: String
)