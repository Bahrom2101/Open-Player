package uz.mobilestudio.openplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import uz.mobilestudio.openplayer.R
import uz.mobilestudio.openplayer.db.AppDatabase
import uz.mobilestudio.openplayer.entity.Song
import uz.mobilestudio.openplayer.entity.SongUploaded
import uz.mobilestudio.openplayer.models.SongFirebase
import uz.mobilestudio.openplayer.utils.Utils
import java.io.File
import java.util.*

class UploadService : Service() {

    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var appDatabase: AppDatabase
    private val TAG = "UploadService"

    companion object {
        var isUploading = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getInstance(this)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("audios")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isUploading = true
        val bundle = intent?.extras?.getBundle("bundle")
        val song = bundle?.getSerializable("song") as Song
        val day = bundle.getInt("day", -1)
        try {
            val uid = UUID.randomUUID().toString()
            val audioFile = File(song.path)
            MediaScannerConnection.scanFile(this,
                arrayOf(audioFile.absolutePath),
                null,
                MediaScannerConnection.OnScanCompletedListener { _: String?, uri: Uri ->
                    val uploadTask =
                        reference.child(uid).putFile(uri)
                    uploadTask.addOnSuccessListener {
                        if (it.task.isSuccessful) {
                            val downloadUrl =
                                it.metadata?.reference?.downloadUrl
                            downloadUrl?.addOnSuccessListener { audioUri ->
                                val audioUrl = audioUri.toString()
                                val calendar = Calendar.getInstance()
                                val time = calendar.timeInMillis
                                val songFirebase = SongFirebase(
                                    uid,
                                    song.displayName,
                                    song.title,
                                    song.artist,
                                    song.duration,
                                    song.size,
                                    audioUrl,
                                    isPlaying = false,
                                    isCheckedByAdmin = false,
                                    isShow = true,
                                    time
                                )
                                firebaseFirestore.collection("audios")
                                    .document(uid)
                                    .set(songFirebase)
                                    .addOnSuccessListener {
                                        val songUploaded = SongUploaded(song.mediaStoreId)
                                        appDatabase.songUploadedDao().addSong(songUploaded)
                                        Utils.uploadSong(
                                            day,
                                            song.mediaStoreId,
                                            this
                                        )
                                        isUploading = false
                                        Toast.makeText(
                                            this,
                                            getString(R.string.uploaded),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnFailureListener {
                                        isUploading = false
                                        Toast.makeText(
                                            this,
                                            getString(R.string.error_occurred),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }?.addOnFailureListener {
                                isUploading = false
                            }
                        }
                    }.addOnFailureListener {
                        isUploading = false
                    }
                })
        } catch (e: Exception) {
            isUploading = false
        }
        return super.onStartCommand(intent, flags, startId)
    }
}