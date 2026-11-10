package com.example.constancia.data.repository

import com.example.constancia.data.remote.BrasilApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/**
 * Resultado da busca de feriados.
 * `sealed interface` = conjunto FECHADO de possibilidades (≈ discriminated union / OneOf).
 * Com `when`, o compilador obriga a tratar todos os casos — não dá para esquecer o erro.
 */
sealed interface ResultadoFeriados {
    // Map data → nome do feriado, ex.: 2026-11-15 → "Proclamação da República"
    data class Sucesso(val feriados: Map<LocalDate, String>) : ResultadoFeriados
    data class Erro(val mensagem: String) : ResultadoFeriados
}

class FeriadoRepository(private val api: BrasilApiService) {

    // Cache em memória: cada ano é buscado na API uma única vez enquanto o app estiver aberto.
    private val cache = mutableMapOf<Int, Map<LocalDate, String>>()
    // Mutex ≈ SemaphoreSlim(1,1): se duas telas pedirem ao mesmo tempo, só uma vai à API.
    private val mutex = Mutex()

    suspend fun buscar(anos: Set<Int>): ResultadoFeriados = mutex.withLock {
        try {
            for (ano in anos) {
                if (ano !in cache) {
                    cache[ano] = api.feriados(ano).associate { LocalDate.parse(it.date) to it.name }
                }
            }
            // flatMap junta os Maps dos anos pedidos num só (≈ SelectMany).
            ResultadoFeriados.Sucesso(anos.flatMap { cache[it].orEmpty().entries }.associate { it.key to it.value })
        } catch (e: IOException) {
            // Sem internet, DNS falhou, tempo esgotado...
            ResultadoFeriados.Erro("Sem conexão com a internet. Os feriados não puderam ser carregados.")
        } catch (e: HttpException) {
            // A API respondeu, mas com erro (ex.: 500 = servidor fora do ar).
            ResultadoFeriados.Erro("O serviço de feriados está indisponível no momento (erro ${e.code()}).")
        } catch (e: Exception) {
            // JSON inesperado ou outra falha qualquer: o app não pode fechar por causa disso.
            ResultadoFeriados.Erro("Não foi possível carregar os feriados.")
        }
    }
}
