package uz.jabborovbahrom.openplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jabborovbahrom.openplayer.databinding.ItemArtistBinding

class ArtistAdapter(
    var list: List<String>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<ArtistAdapter.Vh>() {
    inner class Vh(var itemArtistBinding: ItemArtistBinding) :
        RecyclerView.ViewHolder(itemArtistBinding.root) {
        fun onBind(artist: String) {
            itemArtistBinding.name.text = artist
            itemArtistBinding.root.setOnClickListener {
                onClickListener.onViewClick(artist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onViewClick(artist: String)
    }

}