package uz.mobilestudio.openplayer.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "songs_table")
data class Song(
    @PrimaryKey
    @ColumnInfo(name = "media_store_id")
    var mediaStoreId: Long,
    @ColumnInfo(name = "content_uri")
    var contentUri: String,
    @ColumnInfo(name = "display_name")
    var displayName: String,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "artist")
    var artist: String,
    @ColumnInfo(name = "path")
    var path: String,
    @ColumnInfo(name = "duration")
    var duration: Int,
    @ColumnInfo(name = "album")
    var album: String,
    @ColumnInfo(name = "album_id")
    var albumId: Long,
    @ColumnInfo(name = "size")
    var size: Int,
    @ColumnInfo(name = "cover_art")
    var coverArt: String,
    @ColumnInfo(name = "isUploaded")
    var isUploaded: Int
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.mediaStoreId)
        dest.writeString(this.contentUri)
        dest.writeString(this.displayName)
        dest.writeString(this.title)
        dest.writeString(this.artist)
        dest.writeString(this.path)
        dest.writeInt(this.duration)
        dest.writeString(this.album)
        dest.writeLong(this.albumId)
        dest.writeInt(this.size)
        dest.writeString(this.coverArt)
        dest.writeInt(this.isUploaded)
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}