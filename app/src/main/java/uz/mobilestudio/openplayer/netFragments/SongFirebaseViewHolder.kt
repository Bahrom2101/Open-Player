package uz.mobilestudio.openplayer.netFragments

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uz.mobilestudio.openplayer.databinding.ItemExploreBinding
import uz.mobilestudio.openplayer.models.SongFirebase

class SongFirebaseViewHolder(
    var itemExploreBinding: ItemExploreBinding,
    var onClickListener: OnClickListener
) :
    RecyclerView.ViewHolder(itemExploreBinding.root) {

    @SuppressLint("SetTextI18n")
    fun bind(songFirebase: SongFirebase, position: Int) {
        itemExploreBinding.title.text = songFirebase.title
        itemExploreBinding.artist.text = songFirebase.artist
        val duration = songFirebase.duration
        val seconds = duration!! % 60
        val minutes = duration / 60

        if (seconds <= 9) {
            itemExploreBinding.duration.text = "$minutes:0$seconds"
        } else {
            itemExploreBinding.duration.text = "$minutes:$seconds"
        }

        val size = songFirebase.size!! / 100000
        itemExploreBinding.size.text = "${size / 10}.${size % 10} MB"

        itemExploreBinding.options.setOnClickListener {
            onClickListener.onOptionsClick(songFirebase, position, itemExploreBinding.options)
        }

        itemExploreBinding.root.setOnClickListener {
            onClickListener.onPlayPauseClick(songFirebase, position, itemExploreBinding.playPause)
        }
    }

    interface OnClickListener {
        fun onPlayPauseClick(songFirebase: SongFirebase, position: Int, playPauseButton: ImageView)

        fun onOptionsClick(songFirebase: SongFirebase, position: Int, view: View)
    }

}