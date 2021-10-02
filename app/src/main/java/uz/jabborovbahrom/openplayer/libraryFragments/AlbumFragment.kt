package uz.jabborovbahrom.openplayer.libraryFragments

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.FragmentAlbumBinding
import uz.jabborovbahrom.openplayer.adapters.SongAdapter
import uz.jabborovbahrom.openplayer.db.AppDatabase
import uz.jabborovbahrom.openplayer.entity.Song

class AlbumFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    lateinit var binding: FragmentAlbumBinding
    lateinit var appDatabase: AppDatabase
    lateinit var songAdapter: SongAdapter
    lateinit var allSongs: List<Song>

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAlbumBinding.inflate(layoutInflater)
        appDatabase = AppDatabase.getInstance(requireContext())

        val artist = arguments?.getString("artist", "")

        if (artist == "") {
            val byteArray: ByteArray = arguments?.getByteArray("image")!!
            val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            val album = arguments?.getString("album", "")
            binding.image.setImageBitmap(bmp)
            binding.name.text = album
            allSongs = appDatabase.songDao().getAllSongByAlbumName(album!!)
            binding.count.text = "${allSongs.size} ${getString(R.string.song)}"
        } else {
            binding.name.text = artist
            allSongs = appDatabase.songDao().getAllSongByArtistName(artist!!)
            binding.count.text = "${allSongs.size} ${getString(R.string.song)}"
        }

        songAdapter = SongAdapter(allSongs, object : SongAdapter.OnClickListener {
            override fun onViewClick(song: Song, position: Int) {
                val bundle = Bundle()
                bundle.putSerializable("song", song)
                bundle.putInt("pos", position)
                val songs = ArrayList<Parcelable>()
                songs.addAll(allSongs)
                bundle.putParcelableArrayList("songs", songs)
                findNavController().navigate(R.id.playMusicFragment, bundle)
            }
        })
        binding.rv.adapter = songAdapter

        return binding.root
    }
}