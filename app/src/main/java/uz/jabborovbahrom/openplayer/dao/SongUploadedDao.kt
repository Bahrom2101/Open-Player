package uz.jabborovbahrom.openplayer.dao

import androidx.room.*
import uz.jabborovbahrom.openplayer.entity.SongUploaded

@Dao
interface SongUploadedDao {
    @Insert
    fun addSong(songUploaded: SongUploaded)

    @Delete
    fun deleteSongUploaded(songUploaded: SongUploaded)

    @Update
    fun updateSongUploaded(songUploaded: SongUploaded)

    @Query("delete from songsUploaded_table")
    fun deleteAllSongUploaded()

    @Query("select * from songsUploaded_table")
    fun getAllSongUploaded(): List<SongUploaded>

    @Query("select *from songsUploaded_table where media_store_id=:id")
    fun getSongUploadedById(id: Long): SongUploaded?

    @Query("select *from songsUploaded_table where media_store_id=(select max(media_store_id) from songsUploaded_table)")
    fun getLastSongUploaded(): SongUploaded
}