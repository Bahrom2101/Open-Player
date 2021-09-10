package uz.mobilestudio.openplayer.admin

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uz.mobilestudio.openplayer.databinding.ItemExploreBinding
import uz.mobilestudio.openplayer.databinding.ItemReportBinding
import uz.mobilestudio.openplayer.models.Report
import uz.mobilestudio.openplayer.models.SongFirebase

class ReportFirebaseViewHolder(
    var itemReportBinding: ItemReportBinding,
    var onClickListener: OnClickListener
) :
    RecyclerView.ViewHolder(itemReportBinding.root) {

    @SuppressLint("SetTextI18n")
    fun bind(report: Report, position: Int) {
        itemReportBinding.type.text = report.type

        itemReportBinding.options.setOnClickListener {
            onClickListener.onOptionsClick(report, position, itemReportBinding.options)
        }

        itemReportBinding.root.setOnClickListener {
            onClickListener.onPlayPauseClick(report, position, itemReportBinding.root)
        }
    }

    interface OnClickListener {
        fun onPlayPauseClick(report: Report, position: Int, playPauseButton: View)

        fun onOptionsClick(report: Report, position: Int, view: View)
    }

}