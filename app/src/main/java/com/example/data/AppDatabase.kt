package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Reward::class, ChestHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lootboxDao(): LootboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lootbox_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.lootboxDao())
                }
            }
        }

        suspend fun populateDatabase(dao: LootboxDao) {
            if (dao.getRewardsCount() == 0) {
                val defaultRewards = listOf(
                    // Mythics (0.5%)
                    Reward(name = "Foice Ceifadora de Almas", rarity = "MITICO", icon = "💀"),
                    Reward(name = "Orbe Primordial do Caos", rarity = "MITICO", icon = "☄️"),
                    Reward(name = "Tridente do Destino Eterno", rarity = "MITICO", icon = "🔱"),
                    Reward(name = "Divindade Estelar Ancestral", rarity = "MITICO", icon = "🌌"),

                    // Legendaries (3%)
                    Reward(name = "Dragão de Ouro Ancestral", rarity = "LENDARIO", icon = "🐲"),
                    Reward(name = "Espada Celestial Cósmica", rarity = "LENDARIO", icon = "⚔️"),
                    Reward(name = "Cálice de Fogo Sagrado", rarity = "LENDARIO", icon = "🍷"),
                    Reward(name = "Asas de Fênix Divina", rarity = "LENDARIO", icon = "🪽"),
                    Reward(name = "Coroa Real de Diamantes", rarity = "LENDARIO", icon = "👑"),

                    // Epics (12%)
                    Reward(name = "Escudo Rúnico do Sol", rarity = "EPICO", icon = "🛡️"),
                    Reward(name = "Anel do Eclipse Lunar", rarity = "EPICO", icon = "💍"),
                    Reward(name = "Amuleto de Olho Místico", rarity = "EPICO", icon = "🧿"),
                    Reward(name = "Poção de Mana Infinita", rarity = "EPICO", icon = "🧪"),
                    Reward(name = "Besta Cósmica Veloz", rarity = "EPICO", icon = "🏹"),
                    Reward(name = "Cetro Mágico de Jade", rarity = "EPICO", icon = "🪄"),

                    // Commons (84.5%)
                    Reward(name = "Saco de Moedas de Bronze", rarity = "COMUM", icon = "🪙"),
                    Reward(name = "Poção de Vida Menor", rarity = "COMUM", icon = "🧪"),
                    Reward(name = "Pergaminho de Teleporte", rarity = "COMUM", icon = "📜"),
                    Reward(name = "Minério de Ferro Bruto", rarity = "COMUM", icon = "🪵"),
                    Reward(name = "Maçã de Energia Silvestre", rarity = "COMUM", icon = "🍎"),
                    Reward(name = "Chave de Ferro Antiga", rarity = "COMUM", icon = "🔑"),
                    Reward(name = "Lâmina Curta Enferrujada", rarity = "COMUM", icon = "🔪"),
                    Reward(name = "Pão de Trigo Fresco", rarity = "COMUM", icon = "🍞")
                )
                dao.insertRewards(defaultRewards)
            }
        }
    }
}
