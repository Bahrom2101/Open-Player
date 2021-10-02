package uz.jabborovbahrom.openplayer.dao

import androidx.room.*
import uz.jabborovbahrom.openplayer.entity.Song

@Dao
interface SongDao {
    @Insert
    fun addSong(song: Song)

    @Delete
    fun deleteSong(song: Song)

    @Update
    fun updateSong(song: Song)

    @Query("delete from songs_table")
    fun deleteAllSong()

    @Query("select * from songs_table")
    fun getAllSong(): List<Song>

    @Query("select * from songs_table where lower(display_name) like :search ")
    fun search(search: String): List<Song>

    @Query("select * from songs_table where album=:album")
    fun getAllSongByAlbumName(album: String): List<Song>

    @Query("select * from songs_table where isUploaded=0")
    fun getAllSongNotUploaded(): List<Song>

    @Query("select * from songs_table where artist=:artist")
    fun getAllSongByArtistName(artist: String): List<Song>

    @Query("select * from songs_table where content_uri=:contentUri")
    fun getSongByContentUri(contentUri: String): Song

    @Query("select distinct album from songs_table")
    fun getAllAlbumsName(): List<String>

    @Query("select album_id from songs_table where album=:album limit 1")
    fun getAlbumIdByAlbumName(album: String): Long

    @Query("select distinct artist from songs_table")
    fun getAllArtistsName(): List<String>

    @Query("select *from songs_table where media_store_id=:id")
    fun getSongById(id: Long): Song?

    @Query("select *from songs_table where media_store_id=(select max(media_store_id) from songs_table)")
    fun getLastSong(): Song
}