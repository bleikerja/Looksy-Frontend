package com.example.looksy

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.looksy.dao.ClothesDao
import com.example.looksy.dataClassClones.Clothes

// 1. Die @Database-Annotation konfiguriert die Datenbank
@Database(entities = [Clothes::class], version = 1, exportSchema = false)
// 2. Deine Enum-Klassen müssen für Room umgewandelt werden.
//    Füge hier alle deine TypeConverter hinzu.
@TypeConverters(Converters::class) // WICHTIG!
abstract class ClothesDatabase : RoomDatabase() {
    abstract fun clothesDao(): ClothesDao
    companion object {
        // Das @Volatile sorgt dafür, dass die INSTANCE-Variable
        // immer für alle Threads aktuell ist.
        @Volatile
        private var INSTANCE: ClothesDatabase? = null

        // Diese Funktion ist der einzige Weg, eine Instanz der Datenbank zu bekommen.
        fun getDatabase(context: Context): ClothesDatabase {
            // Wir prüfen, ob schon eine Instanz existiert.
            // Wenn ja, geben wir sie zurück und sparen uns die teure Neuerstellung.
            return INSTANCE ?: synchronized(this) {
                // `synchronized` stellt sicher, dass nicht zwei Threads gleichzeitig
                // versuchen, eine neue Datenbank zu erstellen.

                // Hier wird die Datenbank tatsächlich gebaut.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClothesDatabase::class.java,
                    "clothes_database" // So wird die Datenbank-Datei auf dem Gerät heißen
                )
                    // Optional: Füge hier Fallback-Strategien für Migrationen hinzu
                    .fallbackToDestructiveMigration() // Einfachste Lösung: Bei Versions-Upgrade wird die DB gelöscht.
                    .build()

                // Wir weisen die neu erstellte Instanz unserer Variable zu...
                INSTANCE = instance
                // ...und geben sie zurück.
                instance
            }
        }
    }
}