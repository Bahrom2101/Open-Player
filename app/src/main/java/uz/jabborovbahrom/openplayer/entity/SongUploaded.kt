package uz.jabborovbahrom.openplayer.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songsUploaded_table")
data class SongUploaded(
    @PrimaryKey
    @ColumnInfo(name = "media_store_id")
    var mediaStoreId: Long
)
