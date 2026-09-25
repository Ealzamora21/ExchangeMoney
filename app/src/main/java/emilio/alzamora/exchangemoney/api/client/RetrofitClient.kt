package emilio.alzamora.exchangemoney.api.client

import emilio.alzamora.exchangemoney.api.service.ExchangeRateService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 1. Dirección base de ExchangeRate-API.
    // Todas nuestras peticiones comenzarán desde esta URL.
    private const val BASE_URL =
        "https://v6.exchangerate-api.com/"

    // 2. Creamos y configuramos Retrofit.
    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // 3. Creamos nuestro servicio para poder realizar
    // las peticiones definidas en ExchangeRateService.
    val service: ExchangeRateService =
        retrofit.create(ExchangeRateService::class.java)
}