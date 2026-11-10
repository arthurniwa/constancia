package com.example.constancia.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * DTO (Data Transfer Object): o formato EXATO do JSON da API.
 * Exemplo de resposta de https://brasilapi.com.br/api/feriados/v1/2026 :
 *   [{"date":"2026-11-15","name":"Proclamação da República","type":"national"}, ...]
 * O Gson casa os nomes das propriedades com as chaves do JSON (≈ System.Text.Json).
 * Campos do JSON que não estão aqui (ex.: "weekday") são simplesmente ignorados.
 */
data class FeriadoDto(
    val date: String,   // "2026-11-15" (texto; convertemos para LocalDate no repositório)
    val name: String,
    val type: String
)

/**
 * Retrofit: você descreve a API como uma interface e ele gera a implementação
 * (mesma ideia do Room com os DAOs). ≈ Refit no .NET, ou um HttpClient tipado.
 */
interface BrasilApiService {

    // GET https://brasilapi.com.br/api/feriados/v1/{ano}
    // @Path substitui {ano} na URL. `suspend`: a chamada roda fora da thread da tela.
    // Se o servidor responder erro (404, 500...), o Retrofit lança HttpException.
    // Se não houver internet, lança IOException.
    @GET("api/feriados/v1/{ano}")
    suspend fun feriados(@Path("ano") ano: Int): List<FeriadoDto>
}
