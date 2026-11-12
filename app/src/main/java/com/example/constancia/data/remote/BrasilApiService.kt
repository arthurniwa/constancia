package com.example.constancia.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

/** DTO: formato do JSON da API. Ex.: {"date":"2026-11-15","name":"...","type":"national"} */
data class FeriadoDto(
    val date: String,   // texto; vira LocalDate no repositório
    val name: String,
    val type: String
)

/** Retrofit: descrevemos a API numa interface e ele gera a implementação. */
interface BrasilApiService {

    // GET https://brasilapi.com.br/api/feriados/v1/{ano}
    // Erro do servidor → HttpException; sem internet → IOException.
    @GET("api/feriados/v1/{ano}")
    suspend fun feriados(@Path("ano") ano: Int): List<FeriadoDto>
}
