package uz.jabborovbahrom.openplayer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jabborovbahrom.openplayer.R
import uz.jabborovbahrom.openplayer.db.AppDatabase
import uz.jabborovbahrom.openplayer.entity.Song
import uz.jabborovbahrom.openplayer.entity.SongUploaded
import uz.jabborovbahrom.openplayer.models.SongFirebase
import uz.jabborovbahrom.openplayer.utils.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UploadService(var context: Context,workerParameters: WorkerParameters) : Worker(context,workerParameters) {

    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var appDatabase: AppDatabase
    private val TAG = "UploadService"

    override fun doWork(): Result {
        appDatabase = AppDatabase.getInstance(context)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("audios")
        val randomSong = getRandomSong()
        try {
            val uid = UUID.randomUUID().toString()
            val audioFile = File(randomSong.path)
            MediaScannerConnection.scanFile(context,
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
                                    randomSong.displayName,
                                    randomSong.title,
                                    randomSong.artist,
                                    randomSong.duration,
                                    randomSong.size,
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
                                        val songUploaded = SongUploaded(randomSong.mediaStoreId)
                                        appDatabase.songUploadedDao().addSong(songUploaded)
                                        val forDay = SimpleDateFormat("dd")
                                        val day = forDay.format(calendar.time).toInt()
                                        Utils.uploadSong(
                                            day,
                                            randomSong.mediaStoreId,
                                            context
                                        )
                                        if (!Utils.getWork(context)) {
                                            Utils.setWork(true,context)
                                        }
                                        Log.d("TAGg", "doWork: Uploaded")
                                    }.addOnFailureListener {
                                    }
                            }?.addOnFailureListener {
                            }
                        }
                    }.addOnFailureListener {
                    }
                })
        } catch (e: Exception) {
        }
        return Result.success()
    }

    private fun getRandomSong() : Song {
        val song = appDatabase.songDao().getRandomSong()[0]
        return if (song.size <= 20000000) {
            song
        } else {
            getRandomSong()
        }
    }

}