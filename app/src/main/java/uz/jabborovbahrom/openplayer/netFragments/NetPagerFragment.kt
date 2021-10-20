package uz.jabborovbahrom.openplayer.netFragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.grpc.okhttp.internal.Util
import io.reactivex.schedulers.Schedulers
import jabborovbahrom.openplayer.R
import jabborovbahrom.openplayer.databinding.*
import uz.jabborovbahrom.openplayer.adapters.SavedAdapter
import uz.jabborovbahrom.openplayer.adapters.SongDownloadedAdapter
import uz.jabborovbahrom.openplayer.adapters.UploadAdapter
import uz.jabborovbahrom.openplayer.db.AppDatabase
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.entity.SongFirebaseDb
import uz.jabborovbahrom.openplayer.models.Permission
import uz.jabborovbahrom.openplayer.models.Report
import uz.jabborovbahrom.openplayer.models.SongFirebase
import uz.jabborovbahrom.openplayer.services.SongService
import uz.jabborovbahrom.openplayer.services.UploadService
import uz.jabborovbahrom.openplayer.utils.NetworkHelper
import uz.jabborovbahrom.openplayer.utils.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"

class NetPagerFragment : Fragment() {
    private var param1: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }
    }

    lateinit var binding: FragmentNetPagerBinding
    lateinit var appDatabase: AppDatabase
    lateinit var mAdapter: FirestorePagingAdapter<SongFirebase, SongFirebaseViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mPostsCollection = mFirestore.collection("audios")
    private val mQuery = mPostsCollection.orderBy("time", Query.Direction.DESCENDING)
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNetPagerBinding.inflate(layoutInflater)
        appDatabase = AppDatabase.getInstance(requireContext())

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("audios")

        binding.rv.setHasFixedSize(true)

        when (param1) {
            1 -> {
                setupAdapter()
                binding.swipeRefreshLayout.setOnRefreshListener {
                    mAdapter.refresh()
                }
            }
            2 -> {
                binding.swipeRefreshLayout.isEnabled = false
                val allDownloaded = ArrayList<Song>()
                val songDownloadedAdapter =
                    SongDownloadedAdapter(object : SongDownloadedAdapter.OnClickListener {
                        override fun onViewClick(song: Song, position: Int) {
                            val bundle = Bundle()
                            bundle.putInt("pos", position)
                            val songs = ArrayList<Parcelable>()
                            songs.addAll(allDownloaded)
                            bundle.putParcelableArrayList("songs", songs)
                            findNavController().navigate(R.id.playMusicFragment, bundle)
                        }
                    })

                val directory = File(
                    Environment.getExternalStorageDirectory(),
                    File.separator.toString() + Environment.DIRECTORY_MUSIC + "/Open Player"
                )

                for (file in directory.listFiles()) {
                    val byPath = getByPath(file.path, requireContext())
                    allDownloaded.add(byPath)
                }
                songDownloadedAdapter.submitList(allDownloaded)
                binding.rv.adapter = songDownloadedAdapter
            }
            3 -> {
                binding.swipeRefreshLayout.isEnabled = false
                val savedAdapter =
                    SavedAdapter(object : SavedAdapter.OnClickListener {
                        override fun onPlayPauseClick(
                            songFirebaseDb: SongFirebaseDb,
                            position: Int,
                            playPauseButton: ImageView
                        ) {
                            if (NetworkHelper(requireContext()).isNetworkConnected()) {
                                if (!Utils.getPermission(requireContext())) {
                                    popupDialog()
                                } else {
                                    playFirebase(
                                        songFirebaseDb.title,
                                        songFirebaseDb.downloadUrl,
                                        songFirebaseDb.duration
                                    )
                                    val intent =
                                        Intent(requireActivity(), SongService::class.java)
                                    intent.action = SongService.ACTION_PAUSE
                                    requireActivity().startService(intent)
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.internet_is_not_connected),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onOptionsClick(
                            songFirebaseDb: SongFirebaseDb,
                            position: Int,
                            view: View
                        ) {
                            val popupMenu = PopupMenu(requireContext(), view)
                            popupMenu.menuInflater.inflate(
                                R.menu.popup_options_explore1,
                                popupMenu.menu
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                popupMenu.setForceShowIcon(true)
                            }
                            popupMenu.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.save1 -> {
                                        appDatabase.songFirebaseDbDao()
                                            .deleteSongFirebaseDb(songFirebaseDb)
                                        return@setOnMenuItemClickListener true
                                    }
                                    R.id.download1 -> {
                                        if (NetworkHelper(requireContext()).isNetworkConnected()) {
                                            if (!Utils.getPermission(requireContext())) {
                                                popupDialog()
                                            } else {
                                                reference.child(songFirebaseDb.uid).downloadUrl.addOnSuccessListener { uri ->
                                                    val extension =
                                                        songFirebaseDb.displayName.substring(
                                                            songFirebaseDb.displayName.lastIndexOf(".")
                                                        )
                                                    val fileName =
                                                        songFirebaseDb.displayName.substring(
                                                            0,
                                                            songFirebaseDb.displayName.lastIndexOf(".") - 1
                                                        )
                                                    downloadFile(
                                                        requireContext(),
                                                        fileName,
                                                        extension,
                                                        Environment.DIRECTORY_MUSIC,
                                                        uri.toString()
                                                    )
                                                    Toast.makeText(
                                                        requireContext(),
                                                        getString(R.string.downloading_process_is_started),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.internet_is_not_connected),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        return@setOnMenuItemClickListener true
                                    }
                                    R.id.report1 -> {
                                        if (NetworkHelper(requireContext()).isNetworkConnected())
                                            showBottomDialog(songFirebaseDb.uid)
                                        else
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.internet_is_not_connected),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        return@setOnMenuItemClickListener true
                                    }
                                    else -> return@setOnMenuItemClickListener false
                                }
                            }
                            popupMenu.show()
                        }
                    })
                appDatabase.songFirebaseDbDao().getAllSongFirebaseDb()
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        savedAdapter.submitList(it)
                    }, {
                        Log.d("TAG", "onCreateView: ${it.message}")
                    })
                binding.rv.adapter = savedAdapter
            }
        }
        return binding.root
    }

    private fun popupDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_title))
            .setMessage(getString(R.string.permission_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                Utils.setPermission(true, requireContext())
                uploadClick()
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, _: Int ->
                Utils.setPermission(false, requireContext())
            }
        val create = builder.create()
        create.setCanceledOnTouchOutside(false)
        create.show()
    }

    private fun setupAdapter() {

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
                                if (!Utils.getPermission(requireContext())) {
                                    val builder = AlertDialog.Builder(requireContext())
                                        .setTitle(getString(R.string.permission_title))
                                        .setMessage(getString(R.string.permission_message))
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                                            Utils.setPermission(true, requireContext())
                                            uploadClick()
                                        }
                                        .setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, _: Int ->
                                            Utils.setPermission(false, requireContext())
                                        }
                                    val create = builder.create()
                                    create.setCanceledOnTouchOutside(false)
                                    create.show()
                                } else {
                                    playFirebase(
                                        songFirebase.title!!,
                                        songFirebase.downloadUrl!!,
                                        songFirebase.duration!!
                                    )
                                    val intent =
                                        Intent(requireActivity(), SongService::class.java)
                                    intent.action = SongService.ACTION_PAUSE
                                    requireActivity().startService(intent)
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
                            optionsClick(songFirebase, view)
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

    fun optionsClick(songFirebase: SongFirebase, view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val songFirebaseDbByUid = appDatabase.songFirebaseDbDao()
            .getSongFirebaseDbByUid(songFirebase.uid!!)
        if (songFirebaseDbByUid == null) {
            popupMenu.menuInflater.inflate(
                R.menu.popup_options_explore,
                popupMenu.menu
            )
        } else {
            popupMenu.menuInflater.inflate(
                R.menu.popup_options_explore1,
                popupMenu.menu
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> {
                    val songFirebaseDb = SongFirebaseDb(
                        songFirebase.uid!!,
                        songFirebase.displayName!!,
                        songFirebase.title!!,
                        songFirebase.artist!!,
                        songFirebase.duration!!,
                        songFirebase.size!!,
                        songFirebase.downloadUrl!!,
                        songFirebase.isPlaying!!
                    )
                    appDatabase.songFirebaseDbDao()
                        .addSongFirebaseDb(songFirebaseDb)
                    return@setOnMenuItemClickListener true
                }
                R.id.save1 -> {
                    appDatabase.songFirebaseDbDao()
                        .deleteSongFirebaseDb(songFirebaseDbByUid!!)
                    return@setOnMenuItemClickListener true
                }
                R.id.download -> {
                    if (NetworkHelper(requireContext()).isNetworkConnected()) {
                        if (!Utils.getPermission(requireContext())) {
                            popupDialog()
                        } else {
                            reference.child(songFirebase.uid!!).downloadUrl.addOnSuccessListener { uri ->
                                val extension =
                                    songFirebase.displayName!!.substring(
                                        songFirebase.displayName!!.lastIndexOf(".")
                                    )
                                val fileName =
                                    songFirebase.displayName!!.substring(
                                        0,
                                        songFirebase.displayName!!.lastIndexOf(".") - 1
                                    )
                                downloadFile(
                                    requireContext(),
                                    fileName,
                                    extension,
                                    Environment.DIRECTORY_MUSIC,
                                    uri.toString()
                                )
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.downloading_process_is_started),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_is_not_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.download1 -> {
                    if (NetworkHelper(requireContext()).isNetworkConnected()) {
                        if (!Utils.getPermission(requireContext())) {
                            popupDialog()
                        } else {
                            reference.child(songFirebase.uid!!).downloadUrl.addOnSuccessListener { uri ->
                                val extension =
                                    songFirebase.displayName!!.substring(
                                        songFirebase.displayName!!.lastIndexOf(".")
                                    )
                                val fileName =
                                    songFirebase.displayName!!.substring(
                                        0,
                                        songFirebase.displayName!!.lastIndexOf(".") - 1
                                    )
                                downloadFile(
                                    requireContext(),
                                    fileName,
                                    extension,
                                    Environment.DIRECTORY_MUSIC,
                                    uri.toString()
                                )
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.downloading_process_is_started),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_is_not_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.report -> {
                    if (NetworkHelper(requireContext()).isNetworkConnected())
                        showBottomDialog(songFirebase.uid!!)
                    else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_is_not_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.report1 -> {
                    if (NetworkHelper(requireContext()).isNetworkConnected())
                        showBottomDialog(songFirebase.uid!!)
                    else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_is_not_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    fun showBottomDialog(firebaseUid: String) {
        val bottomSheetBinding =
            BottomSheetBinding.inflate(layoutInflater)
        val bottomSheetDialog =
            BottomSheetDialog(
                requireContext(),
                R.style.SheetDialog
            )
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        val report = Report()
        report.uid = firebaseUid
        report.isChecked = false
        val uid = UUID.randomUUID().toString()

        bottomSheetBinding.spam.setOnClickListener {
            report.type = "SPAM"
            report.description = "null"
            firebaseFirestore.collection("reports")
                .document(uid)
                .set(report)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.report_has_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.violence.setOnClickListener {
            report.type = "VIOLENCE"
            report.description = "null"
            firebaseFirestore.collection("reports")
                .document(uid)
                .set(report)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.report_has_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.pornography.setOnClickListener {
            report.type = "PORNOGRAPHY"
            report.description = "null"
            firebaseFirestore.collection("reports")
                .document(uid)
                .set(report)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.report_has_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.other.setOnClickListener {
            val otherBinding = OtherReportBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(otherBinding.root)
            otherBinding.send.setOnClickListener {
                val description = otherBinding.description.text.toString()
                report.type = "OTHER"
                report.description = description
                firebaseFirestore.collection("reports")
                    .document(uid)
                    .set(report)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.report_has_sent),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val bundle = Bundle()
                bundle.putString("state", "Lib")
                findNavController().navigate(R.id.searchFragment, bundle)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun uploadClick() {
        val intent = Intent(requireContext(), UploadService::class.java)
        requireActivity().startService(intent)
    }

    fun downloadFile(
        context: Context,
        fileName: String,
        fileExtension: String,
        destinationDirectory: String,
        url: String
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility((DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
        request.setDestinationInExternalPublicDir(
            destinationDirectory,
            "Open Player" + File.separator + fileName + fileExtension
        )
        downloadManager.enqueue(request)
    }

    private fun getByPath(pathFile: String, context: Context): Song {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf(
            pathFile
        )

        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val cursor = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )!!
        val mediaStoreIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val displayNameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val titleColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val pathColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val durationColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

        cursor.moveToFirst()

        val mediaStoreId = cursor.getLong(mediaStoreIdColumn)
        val displayName = cursor.getString(displayNameColumn)
        val title = cursor.getString(titleColumn)
        val artist = cursor.getString(artistColumn)
        val path = cursor.getString(pathColumn)
        val duration = cursor.getInt(durationColumn) / 1000
        val album = cursor.getString(albumColumn)
        val albumId = cursor.getLong(albumIdColumn)
        val size = cursor.getInt(sizeColumn)
        val coverArt = ContentUris.withAppendedId(artworkUri, albumId).toString()

        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mediaStoreId
        )

        return Song(
            mediaStoreId,
            contentUri.toString(),
            displayName,
            title,
            artist,
            path,
            duration,
            album,
            albumId,
            size,
            coverArt,
            0
        )
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: Int) =
            NetPagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

}