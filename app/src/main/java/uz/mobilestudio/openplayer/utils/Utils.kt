package uz.mobilestudio.openplayer.utils

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import uz.mobilestudio.openplayer.R
import uz.mobilestudio.openplayer.entity.Song
import java.io.File
import java.io.FileDescriptor
import java.net.URI

object Utils {
    lateinit var sharedPreferences: SharedPreferences

    fun getIsShuffle(context: Context): Boolean {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isShuffle", false)
    }

    fun setIsShuffle(isShuffle: Boolean, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isShuffle", isShuffle).apply()
    }

    fun getIsRepeat(context: Context): Boolean {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isRepeat", false)
    }

    fun setIsRepeat(isRepeat: Boolean, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isRepeat", isRepeat).apply()
    }

    fun getLastSongId(context: Context): Long {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("songId", -1)
    }

    fun setLastSongId(songId: Long, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong("songId", songId).apply()
    }

    fun getUploadedDay(context: Context): Int {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("uploadedDay", -1)
    }

    fun setLanguage(lang: String, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("lang", lang).apply()
    }

    fun getLanguage(context: Context): String {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getString("lang", "")!!
    }

    fun uploadSong(day: Int, songId: Long, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong("uploadedSongId", songId).apply()
        sharedPreferences.edit().putInt("uploadedDay", day).apply()
    }

    fun downloadFileToSong(context: Context, songPath: String): Song {
        var song: Song? = null
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val path1: String = File(URI(songPath).path).canonicalPath
        val c: Cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.SIZE
            ),
            MediaStore.Audio.Media.DATA + " = ?", arrayOf(
                path1
            ),
            ""
        )!!

        if (null == c) {
            // ERROR
        }

        while (c.moveToNext()) {
            val mediaStoreId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID))
            val displayName = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
            val title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE))
            var artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val path = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA))
            val duration = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000
            var album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM))
            val albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val size = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.SIZE))
            val coverArt = ContentUris.withAppendedId(artworkUri, albumId).toString()
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mediaStoreId
            )
            if (artist == null) {
                artist = "artist"
            }
            if (album == null) {
                album = "album"
            }
            song = Song(
                mediaStoreId,
                contentUri.toString(),
                displayName,
                title,
                artist,
                path,
                duration,
                album,
                albumId,
                size,
                coverArt,
                0
            )
            println("song -> $song")
            return song
        }
        return song!!
    }

    fun getAlbumArt(context: Context, album_id: Long?): Bitmap? {
        var bm: Bitmap? = null
        try {
            val sArtworkUri = Uri
                .parse("content://media/external/audio/albumart")
            val uri = ContentUris.withAppendedId(sArtworkUri, album_id!!)
            val pfd: ParcelFileDescriptor = context.contentResolver
                .openFileDescriptor(uri, "r")!!
            if (pfd != null) {
                val fd: FileDescriptor = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd)
            }
        } catch (e: Exception) {
            bm = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.cover
            )
        }
        return bm
    }

//    private var resultLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                // There are no request codes
//                val data: Intent? = result.data
//            }
//        }
//
//    fun openSomeActivityForResult() {
//        val intent = Intent()
//        intent.type = "audio/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        resultLauncher.launch(intent)
//    }
}