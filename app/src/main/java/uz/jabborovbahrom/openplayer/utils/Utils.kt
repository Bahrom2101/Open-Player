package uz.jabborovbahrom.openplayer.utils

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import jabborovbahrom.openplayer.R
import uz.jabborovbahrom.openplayer.entity.Song
import java.io.File
import java.io.FileDescriptor
import java.net.URI

object Utils {
    lateinit var sharedPreferences: SharedPreferences

    fun getWork(context: Context): Boolean {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isWorking", false)
    }

    fun setWork(isWorking: Boolean, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isWorking", isWorking).apply()
    }
    fun getPermission(context: Context): Boolean {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("permission", false)
    }

    fun setPermission(permission: Boolean, context: Context) {
        sharedPreferences = context.getSharedPreferences("OPEN_PLAYER_ATTR", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("permission", permission).apply()
    }

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
}