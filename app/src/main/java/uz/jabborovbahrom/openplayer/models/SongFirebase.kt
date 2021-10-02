package uz.jabborovbahrom.openplayer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class SongFirebase : Serializable, Parcelable {
    var uid: String? = null
    var displayName: String? = null
    var title: String? = null
    var artist: String? = null
    var duration: Int? = null
    var size: Int? = null
    var downloadUrl: String? = null
    var isPlaying: Boolean? = null
    var isCheckedByAdmin: Boolean? = null
    var isShow: Boolean? = null
    var time: Long? = null

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()
        displayName = parcel.readString()
        title = parcel.readString()
        artist = parcel.readString()
        duration = parcel.readValue(Int::class.java.classLoader) as? Int
        size = parcel.readValue(Int::class.java.classLoader) as? Int
        downloadUrl = parcel.readString()
        isPlaying = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isCheckedByAdmin = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isShow = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        time = parcel.readValue(Long::class.java.classLoader) as? Long
    }

    constructor()
    constructor(
        uid: String?,
        displayName: String?,
        title: String?,
        artist: String?,
        duration: Int?,
        size: Int?,
        downloadUrl: String?,
        isPlaying: Boolean?,
        isCheckedByAdmin: Boolean?,
        isShow: Boolean?,
        time: Long?
    ) {
        this.uid = uid
        this.displayName = displayName
        this.title = title
        this.artist = artist
        this.duration = duration
        this.size = size
        this.downloadUrl = downloadUrl
        this.isPlaying = isPlaying
        this.isCheckedByAdmin = isCheckedByAdmin
        this.isShow = isShow
        this.time = time
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(displayName)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeValue(duration)
        parcel.writeValue(size)
        parcel.writeString(downloadUrl)
        parcel.writeValue(isPlaying)
        parcel.writeValue(isCheckedByAdmin)
        parcel.writeValue(isShow)
        parcel.writeValue(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongFirebase> {
        override fun createFromParcel(parcel: Parcel): SongFirebase {
            return SongFirebase(parcel)
        }

        override fun newArray(size: Int): Array<SongFirebase?> {
            return arrayOfNulls(size)
        }
    }


}