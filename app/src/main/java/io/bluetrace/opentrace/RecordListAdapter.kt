package io.bluetrace.opentrace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_view_item.view.*
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecord
import io.bluetrace.opentrace.streetpass.view.StreetPassRecordViewModel


class RecordListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<RecordListAdapter.RecordViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var records = emptyList<StreetPassRecordViewModel>() // Cached copy of records
    private var sourceData = emptyList<StreetPassRecord>()

    enum class MODE {
        ALL, COLLAPSE, MODEL_P, MODEL_C
    }

    private var mode = MODE.ALL

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelCView: TextView = itemView.modelc
        val modelPView: TextView = itemView.modelp
        val timestampView: TextView = itemView.timestamp
        val findsView: TextView = itemView.finds
        val txpowerView: TextView = itemView.txpower
        val signalStrengthView: TextView = itemView.signal_strength
        val filterModelP: View = itemView.filter_by_modelp
        val filterModelC: View = itemView.filter_by_modelc
        val msgView: TextView = itemView.msg
        val version: TextView = itemView.version
        val org: TextView = itemView.org
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = inflater.inflate(R.layout.recycler_view_item, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val current = records[position]
        holder.msgView.text = current.msg
        holder.modelCView.text = current.modelC
        holder.modelPView.text = current.modelP
        holder.findsView.text = "Detections: ${current.number}"
        val readableDate = Utils.getDate(current.timeStamp)
        holder.timestampView.text = readableDate
        holder.version.text = "v: ${current.version}"
        holder.org.text = "ORG: ${current.org}"

        holder.filterModelP.tag = current
        holder.filterModelC.tag = current

        holder.signalStrengthView.text = "Signal Strength: ${current.rssi}"

        holder.txpowerView.text = "Tx Power: ${current.transmissionPower}"

        holder.filterModelP.setOnClickListener {
            val model = it.tag as StreetPassRecordViewModel
            setMode(MODE.MODEL_P, model)
        }

        holder.filterModelC.setOnClickListener {
            val model = it.tag as StreetPassRecordViewModel
            setMode(MODE.MODEL_C, model)
        }
    }

    private fun filter(sample: StreetPassRecordViewModel?): List<StreetPassRecordViewModel> {
        return when (mode) {
            MODE.COLLAPSE -> prepareCollapsedData(sourceData)
            MODE.ALL -> prepareViewData(sourceData)
            MODE.MODEL_P -> filterByModelP(sample, sourceData)
            MODE.MODEL_C -> filterByModelC(sample, sourceData)
        }
    }

    private fun filterByModelC(
        model: StreetPassRecordViewModel?,
        words: List<StreetPassRecord>
    ): List<StreetPassRecordViewModel> {
        if (model != null) {
            return prepareViewData(words.filter { it.modelC == model.modelC })
        }
        return prepareViewData(words)
    }

    private fun filterByModelP(
        model: StreetPassRecordViewModel?,
        words: List<StreetPassRecord>
    ): List<StreetPassRecordViewModel> {

        if (model != null) {
            return prepareViewData(words.filter { it.modelP == model.modelP })
        }
        return prepareViewData(words)
    }


    private fun prepareCollapsedData(words: List<StreetPassRecord>): List<StreetPassRecordViewModel> {
        //we'll need to count the number of unique device IDs
        val countMap = words.groupBy {
            it.modelC
        }

        val distinctAddresses = words.distinctBy { it.modelC }

        return distinctAddresses.map { record ->
            val count = countMap[record.modelC]?.size

            count?.let { count ->
                val mostRecentRecord = countMap[record.modelC]?.maxBy { it.timestamp }

                if (mostRecentRecord != null) {
                    return@map StreetPassRecordViewModel(mostRecentRecord, count)
                }

                return@map StreetPassRecordViewModel(record, count)
            }
            //fallback - unintended
            return@map StreetPassRecordViewModel(record)
        }
    }

    private fun prepareViewData(words: List<StreetPassRecord>): List<StreetPassRecordViewModel> {
        return words.reversed().map {
            return@map StreetPassRecordViewModel(it)
        }
    }

    fun setMode(mode: MODE) {
        setMode(mode, null)
    }

    private fun setMode(mode: MODE, model: StreetPassRecordViewModel?) {
        this.mode = mode

        val list = filter(model)
        setRecords(list)
    }

    private fun setRecords(records: List<StreetPassRecordViewModel>) {
        this.records = records
        notifyDataSetChanged()
    }

    internal fun setSourceData(records: List<StreetPassRecord>) {
        this.sourceData = records
        setMode(mode)
    }

    override fun getItemCount() = records.size
}
