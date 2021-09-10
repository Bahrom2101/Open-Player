package uz.mobilestudio.openplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.mobilestudio.openplayer.databinding.ItemSongBinding
import uz.mobilestudio.openplayer.entity.Song
import uz.mobilestudio.openplayer.services.SongService

class SongDownloadedAdapter(
    var onClickListener: OnClickListener
) :
    ListAdapter<Song, SongDownloadedAdapter.Vh>(MyDiffUtils()) {

    inner class Vh(var itemSongBinding: ItemSongBinding) :
        RecyclerView.ViewHolder(itemSongBinding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(song: Song, position: Int) {
            itemSongBinding.title.text = song.title
            itemSongBinding.artist.text = song.artist
            val duration = song.duration
            val seconds = duration % 60
            val minutes = duration / 60
            if (SongService.getCurrentSong() != null)
                if (song.mediaStoreId == SongService.getCurrentSong()?.mediaStoreId) {
                    itemSongBinding.playButton.visibility = View.VISIBLE
                } else {
                    itemSongBinding.playButton.visibility = View.INVISIBLE
                }
            if (seconds <= 9) {
                itemSongBinding.duration.text = "$minutes:0$seconds"
            } else {
                itemSongBinding.duration.text = "$minutes:$seconds"
            }
            itemSongBinding.root.setOnClickListener {
                onClickListener.onViewClick(song, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)
    }

    class MyDiffUtils: DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaStoreId == newItem.mediaStoreId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.equals(newItem)
        }

    }

    interface OnClickListener {
        fun onViewClick(song: Song, position: Int)
    }

}