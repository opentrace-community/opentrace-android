package io.bluetrace.opentrace

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_plot.*
import io.bluetrace.opentrace.fragment.ExportData
import io.bluetrace.opentrace.status.persistence.StatusRecord
import io.bluetrace.opentrace.status.persistence.StatusRecordStorage
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecord
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecordStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class PlotActivity : AppCompatActivity() {
    private var TAG = "PlotActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_plot)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        val displayTimePeriod = intent.getIntExtra("time_period", 1) // in hours

        var observableStreetRecords = Observable.create<List<StreetPassRecord>> {
            val result = StreetPassRecordStorage(this).getAllRecords()
            it.onNext(result)
        }
        var observableStatusRecords = Observable.create<List<StatusRecord>> {
            val result = StatusRecordStorage(this).getAllRecords()
            it.onNext(result)
        }

        val zipResult = Observable.zip(observableStreetRecords, observableStatusRecords,
            BiFunction<List<StreetPassRecord>, List<StatusRecord>, ExportData> { records, status -> ExportData(records, status) }
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { exportedData ->

                if (exportedData.recordList.isEmpty()) {
                    return@subscribe
                }

                val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                // Use the date of the last record as the end time (Epoch time in seconds)
                val endTime =
                    exportedData.recordList.sortedByDescending { it.timestamp }[0].timestamp / 1000 + 1 * 60
                val endTimeString = dateFormatter.format(Date(endTime * 1000))

                val startTime =
                    endTime - displayTimePeriod * 3600 // ignore records older than X hour(s)
                val startTimeString = dateFormatter.format(Date(startTime * 1000))

                val filteredRecords = exportedData.recordList.filter {
                    it.timestamp / 1000 >= startTime && it.timestamp / 1000 <= endTime
                }

                if (filteredRecords.isNotEmpty()) {
                    val dataByModelC = filteredRecords.groupBy { it.modelC }
                    val dataByModelP = filteredRecords.groupBy { it.modelP }

                    // get all models
                    val allModelList = dataByModelC.keys union dataByModelP.keys.toList()
                    Log.d(TAG, "allModels: ${allModelList}")

                    // sort the list by the models that appear the most frequently
                    val sortedModelList =
                        allModelList.sortedWith(Comparator { a: String, b: String ->
                            val aSize = (dataByModelC[a]?.size ?: 0) + (dataByModelP[a]?.size ?: 0)
                            val bSize = (dataByModelC[b]?.size ?: 0) + (dataByModelP[b]?.size ?: 0)

                            bSize - aSize
                        })

                    // for each model form the data for that model
                    // e.g.:
                    //    var data1 = [];
                    //    var data1a = {
                    //        name: 'central',
                    //        x: ["2020-02-20 13:49"],
                    //        y: [-97],
                    //        xaxis: 'x1',
                    //        yaxis: 'y1',
                    //        mode: 'markers',
                    //        type: 'scatter',
                    //        line: {color: 'blue'}
                    //    };
                    //    data1 = data1.concat(data1a);
                    //    var data1b = {
                    //        name: 'peripheral',
                    //        x: ["2020-02-20 13:49", "2020-02-20 13:50", "2020-02-20 13:51", "2020-02-20 13:51", "2020-02-20 13:52", "2020-02-20 13:53", "2020-02-20 13:53", "2020-02-20 13:53"],
                    //        y: [-91, -94, -91, -98, -93, -101, -101, -97],
                    //        xaxis: 'x1',
                    //        yaxis: 'y1',
                    //        mode: 'markers',
                    //        type: 'scatter',
                    //        line: {color: 'red'}
                    //    };
                    //    data1 = data1.concat(data1b);
                    //
                    val individualData = sortedModelList.map { model ->
                        val index = sortedModelList.indexOf(model) + 1

                        val hasC = dataByModelC.containsKey(model)
                        val hasP = dataByModelP.containsKey(model)

                        val x1 = dataByModelC[model]?.map {
                            dateFormatter.format(Date(it.timestamp))
                        }?.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

                        val y1 = dataByModelC[model]?.map { it.rssi }
                            ?.joinToString(separator = ", ", prefix = "[", postfix = "]")

                        val x2 = dataByModelP[model]?.map {
                            dateFormatter.format(Date(it.timestamp))
                        }?.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

                        val y2 = dataByModelP[model]?.map { it.rssi }
                            ?.joinToString(separator = ", ", prefix = "[", postfix = "]")

                        val dataHead = "var data${index} = [];"

                        val dataA = if (!hasC) "" else """
                            var data${index}a = {
                              name: 'central',
                              x: ${x1},
                              y: ${y1},
                              xaxis: 'x${index}',
                              yaxis: 'y${index}',
                              mode: 'markers',
                              type: 'scatter',
                              line: {color: 'blue'}
                            };
                            data${index} = data${index}.concat(data${index}a);
                        """.trimIndent()

                        val dataB = if (!hasP) "" else """
                            var data${index}b = {
                              name: 'peripheral',
                              x: ${x2},
                              y: ${y2},
                              xaxis: 'x${index}',
                              yaxis: 'y${index}',
                              mode: 'markers',
                              type: 'scatter',
                              line: {color: 'red'}
                            };
                            data${index} = data${index}.concat(data${index}b);
                        """.trimIndent()

                        val data = dataHead + dataA + dataB

                        data

                    }.joinToString(separator = "\n")

                    val top = 20

                    // Combine data of all the models
                    // e.g.
                    //    var data = [];
                    //    data = data.concat(data1);
                    //    data = data.concat(data2);
                    //    data = data.concat(data3);
                    //    data = data.concat(data4);
                    //    data = data.concat(data5);
                    //    data = data.concat(data6);
                    //    data = data.concat(data7);
                    //
                    val combinedData = sortedModelList.map { model ->
                        val index = sortedModelList.indexOf(model) + 1
                        if (index < top) """
                            data = data.concat(data${index});
                        """.trimIndent() else ""
                    }.joinToString(separator = "\n")

                    // Get definition for all xAxes
                    // e.g.
                    //    xaxis1: {
                    //        type: 'date',
                    //        tickformat: '%H:%M',
                    //        range: ['2020-02-20 13:00', '2020-02-20 14:00'],
                    //        dtick: 5 * 60 * 1000
                    //    },
                    //    xaxis2: {
                    //        type: 'date',
                    //        tickformat: '%H:%M',
                    //        range: ['2020-02-20 13:00', '2020-02-20 14:00'],
                    //        dtick: 5 * 60 * 1000
                    //    }
                    //
                    val xAxis = sortedModelList.map { model ->
                        val index = sortedModelList.indexOf(model) + 1
                        if (index < top) """
                                  xaxis${index}: {
                                    type: 'date',
                                    tickformat: '%H:%M:%S',
                                    range: ['${startTimeString}', '${endTimeString}'],
                                    dtick: ${displayTimePeriod * 5} * 60 * 1000
                                  }
                        """.trimIndent() else ""
                    }.joinToString(separator = ",\n")

                    // Get definition for all xAxes
                    // e.g.
                    //    yaxis1: {
                    //        range: [-100, -30],
                    //        ticks: 'outside',
                    //        dtick: 10,
                    //        title: {
                    //            text: "SM-N960F"
                    //        }
                    //    },
                    //    yaxis2: {
                    //        range: [-100, -30],
                    //        ticks: 'outside',
                    //        dtick: 10,
                    //        title: {
                    //            text: "POCOPHONE F1"
                    //        }
                    //    }
                    //
                    val yAxis = sortedModelList.map { model ->
                        val index = sortedModelList.indexOf(model) + 1
                        if (index < top) """
                                  yaxis${index}: {
                                    range: [-100, -30],
                                    ticks: 'outside',
                                    dtick: 10,
                                    title: {
                                      text: "${model}"
                                    }
                                  }
                        """.trimIndent() else ""
                    }.joinToString(separator = ",\n")

                    // Form the complete HTML
                    val customHtml = """
                        <head>
                            <script src='https://cdn.plot.ly/plotly-latest.min.js'></script>
                        </head>
                        <body>
                            <div id='myDiv'></div>
                            <script>
                                ${individualData}
                                
                                var data = [];
                                ${combinedData}
                                
                                var layout = {
                                  title: 'Activities from <b>${startTimeString.substring(11..15)}</b> to <b>${endTimeString.substring(
                        11..15
                    )}</b>   <span style="color:blue">central</span> <span style="color:red">peripheral</span>',
                                  height: 135 * ${allModelList.size},
                                  showlegend: false,
                                  grid: {rows: ${allModelList.size}, columns: 1, pattern: 'independent'},
                                  margin: {
                                    t: 30,
                                    r: 30,
                                    b: 20,
                                    l: 50,
                                    pad: 0
                                  },
                                  ${xAxis},
                                  ${yAxis}
                                };
                                
                                var config = {
                                    responsive: true, 
                                    displayModeBar: false, 
                                    displaylogo: false, 
                                    modeBarButtonsToRemove: ['toImage', 'sendDataToCloud', 'editInChartStudio', 'zoom2d', 'select2d', 'pan2d', 'lasso2d', 'autoScale2d', 'resetScale2d', 'zoomIn2d', 'zoomOut2d', 'hoverClosestCartesian', 'hoverCompareCartesian', 'toggleHover', 'toggleSpikelines']
                                }
                                
                                Plotly.newPlot('myDiv', data, layout, config);
                            </script>
                        </body>
                    """.trimIndent()

                    Log.d(TAG, "customHtml: ${customHtml}")
                    webView.loadData(customHtml, "text/html", "UTF-8")
                } else {
                    webView.loadData(
                        "No data received in the last ${displayTimePeriod} hour(s) or more.",
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        Log.d(TAG, "zipResult: ${zipResult}")

        webView.loadData("Loading...", "text/html", "UTF-8")
    }
}
