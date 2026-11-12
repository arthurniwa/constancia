package com.example.constancia.data.repository

import com.example.constancia.data.remote.BrasilApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/**
 * Resultado da busca: Sucesso ou Erro. `sealed interface` (como union fechada) =
 * o `when` obriga a tratar os dois casos.
 */
sealed interface ResultadoFeriados {
    data class Sucesso(val feriados: Map<LocalDate, String>) : ResultadoFeriados
    data class Erro(val mensagem: String) : ResultadoFeriados
}

class FeriadoRepository(private val api: BrasilApiService) {

    // Cache em memória: cada ano vai à API uma vez só.
    private val cache = mutableMapOf<Int, Map<LocalDate, String>>()
    // Mutex: duas telas pedindo ao mesmo tempo → só uma chama a API.
    private val mutex = Mutex()

    suspend fun buscar(anos: Set<Int>): ResultadoFeriados = mutex.withLock {
        try {
            for (ano in anos) {
                if (ano !in cache) {
                    cache[ano] = api.feriados(ano).associate { LocalDate.parse(it.date) to it.name }
                }
            }
            ResultadoFeriados.Sucesso(anos.flatMap { cache[it].orEmpty().entries }.associate { it.key to it.value })
        } catch (e: IOException) {
            // Sem internet / tempo esgotado.
            ResultadoFeriados.Erro("Sem conexão com a internet. Os feriados não puderam ser carregados.")
        } catch (e: HttpException) {
            // A API respondeu com erro (ex.: 500).
            ResultadoFeriados.Erro("O serviço de feriados está indisponível no momento (erro ${e.code()}).")
        } catch (e: Exception) {
            // Qualquer outra falha: o app não pode fechar por isso.
            ResultadoFeriados.Erro("Não foi possível carregar os feriados.")
        }
    }
}
