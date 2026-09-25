package emilio.alzamora.exchangemoney.api.service

import emilio.alzamora.exchangemoney.api.model.ExchangeRateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateService {

    // 1. Realizamos una petición GET a ExchangeRate-API.
    //
    // {apiKey} será reemplazado por nuestra API Key.
    // {baseCurrency} será reemplazado por la moneda
    // seleccionada por el usuario, por ejemplo USD.
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String
    ): Call<ExchangeRateResponse>
}