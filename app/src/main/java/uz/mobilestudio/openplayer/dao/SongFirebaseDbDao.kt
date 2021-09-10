package uz.mobilestudio.openplayer.dao

import androidx.room.*
import io.reactivex.Flowable
import uz.mobilestudio.openplayer.entity.SongFirebaseDb

@Dao
interface SongFirebaseDbDao {
    @Insert
    fun addSongFirebaseDb(songFirebaseDb: SongFirebaseDb)

    @Delete
    fun deleteSongFirebaseDb(songFirebaseDb: SongFirebaseDb)

    @Update
    fun updateSongFirebaseDb(songFirebaseDb: SongFirebaseDb)

    @Query("delete from songFirebaseDb_table")
    fun deleteAllSongFirebaseDb()

    @Query("select * from songFirebaseDb_table")
    fun getAllSongFirebaseDb(): Flowable<List<SongFirebaseDb>>

    @Query("select *from songFirebaseDb_table where uid=:uid")
    fun getSongFirebaseDbByUid(uid: String): SongFirebaseDb?
}