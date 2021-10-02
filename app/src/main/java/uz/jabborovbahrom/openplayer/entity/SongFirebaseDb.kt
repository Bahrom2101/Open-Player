package uz.jabborovbahrom.openplayer.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "songFirebaseDb_table")
data class SongFirebaseDb(
    @PrimaryKey
    @ColumnInfo(name = "uid")
    var uid: String,
    @ColumnInfo(name = "display_name")
    var displayName: String,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "artist")
    var artist: String,
    @ColumnInfo(name = "duration")
    var duration: Int,
    @ColumnInfo(name = "size")
    var size: Int,
    @ColumnInfo(name = "downloadUrl")
    var downloadUrl: String,
    @ColumnInfo(name = "is_playing")
    var isPlaying: Boolean
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(displayName)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeInt(duration)
        parcel.writeInt(size)
        parcel.writeString(downloadUrl)
        parcel.writeByte(if (isPlaying) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongFirebaseDb> {
        override fun createFromParcel(parcel: Parcel): SongFirebaseDb {
            return SongFirebaseDb(parcel)
        }

        override fun newArray(size: Int): Array<SongFirebaseDb?> {
            return arrayOfNulls(size)
        }
    }

}
