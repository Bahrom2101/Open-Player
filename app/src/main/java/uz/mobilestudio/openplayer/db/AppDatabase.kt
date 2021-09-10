package uz.mobilestudio.openplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.mobilestudio.openplayer.dao.SongDao
import uz.mobilestudio.openplayer.dao.SongFirebaseDbDao
import uz.mobilestudio.openplayer.dao.SongUploadedDao
import uz.mobilestudio.openplayer.entity.Song
import uz.mobilestudio.openplayer.entity.SongFirebaseDb
import uz.mobilestudio.openplayer.entity.SongUploaded

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