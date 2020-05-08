package au.gov.health.covidsafe.streetpass

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import com.google.gson.Gson
import au.gov.health.covidsafe.logging.CentralLog
import kotlin.properties.Delegates

class Work constructor(
    var device: BluetoothDevice,
    var connectable: ConnectablePeripheral,
    private val onWorkTimeoutListener: OnWorkTimeoutListener
) : Comparable<Work> {
    var timeStamp: Long by Delegates.notNull()
    var checklist = WorkCheckList()
    var gatt: BluetoothGatt? = null
    var finished = false
    var timeout : Long = 0

    private val TAG = "Work"

    val timeoutRunnable: Runnable = Runnable {
        onWorkTimeoutListener.onWorkTimeout(this)
    }

    init {
        timeStamp = System.currentTimeMillis()
    }

    fun isCriticalsCompleted(): Boolean {
        return (checklist.connected.status && checklist.readCharacteristic.status && checklist.writeCharacteristic.status) || checklist.skipped.status
    }

    fun startWork(
        context: Context,
        gattCallback: StreetPassWorker.StreetPassGattCallback
    ) {
        gatt = device.connectGatt(context, false, gattCallback)
        if (gatt == null) {
            CentralLog.e(TAG, "Unable to connect to ${device.address}")
        }
    }

    override fun compareTo(other: Work): Int {
        return -(timeStamp - other.timeStamp).toInt()
    }

    inner class WorkCheckList {
        var started = Check()
        var connected = Check()
        var mtuChanged = Check()
        var readCharacteristic = Check()
        var writeCharacteristic = Check()
        var disconnected = Check()
        var skipped = Check()

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    inner class Check {
        var status = false
        var timePerformed: Long = 0
    }

    interface OnWorkTimeoutListener {
        fun onWorkTimeout(work: Work)
    }
}
