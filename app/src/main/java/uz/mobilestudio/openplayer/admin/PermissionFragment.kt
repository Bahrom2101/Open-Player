package uz.mobilestudio.openplayer.admin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import uz.mobilestudio.openplayer.R
import uz.mobilestudio.openplayer.databinding.FragmentPermissonBinding
import uz.mobilestudio.openplayer.models.Permission
import uz.mobilestudio.openplayer.models.SongFirebase
import uz.mobilestudio.openplayer.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class PermissionFragment : Fragment() {

    lateinit var binding: FragmentPermissonBinding
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var permission: Permission

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPermissonBinding.inflate(layoutInflater)

        firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("permission")
            .document("permission")
            .get()
            .addOnSuccessListener {
                permission = it.toObject(Permission::class.java)!!
                binding.switchButton.isChecked = permission.canUseContents!!
            }

        binding.switchButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                firebaseFirestore.collection("permission")
                    .document("permission")
                    .update("canUseContents", true)
            } else {
                firebaseFirestore.collection("permission")
                    .document("permission")
                    .update("canUseContents", false)
            }
        }

        return binding.root
    }

}