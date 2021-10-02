package uz.jabborovbahrom.openplayer.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jabborovbahrom.openplayer.databinding.ItemAlbumBinding

class AlbumAdapter(
    var list: List<String>,
    var bmList: List<Bitmap>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<AlbumAdapter.Vh>() {
    inner class Vh(var itemAlbumBinding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(itemAlbumBinding.root) {
        fun onBind(album: String, bm: Bitmap) {
            itemAlbumBinding.image.setImageBitmap(bm)
            itemAlbumBinding.album.text = album
            itemAlbumBinding.root.setOnClickListener {
                onClickListener.onViewClick(album, bm)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position], bmList[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onViewClick(album: String, bm: Bitmap)
    }
}