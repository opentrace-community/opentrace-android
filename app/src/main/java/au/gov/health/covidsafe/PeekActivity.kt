package au.gov.health.covidsafe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecordStorage
import au.gov.health.covidsafe.streetpass.view.RecordViewModel


class PeekActivity : AppCompatActivity() {

    private lateinit var viewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newPeek()
    }

    private fun newPeek() {
        setContentView(R.layout.database_peek)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = RecordListAdapter(this)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        viewModel.allRecords.observe(this, Observer { records ->
            adapter.setSourceData(records)
        })

        findViewById<FloatingActionButton>(R.id.expand)
            .setOnClickListener {
                viewModel.allRecords.value?.let {
                    adapter.setMode(RecordListAdapter.MODE.ALL)
                }
            }

        findViewById<FloatingActionButton>(R.id.collapse)
            .setOnClickListener {
                viewModel.allRecords.value?.let {
                    adapter.setMode(RecordListAdapter.MODE.COLLAPSE)
                }
            }


        val start = findViewById<FloatingActionButton>(R.id.start)
        start.setOnClickListener {
            startService()
        }

        val stop = findViewById<FloatingActionButton>(R.id.stop)
        stop.setOnClickListener {
            stopService()
        }

        val delete = findViewById<FloatingActionButton>(R.id.delete)
        delete.setOnClickListener { view ->
            view.isEnabled = false

            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("Are you sure?")
                .setCancelable(false)
                .setMessage("Deleting the DB records is irreversible")
                .setPositiveButton("DELETE") { dialog, which ->
                    Observable.create<Boolean> {
                        StreetPassRecordStorage(this).nukeDb()
                        it.onNext(true)
                    }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe { result ->
                            Toast.makeText(this, "Database nuked: $result", Toast.LENGTH_SHORT)
                                .show()
                            view.isEnabled = true
                            dialog.cancel()
                        }
                }

                .setNegativeButton("DON'T DELETE") { dialog, which ->
                    view.isEnabled = true
                    dialog.cancel()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

        val plot = findViewById<FloatingActionButton>(R.id.plot)
        plot.setOnClickListener { view ->
            val intent = Intent(this, PlotActivity::class.java)
            intent.putExtra("time_period", nextTimePeriod())
            startActivity(intent)
        }

        if(!BuildConfig.DEBUG) {
            start.visibility = View.GONE
            stop.visibility = View.GONE
            delete.visibility = View.GONE
        }

    }

    private var timePeriod: Int = 0

    private fun nextTimePeriod(): Int {
        timePeriod = when (timePeriod) {
            1 -> 3
            3 -> 6
            6 -> 12
            12 -> 24
            else -> 1
        }

        return timePeriod
    }


    private fun startService() {
        Utils.startBluetoothMonitoringService(this)
    }

    private fun stopService() {
        Utils.stopBluetoothMonitoringService(this)
    }

}