package uz.mobilestudio.openplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.mobilestudio.openplayer.databinding.ItemUploadBinding
import uz.mobilestudio.openplayer.entity.Song

class UploadAdapter(
    var list: List<Song>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<UploadAdapter.Vh>() {
    inner class Vh(var itemUploadBinding: ItemUploadBinding) :
        RecyclerView.ViewHolder(itemUploadBinding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(song: Song, position: Int) {
            itemUploadBinding.title.text = song.title
            itemUploadBinding.artist.text = song.artist
            val duration = song.duration
            val seconds = duration % 60
            val minutes = duration / 60

            if (seconds <= 9) {
                itemUploadBinding.duration.text = "$minutes:0$seconds"
            } else {
                itemUploadBinding.duration.text = "$minutes:$seconds"
            }

            val size = song.size / 100000
            itemUploadBinding.size.text = "${size/10}.${size%10} MB"

            itemUploadBinding.root.setOnClickListener {
                if (song.isUploaded == 0) {
                    onClickListener.onUploadClick(song, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onUploadClick(song: Song, position: Int)
    }

}