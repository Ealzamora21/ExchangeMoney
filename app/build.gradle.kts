// Importamos Properties para leer el archivo local.properties.
import java.util.Properties

plugins {

    // Activamos el plugin para aplicaciones Android.
    alias(libs.plugins.android.application)
}

// Creamos un objeto para recuperar las propiedades locales.
val localProperties = Properties()

// Buscamos el archivo local.properties del proyecto.
val localPropertiesFile = rootProject.file("local.properties")

// Verificamos que el archivo exista.
if (localPropertiesFile.exists()) {

    // Cargamos las propiedades guardadas en el archivo.
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

// Recuperamos la clave de ExchangeRate-API.
val exchangeApiKey = localProperties.getProperty("EXCHANGE_API_KEY", "")

android {

    // Definimos el paquete principal de nuestra aplicación.
    namespace = "emilio.alzamora.exchangemoney"

    // Indicamos la versión del SDK utilizado para compilar.
    compileSdk {
        version = release(37)
    }

    // Configuración general de nuestra aplicación.
    defaultConfig {

        // Identificador de Exchange Money.
        applicationId = "emilio.alzamora.exchangemoney"

        // Versión mínima de Android compatible.
        minSdk = 24

        // Versión de Android a la que está dirigida la aplicación.
        targetSdk = 37

        // Número interno de la versión.
        versionCode = 1

        // Nombre de la versión de nuestra aplicación.
        versionName = "1.0"

        // Configuramos las pruebas instrumentadas.
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Guardamos la API key dentro de BuildConfig.
        buildConfigField(
            "String",
            "EXCHANGE_API_KEY",
            "\"$exchangeApiKey\""
        )
    }

    // Configuramos los tipos de compilación.
    buildTypes {

        // Configuración para la versión de publicación.
        release {

            // Conservamos la configuración original.
            optimization {
                enable = false
            }
        }
    }

    // Configuramos la compatibilidad con Java.
    compileOptions {

        // Versión de Java utilizada en el código fuente.
        sourceCompatibility = JavaVersion.VERSION_11

        // Versión de Java utilizada para compilar.
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Activamos las funcionalidades de compilación.
    buildFeatures {

        // Conectamos nuestros archivos XML con Kotlin.
        viewBinding = true

        // Generamos BuildConfig para recuperar la API key.
        buildConfig = true
    }
}

// Dependencias utilizadas en Exchange Money.
dependencies {

    // Dependencia para trabajar con Activities.
    implementation(libs.androidx.activity.ktx)

    // Dependencia para mantener la compatibilidad con Android.
    implementation(libs.androidx.appcompat)

    // Dependencia para organizar los elementos de las pantallas.
    implementation(libs.androidx.constraintlayout)

    // Dependencia con funciones adicionales de Kotlin para Android.
    implementation(libs.androidx.core.ktx)

    // Dependencia para utilizar componentes de Material Design.
    implementation(libs.material)

    // Retrofit permite realizar peticiones HTTP a ExchangeRate-API.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Gson transforma las respuestas JSON en objetos de Kotlin.
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Dependencia para realizar pruebas unitarias.
    testImplementation(libs.junit)

    // Dependencia para realizar pruebas de interfaz.
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependencia para realizar pruebas instrumentadas.
    androidTestImplementation(libs.androidx.junit)
}