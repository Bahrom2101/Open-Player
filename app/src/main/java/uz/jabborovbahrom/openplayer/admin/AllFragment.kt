package uz.jabborovbahrom.openplayer.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.paging.PagedList
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.CustomPlayBinding
import jabborovbahrom.openplayer.databinding.FragmentAllBinding
import jabborovbahrom.openplayer.databinding.ItemExploreBinding
import uz.jabborovbahrom.openplayer.models.SongFirebase
import uz.jabborovbahrom.openplayer.netFragments.SongFirebaseViewHolder
import uz.jabborovbahrom.openplayer.utils.NetworkHelper
import java.util.*

class AllFragment : Fragment() {

    lateinit var binding: FragmentAllBinding
    lateinit var mAdapter: FirestorePagingAdapter<SongFirebase, SongFirebaseViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mPostsCollection = mFirestore.collection("audios")
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var sortBy: String
    lateinit var descAsc: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllBinding.inflate(layoutInflater)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("audios")

        binding.rv.setHasFixedSize(true)

        sortBy = "time"
        descAsc = "Descending"
        setupAdapter()
        binding.swipeRefreshLayout.setOnRefreshListener {
            mAdapter.refresh()
        }

        binding.descAsc.setOnClickListener {
            if (binding.descAsc.text.toString() == "Descending") {
                descAsc = "Ascending"
                setupAdapter()
                binding.descAsc.text = "Ascending"
            } else {
                descAsc = "Descending"
                setupAdapter()
                binding.descAsc.text = "Descending"
            }
        }

        binding.sortBy.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.sort, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {
                    R.id.by_time -> {
                        sortBy = "time"
                        setupAdapter()
                        binding.sortBy.text = "Time"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.by_size -> {
                        sortBy = "size"
                        setupAdapter()
                        binding.sortBy.text = "Size"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.by_title -> {
                        sortBy = "title"
                        setupAdapter()
                        binding.sortBy.text = "Title"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.by_duration -> {
                        sortBy = "duration"
                        setupAdapter()
                        binding.sortBy.text = "Duration"
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
            popupMenu.show()
        }

        return binding.root
    }

    private fun setupAdapter() {
        val mQuery = if (descAsc == "Descending") {
            mPostsCollection.orderBy(sortBy, Query.Direction.DESCENDING)
        } else {
            mPostsCollection.orderBy(sortBy, Query.Direction.ASCENDING)
        }
        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(5)
            .setPageSize(3)
            .build()

        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<SongFirebase>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, SongFirebase::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<SongFirebase, SongFirebaseViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): SongFirebaseViewHolder {
                val itemExploreBinding =
                    ItemExploreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return SongFirebaseViewHolder(itemExploreBinding,
                    object : SongFirebaseViewHolder.OnClickListener {
                        @SuppressLint("SimpleDateFormat")
                        override fun onPlayPauseClick(
                            songFirebase: SongFirebase,
                            position: Int,
                            playPauseButton: ImageView
                        ) {
                            if (NetworkHelper(requireContext()).isNetworkConnected()) {
                                try {
                                    playFirebase(
                                        songFirebase.title!!,
                                        songFirebase.downloadUrl!!,
                                        songFirebase.duration!!
                                    )
                                } catch (e: java.lang.Exception) {
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.internet_is_not_connected),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        @SuppressLint("SimpleDateFormat")
                        override fun onOptionsClick(
                            songFirebase: SongFirebase,
                            position: Int,
                            view: View
                        ) {
                            optionsClick(songFirebase, view, position)
                        }
                    })
            }

            override fun onBindViewHolder(
                viewHolder: SongFirebaseViewHolder,
                position: Int,
                songFirebase: SongFirebase
            ) {
                // Bind to ViewHolder
                viewHolder.bind(songFirebase, position)
            }


            override fun onError(e: Exception) {
                super.onError(e)
                Log.e("MainActivity", e.message!!)
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.LOADING_MORE -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.LOADED -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_occurred),
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }

        // Finally Set the Adapter to RecyclerView
        binding.rv.adapter = mAdapter

    }

    fun optionsClick(songFirebase: SongFirebase, view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.admin_options_explore, popupMenu.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    firebaseFirestore.collection("audios")
                        .document(songFirebase.uid!!)
                        .delete()
                        .addOnSuccessListener {
                            reference.child(songFirebase.uid!!).delete()
                            mAdapter.notifyItemRemoved(position)
                            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                        }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    @SuppressLint("SetTextI18n")
    fun playFirebase(title: String, downloadUrl: String, duration: Int) {
        if (NetworkHelper(requireContext()).isNetworkConnected()) {
            val handler = Handler(Looper.getMainLooper())
            val playBinding = CustomPlayBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(requireContext()).create()
            dialog.setView(playBinding.root)
            val seconds = duration % 60
            val minutes = duration / 60
            if (seconds <= 9)
                playBinding.duration.text = "$minutes:0$seconds"
            else
                playBinding.duration.text = "$minutes:$seconds"
            playBinding.title.text = title

            val mediaPlayer = MediaPlayer()

            dialog.setOnShowListener {
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.setDataSource(downloadUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    playBinding.progressBar.visibility = View.GONE
                }

                mediaPlayer.setOnCompletionListener {
                    playBinding.playPause.setImageResource(R.drawable.ic_play)
                }

                playBinding.seekBar.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }
                })
                playBinding.seekBar.max = duration * 1000
                val runnable = object : Runnable {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        try {
                            playBinding.seekBar.progress =
                                mediaPlayer.currentPosition
                            val min =
                                mediaPlayer.currentPosition / 1000 / 60
                            val sek =
                                mediaPlayer.currentPosition / 1000 % 60
                            var time = ""
                            time += if (min < 10)
                                "0$min"
                            else
                                "$min"
                            time += ":"
                            time += if (sek < 10)
                                "0$sek"
                            else
                                "$sek"
                            playBinding.currentTime.text = time
                            handler.postDelayed(this, 100)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                handler.postDelayed(runnable, 100)
                playBinding.playPause.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        playBinding.playPause.setImageResource(R.drawable.ic_play)
                        mediaPlayer.pause()
                    } else {
                        playBinding.playPause.setImageResource(R.drawable.ic_pause)
                        mediaPlayer.start()
                    }
                }
            }

            dialog.setOnDismissListener {
                handler.removeCallbacksAndMessages(null)
                mediaPlayer.reset()
                mediaPlayer.stop()
            }
            dialog.show()

        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.internet_is_not_connected),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }
}