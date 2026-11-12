package com.example.constancia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** O banco Room: lista as tabelas (entities) e a versão do esquema. */
@Database(
    entities = [Usuario::class, Habito::class, RegistroDiario::class],
    version = 4,   // 2 = tabela usuarios; 3 = coluna fotoCaminho (perfil); 4 = coluna unidade (hábitos)
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

        // Migração 2 → 3: só adiciona a coluna nova, sem apagar nada (≈ uma migration do EF).
        // Fica dentro do companion object para o obter() (que é "estático") conseguir usar.
        val MIGRACAO_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN fotoCaminho TEXT")
            }
        }

        // Migração 3 → 4: unidade da meta ("copos", "km"...).
        val MIGRACAO_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habitos ADD COLUMN unidade TEXT")
            }
        }

        /** Singleton: o app inteiro usa uma única instância do banco. */
        fun obter(context: Context): ConstanciaDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    ConstanciaDatabase::class.java,
                    "constancia.db"
                )
                    .addMigrations(MIGRACAO_2_3, MIGRACAO_3_4)   // preservam os dados
                    // Versão sem migration? Apaga e recria o banco (ok para trabalho acadêmico).
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
