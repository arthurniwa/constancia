package com.example.constancia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** O banco Room: lista as tabelas (entities) e a versão do esquema. */
@Database(
    entities = [Usuario::class, Habito::class, RegistroDiario::class],
    version = 2,   // subiu para 2 quando entrou a tabela usuarios
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ConstanciaDatabase : RoomDatabase() {

    // O Room gera a implementação destes métodos.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun habitoDao(): HabitoDao
    abstract fun registroDiarioDao(): RegistroDiarioDao

    // companion object: membros "estáticos" da classe (como static no C#).
    companion object {
        @Volatile
        private var instancia: ConstanciaDatabase? = null

        /** Singleton: o app inteiro usa uma única instância do banco. */
        fun obter(context: Context): ConstanciaDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    ConstanciaDatabase::class.java,
                    "constancia.db"
                )
                    // Versão subiu sem migration? Apaga e recria o banco (ok para trabalho acadêmico).
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
