package uz.mobilestudio.openplayer.models

import java.io.Serializable

class Report : Serializable{
    var uid: String? = null
    var type: String? = null
    var description: String? = null
    var isChecked: Boolean? = null
}