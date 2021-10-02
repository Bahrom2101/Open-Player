package uz.jabborovbahrom.openplayer.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import jabborovbahrom.openplayer.databinding.FragmentReportBinding
import jabborovbahrom.openplayer.databinding.ItemReportBinding
import uz.jabborovbahrom.openplayer.models.Report
import uz.jabborovbahrom.openplayer.utils.NetworkHelper
import java.util.*

class ReportFragment : Fragment() {

    lateinit var binding: FragmentReportBinding
    lateinit var mAdapter: FirestorePagingAdapter<Report, ReportFirebaseViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mPostsCollection = mFirestore.collection("reports")
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
        binding = FragmentReportBinding.inflate(layoutInflater)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("reports")

        binding.rv.setHasFixedSize(true)

        sortBy = "type"
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
            popupMenu.menuInflater.inflate(R.menu.sort1, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {
                    R.id.by_type -> {
                        sortBy = "type"
                        setupAdapter()
                        binding.sortBy.text = "Type"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.by_isChecked -> {
                        sortBy = "checked"
                        setupAdapter()
                        binding.sortBy.text = "Is Checked"
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
        val options = FirestorePagingOptions.Builder<Report>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, Report::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<Report, ReportFirebaseViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ReportFirebaseViewHolder {
                val itemReportBinding =
                    ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ReportFirebaseViewHolder(itemReportBinding,
                    object : ReportFirebaseViewHolder.OnClickListener {
                        @SuppressLint("SimpleDateFormat")
                        override fun onPlayPauseClick(
                            report: Report,
                            position: Int,
                            playPauseButton: View
                        ) {
                            if (NetworkHelper(requireContext()).isNetworkConnected()) {
                                val intent = Intent(requireContext(),ReportInfoActivity::class.java)
                                val bundle = Bundle()
                                bundle.putSerializable("report",report)
                                intent.putExtra("bundle",bundle)
                                startActivity(intent)
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
                            report: Report,
                            position: Int,
                            view: View
                        ) {
                            optionsClick(report, view, position)
                        }
                    })
            }

            override fun onBindViewHolder(
                viewHolder: ReportFirebaseViewHolder,
                position: Int,
                report: Report
            ) {
                // Bind to ViewHolder
                viewHolder.bind(report, position)
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

    fun optionsClick(report: Report, view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.admin_options_explore1, popupMenu.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_report -> {
                    firebaseFirestore.collection("reports")
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val result = task.result
                                result?.forEach { queryDocumentSnapshot ->
                                    val report1 =
                                        queryDocumentSnapshot.toObject(Report::class.java)
                                    if (report.uid == report1.uid) {
                                        firebaseFirestore.collection("reports")
                                            .document(queryDocumentSnapshot.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                mAdapter.notifyItemRemoved(position)
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Report O'chirildi",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }
                        }
                    return@setOnMenuItemClickListener true
                }
                R.id.delete_song -> {
                    firebaseFirestore.collection("audios")
                        .document(report.uid!!)
                        .delete()
                        .addOnSuccessListener {
                            firebaseFirestore.collection("reports")
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val result = task.result
                                        result?.forEach { queryDocumentSnapshot ->
                                            val report1 =
                                                queryDocumentSnapshot.toObject(Report::class.java)
                                            if (report.uid == report1.uid) {
                                                firebaseFirestore.collection("reports")
                                                    .document(queryDocumentSnapshot.id)
                                                    .update("checked", true)
                                                    .addOnSuccessListener {
                                                        mAdapter.notifyItemChanged(position)
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Qo'shiq O'chirildi",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }
                                    }
                                }
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