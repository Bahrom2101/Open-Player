package uz.jabborovbahrom.openplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.jabborovbahrom.openplayer.dao.SongDao
import uz.jabborovbahrom.openplayer.dao.SongFirebaseDbDao
import uz.jabborovbahrom.openplayer.dao.SongUploadedDao
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.entity.SongFirebaseDb
import uz.jabborovbahrom.openplayer.entity.SongUploaded

@Database(entities = [Song::class, SongUploaded::class, SongFirebaseDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun songUploadedDao(): SongUploadedDao
    abstract fun songFirebaseDbDao(): SongFirebaseDbDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "songs_db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }

            return instance!!
        }
    }
}