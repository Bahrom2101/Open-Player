package uz.jabborovbahrom.openplayer.admin

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import jabborovbahrom.openplayer.databinding.ItemReportBinding
import uz.jabborovbahrom.openplayer.models.Report

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