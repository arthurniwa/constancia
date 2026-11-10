package com.example.constancia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * O banco em si ≈ o DbContext do Entity Framework.
 * - entities: as tabelas (≈ os DbSet<T>)
 * - version: versão do esquema. Mudou uma entidade? Sobe a versão (≈ uma migration).
 * - exportSchema = false: não gera o JSON do esquema (não precisamos para o trabalho).
 */
@Database(
    entities = [Habito::class, RegistroDiario::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)   // registra os conversores de LocalDate, Set<DayOfWeek>...
abstract class ConstanciaDatabase : RoomDatabase() {   // `:` = herança (igual ao C#)

    // O Room gera a implementação destes métodos.
    abstract fun habitoDao(): HabitoDao
    abstract fun registroDiarioDao(): RegistroDiarioDao

    // `companion object` ≈ membros `static` de uma classe C#.
    companion object {
        // @Volatile: toda thread enxerga o valor mais recente (≈ `volatile` do C#).
        @Volatile
        private var instancia: ConstanciaDatabase? = null

        /**
         * Singleton: abrir o banco é caro, então o app inteiro usa uma única instância
         * (≈ registrar o DbContext como Singleton, ou um `Lazy<T>` estático).
         */
        fun obter(context: Context): ConstanciaDatabase =
            // Se já existe, devolve. Senão, entra no `synchronized` (≈ `lock` do C#) e cria.
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    ConstanciaDatabase::class.java,
                    "constancia.db"                 // nome do arquivo SQLite no aparelho
                )
                    // Se a versão subir sem migration, apaga e recria o banco.
                    // Aceitável num trabalho acadêmico; num app real escreveríamos Migrations.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }        // `also` executa algo com o objeto e o devolve
            }
    }
}
