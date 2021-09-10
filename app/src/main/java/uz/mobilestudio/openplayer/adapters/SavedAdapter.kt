package uz.mobilestudio.openplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.mobilestudio.openplayer.databinding.ItemExploreBinding
import uz.mobilestudio.openplayer.entity.SongFirebaseDb

class SavedAdapter(
    var onClickListener: OnClickListener
) :
    ListAdapter<SongFirebaseDb, SavedAdapter.Vh>(MyDiffUtils()) {

    inner class Vh(var itemExploreBinding: ItemExploreBinding) :
        RecyclerView.ViewHolder(itemExploreBinding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(songFirebaseDb: SongFirebaseDb, position: Int) {
            itemExploreBinding.title.text = songFirebaseDb.title
            itemExploreBinding.artist.text = songFirebaseDb.artist
            val duration = songFirebaseDb.duration
            val seconds = duration % 60
            val minutes = duration / 60

            if (seconds <= 9) {
                itemExploreBinding.duration.text = "$minutes:0$seconds"
            } else {
                itemExploreBinding.duration.text = "$minutes:$seconds"
            }

            val size = songFirebaseDb.size / 100000
            itemExploreBinding.size.text = "${size / 10}.${size % 10} MB"

            itemExploreBinding.options.setOnClickListener {
                onClickListener.onOptionsClick(songFirebaseDb, position,itemExploreBinding.options)
            }

            itemExploreBinding.root.setOnClickListener {
                onClickListener.onPlayPauseClick(
                    songFirebaseDb,
                    position,
                    itemExploreBinding.playPause
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position),position)
    }

    class MyDiffUtils: DiffUtil.ItemCallback<SongFirebaseDb>() {
        override fun areItemsTheSame(oldItem: SongFirebaseDb, newItem: SongFirebaseDb): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: SongFirebaseDb, newItem: SongFirebaseDb): Boolean {
            return oldItem.equals(newItem)
        }

    }

    interface OnClickListener {
        fun onPlayPauseClick(songFirebaseDb: SongFirebaseDb, position: Int, playPauseButton: ImageView)

        fun onOptionsClick(songFirebaseDb: SongFirebaseDb, position: Int,view: View)
    }

}