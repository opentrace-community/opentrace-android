package au.gov.health.covidsafe.streetpass

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import androidx.annotation.Keep
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.TracerApp
import au.gov.health.covidsafe.Utils
import au.gov.health.covidsafe.bluetooth.gatt.*
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.services.BluetoothMonitoringService
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.blacklistDuration
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.maxQueueTime
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
@Keep
class StreetPassWorker(val context: Context) {

    private val workQueue: PriorityBlockingQueue<Work> = PriorityBlockingQueue()
    private val blacklist: MutableList<BlacklistEntry> = Collections.synchronizedList(ArrayList())

    private val workReceiver = StreetPassWorkReceiver()
    private val deviceProcessedReceiver = DeviceProcessedReceiver()
    private val serviceUUID: UUID = UUID.fromString(BuildConfig.BLE_SSID)
    private val TAG = "StreetPassWorker"

    private val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private lateinit var timeoutHandler: Handler
    private lateinit var queueHandler: Handler
    private lateinit var blacklistHandler: Handler

    private var currentPendingConnection: Work? = null
    private var localBroadcastManager: LocalBroadcastManager = LocalBroadcastManager.getInstance(context)

    val onWorkTimeoutListener = object : Work.OnWorkTimeoutListener {
        override fun onWorkTimeout(work: Work) {

            if (!isCurrentlyWorkedOn(work.device.address)) {
                CentralLog.i(TAG, "Work already removed. Timeout ineffective??.")
            }

            CentralLog.e(
                    TAG,
                    "Work timed out for ${work.device.address} @ ${work.connectable.rssi} queued for ${work.checklist.started.timePerformed - work.timeStamp}ms"
            )
            CentralLog.e(
                    TAG,
                    "${work.device.address} work status: ${work.checklist}."
            )

            //connection never formed - don't need to disconnect
            if (!work.checklist.connected.status) {
                CentralLog.e(TAG, "No connection formed for ${work.device.address}")
                if (work.device.address == currentPendingConnection?.device?.address) {
                    currentPendingConnection = null
                }

                try {
                    work.gatt?.close()
                } catch (e: Exception) {
                    CentralLog.e(
                            TAG,
                            "Unexpected error while attempting to close clientIf to ${work.device.address}: ${e.localizedMessage}"
                    )
                }

                finishWork(work)
            }
            //the connection is still there - might be stuck / work in progress
            else if (work.checklist.connected.status && !work.checklist.disconnected.status) {

                if (work.checklist.readCharacteristic.status || work.checklist.writeCharacteristic.status || work.checklist.skipped.status) {
                    CentralLog.e(
                            TAG,
                            "Connected but did not disconnect in time for ${work.device.address}"
                    )

                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            currentPendingConnection = null
                            finishWork(work)
                        }
                    } catch (e: Throwable) {
                        CentralLog.e(
                                TAG,
                                "Failed to clean up work, bluetooth state likely changed or other device's advertiser stopped: ${e.localizedMessage}"
                        )
                    }

                } else {
                    CentralLog.e(
                            TAG,
                            "Connected but did nothing for ${work.device.address}"
                    )

                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            currentPendingConnection = null
                            finishWork(work)
                        }
                    } catch (e: Throwable) {
                        CentralLog.e(
                                TAG,
                                "Failed to clean up work, bluetooth state likely changed or other device's advertiser stopped: ${e.localizedMessage}"
                        )
                    }
                }
            }

            //all other edge cases? - disconnected
            else {
                CentralLog.e(
                        TAG,
                        "Disconnected but callback not invoked in time. Waiting.: ${work.device.address}: ${work.checklist}"
                )
            }
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        val deviceAvailableFilter = IntentFilter(ACTION_DEVICE_SCANNED)
        localBroadcastManager.registerReceiver(workReceiver, deviceAvailableFilter)

        val deviceProcessedFilter = IntentFilter(ACTION_DEVICE_PROCESSED)
        localBroadcastManager.registerReceiver(deviceProcessedReceiver, deviceProcessedFilter)

        timeoutHandler = Handler()
        queueHandler = Handler()
        blacklistHandler = Handler()
    }

    fun isCurrentlyWorkedOn(address: String?): Boolean {
        return currentPendingConnection?.let {
            it.device.address == address
        } ?: false
    }

    fun addWork(work: Work): Boolean {
        //if it's our current work. ignore
        if (isCurrentlyWorkedOn(work.device.address)) {
            CentralLog.i(TAG, "${work.device.address} is being worked on, not adding to queue")
            return false
        }

        //if its in blacklist - check for both mac address and manu data

        if (blacklist.any { it.uniqueIdentifier == work.device.address }) {
            CentralLog.i(TAG, "${work.device.address} is in blacklist, not adding to queue")
            return false
        }

        //if we haven't seen this device yet
        if (workQueue.none { it.device.address == work.device.address }) {
            workQueue.offer(work)
            queueHandler.postDelayed({
                if (workQueue.contains(work))
                    CentralLog.i(
                            TAG,
                            "Work for ${work.device.address} removed from queue? : ${workQueue.remove(
                                    work
                            )}"
                    )
            }, maxQueueTime)
            CentralLog.i(TAG, "Added to work queue: ${work.device.address}")
            return true
        }
        //this gadget is already in the queue, we can use the latest rssi and txpower? replace the entry
        else {

            //ignore it
            CentralLog.i(TAG, "${work.device.address} is already in work queue")

            val prevWork = workQueue.find { it.device.address == work.device.address }
            val removed = workQueue.remove(prevWork)
            val added = workQueue.offer(work)

            CentralLog.i(TAG, "Queue entry updated - removed: ${removed}, added: $added")

            return false
        }
    }

    fun doWork() {

        if (currentPendingConnection != null) {
            CentralLog.i(
                    TAG,
                    "Already trying to connect to: ${currentPendingConnection?.device?.address}"
            )
            //devices may reset their bluetooth before the disconnection happens properly and disconnect is never called.
            //handle that situation here

            //if the job was finished but not removed
            //or if the job was timed out but not removed
            val timedout = System.currentTimeMillis() > currentPendingConnection?.timeout ?: 0
            if (currentPendingConnection?.finished ?: false || timedout) {

                CentralLog.w(
                        TAG,
                        "Handling erroneous current work for ${currentPendingConnection?.device?.address} : - finished: ${currentPendingConnection?.finished
                                ?: false}, timedout: $timedout"
                )
                //check if there is, for some reason, an existing connection
                if (currentPendingConnection != null) {
                    if (bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).contains(
                                    currentPendingConnection?.device
                            )
                    ) {
                        CentralLog.w(
                                TAG,
                                "Disconnecting dangling connection to ${currentPendingConnection?.device?.address}"
                        )
                        currentPendingConnection?.gatt?.disconnect()
                    }
                } else {
                    doWork()
                }
            }

            return
        }

        if (workQueue.isEmpty()) {
            CentralLog.i(TAG, "Queue empty. Nothing to do.")
            return
        }

        CentralLog.i(TAG, "Queue size: ${workQueue.size}")

        var workToDo: Work? = null
        val now = System.currentTimeMillis()

        while (workToDo == null && workQueue.isNotEmpty()) {
            workToDo = workQueue.poll()
            workToDo?.let { work ->
                if (now - work.timeStamp > maxQueueTime) {
                    CentralLog.w(
                            TAG,
                            "Work request for ${work.device.address} too old. Not doing"
                    )
                    workToDo = null
                }
            }
        }

        workToDo?.let {

            val device = it.device

            if (blacklist.filter { it.uniqueIdentifier == device.address }.isNotEmpty()) {
                CentralLog.w(TAG, "Already worked on ${device.address}. Skip.")
                doWork()
                return
            }

            var currentWorkOrder = it

            val alreadyConnected = getConnectionStatus(device)
            CentralLog.i(TAG, "Already connected to ${device.address} : $alreadyConnected")

            if (alreadyConnected) {
                //this might mean that the other device is currently connected to this device's local gatt server
                //skip. we'll rely on the other party to do a write
                currentWorkOrder.checklist.skipped.status = true
                currentWorkOrder.checklist.skipped.timePerformed = System.currentTimeMillis()
                currentWorkOrder.let {
                    finishWork(it)
                }

            } else {

                currentWorkOrder.let {

                    if (it != null) {

                        val gattCallback = StreetPassGattCallback(it)
                        CentralLog.i(
                                TAG,
                                "Starting work - connecting to device: ${device.address} @ ${it.connectable.rssi} ${System.currentTimeMillis() - it.timeStamp}ms ago"
                        )
                        currentPendingConnection = it

                        try {
                            it.checklist.started.status = true
                            it.checklist.started.timePerformed = System.currentTimeMillis()

                            it.startWork(context, gattCallback)

                            var connecting = it.gatt?.connect() ?: false

                            if (!connecting) {
                                CentralLog.e(
                                        TAG,
                                        "not connecting to ${it.device.address}??"
                                )

                                //bail and do the next job
                                CentralLog.e(TAG, "Moving on to next task")
                                currentPendingConnection = null
                                doWork()
                                return

                            } else {
                                CentralLog.i(
                                        TAG,
                                        "Connection to ${it.device.address} attempt in progress"
                                )
                            }

                            timeoutHandler.postDelayed(
                                    it.timeoutRunnable,
                                    BluetoothMonitoringService.connectionTimeout
                            )
                            it.timeout =
                                    System.currentTimeMillis() + BluetoothMonitoringService.connectionTimeout

                            CentralLog.i(TAG, "Timeout scheduled for ${it.device.address}")
                        } catch (e: Throwable) {
                            CentralLog.e(
                                    TAG,
                                    "Unexpected error while attempting to connect to ${device.address}: ${e.localizedMessage}"
                            )
                            CentralLog.e(TAG, "Moving on to next task")
                            currentPendingConnection = null
                            doWork()
                            return
                        }

                    } else {
                        CentralLog.e(TAG, "Work not started - missing Work Object")
                    }
                }
            }
        }

        if (workToDo == null) {
            CentralLog.i(TAG, "No outstanding work")
        }

    }

    private fun getConnectionStatus(device: BluetoothDevice): Boolean {

        val connectedDevices = bluetoothManager.getDevicesMatchingConnectionStates(
                BluetoothProfile.GATT,
                intArrayOf(BluetoothProfile.STATE_CONNECTED)
        )
        return connectedDevices.contains(device)
    }

    fun finishWork(work: Work) {

        if (work.finished) {
            CentralLog.i(
                    TAG,
                    "Work on ${work.device.address} already finished and closed"
            )
            return
        }

        if (work.isCriticalsCompleted()) {
            Utils.broadcastDeviceProcessed(context, work.device.address)
        }

        CentralLog.i(
                TAG,
                "Work on ${work.device.address} stopped in: ${work.checklist.disconnected.timePerformed - work.checklist.started.timePerformed}"
        )

        CentralLog.i(
                TAG,
                "Work on ${work.device.address} completed?: ${work.isCriticalsCompleted()}. Connected in: ${work.checklist.connected.timePerformed - work.checklist.started.timePerformed}. connection lasted for: ${work.checklist.disconnected.timePerformed - work.checklist.connected.timePerformed}. Status: ${work.checklist}"
        )

        timeoutHandler.removeCallbacks(work.timeoutRunnable)
        CentralLog.i(TAG, "Timeout removed for ${work.device.address}")

        work.finished = true
        doWork()
    }

    inner class StreetPassGattCallback(private val work: Work) : BluetoothGattCallback() {

        private fun endWorkConnection(gatt: BluetoothGatt) {
            CentralLog.i(TAG, "Ending connection with: ${gatt.device.address}")
            gatt.disconnect()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            gatt?.let {

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        CentralLog.i(TAG, "Connected to other GATT server - ${gatt.device.address}")

                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                        gatt.requestMtu(512)

                        work.checklist.connected.status = true
                        work.checklist.connected.timePerformed = System.currentTimeMillis()

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        CentralLog.i(
                                TAG,
                                "Disconnected from other GATT server - ${gatt.device.address}"
                        )
                        work.checklist.disconnected.status = true
                        work.checklist.disconnected.timePerformed = System.currentTimeMillis()

                        //remove timeout runnable if its still there
                        timeoutHandler.removeCallbacks(work.timeoutRunnable)
                        CentralLog.i(TAG, "Timeout removed for ${work.device.address}")

                        //remove job from list of current work - if it is the current work
                        if (work.device.address == currentPendingConnection?.device?.address) {
                            currentPendingConnection = null
                        }
                        gatt.close()
                        finishWork(work)
                    }

                    else -> {
                        CentralLog.i(TAG, "Connection status for ${gatt.device.address}: $newState")
                        endWorkConnection(gatt)
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {

            if (!work.checklist.mtuChanged.status) {

                work.checklist.mtuChanged.status = true
                work.checklist.mtuChanged.timePerformed = System.currentTimeMillis()

                CentralLog.i(
                        TAG,
                        "${gatt?.device?.address} MTU is $mtu. Was change successful? : ${status == BluetoothGatt.GATT_SUCCESS}"
                )

                gatt?.let {
                    val discoveryOn = gatt.discoverServices()
                    CentralLog.i(
                            TAG,
                            "Attempting to start service discovery on ${gatt.device.address}: $discoveryOn"
                    )
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    CentralLog.i(
                            TAG,
                            "onServicesDiscovered received: BluetoothGatt.GATT_SUCCESS - $status"
                    )
                    CentralLog.i(
                            TAG,
                            "Discovered ${gatt.services.size} services on ${gatt.device.address}"
                    )

                    val service = gatt.getService(serviceUUID)

                    service?.let {
                        val characteristic = service.getCharacteristic(serviceUUID)
                        if (characteristic != null) {
                            val readSuccess = gatt.readCharacteristic(characteristic)
                            CentralLog.i(
                                    TAG,
                                    "Attempt to read characteristic of our service on ${gatt.device.address}: $readSuccess"
                            )
                        } else {
                            CentralLog.e(
                                    TAG,
                                    "${gatt.device.address} does not have our characteristic"
                            )
                            endWorkConnection(gatt)
                        }
                    }

                    if (service == null) {
                        CentralLog.e(
                                TAG,
                                "${gatt.device.address} does not have our service"
                        )
                        endWorkConnection(gatt)
                    }
                }
                else -> {
                    CentralLog.w(TAG, "No services discovered on ${gatt.device.address}")
                    endWorkConnection(gatt)
                }
            }
        }

        // data read from a perhipheral
        //I am a central
        override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {

            CentralLog.i(TAG, "Read Status: $status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    CentralLog.i(
                            TAG,
                            "Characteristic read from ${gatt.device.address}: ${characteristic.getStringValue(
                                    0
                            )}"
                    )

                    when (characteristic.uuid) {

                        serviceUUID -> {
                            //need to populate in the rssi here?
                            CentralLog.i(
                                    TAG,
                                    "onCharacteristicRead: ${work.device.address} - [${work.connectable.rssi}]"
                            )

                            val dataBytes = characteristic.value

                            try {
                                val readData = ReadRequestPayload.createReadRequestPayload(dataBytes)
                                val peripheral =
                                        PeripheralDevice(readData.modelP, work.device.address)

                                val connectionRecord = ConnectionRecord(
                                        version = readData.v,
                                        msg = readData.msg,
                                        org = readData.org,
                                        peripheral = peripheral,
                                        central = TracerApp.asCentralDevice(),
                                        rssi = work.connectable.rssi,
                                        txPower = work.connectable.transmissionPower
                                )

                                Utils.broadcastStreetPassReceived(
                                        context,
                                        connectionRecord
                                )

                            } catch (e: Throwable) {
                                CentralLog.e(
                                        TAG,
                                        "Failed to de-serialize request payload object - ${e.message}"
                                )
                            }
                        }
                    }
                    work.checklist.readCharacteristic.status = true
                    work.checklist.readCharacteristic.timePerformed = System.currentTimeMillis()
                }

                else -> {
                    CentralLog.w(
                            TAG,
                            "Failed to read characteristics from ${gatt.device.address}: $status"
                    )
                }
            }

            // Only attempt to write BM back to peripheral if it is still valid
            if (Utils.bmValid(context)) {
                //may have failed to read, can try to write
                //we are writing as the central device
                val thisCentralDevice = TracerApp.asCentralDevice()

                val writedata = WriteRequestPayload(
                        v = TracerApp.protocolVersion,
                        msg = TracerApp.thisDeviceMsg(),
                        org = TracerApp.ORG,
                        modelC = thisCentralDevice.modelC,
                        rssi = work.connectable.rssi,
                        txPower = work.connectable.transmissionPower
                )

                characteristic.value = writedata.getPayload()
                val writeSuccess = gatt.writeCharacteristic(characteristic)
                CentralLog.i(
                        TAG,
                        "Attempt to write characteristic to our service on ${gatt.device.address}: $writeSuccess"
                )
            } else {
                CentralLog.i(
                        TAG,
                        "Expired BM. Skipping attempt to write characteristic to our service on ${gatt.device.address}"
                )

                endWorkConnection(gatt)
            }
        }

        override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    CentralLog.i(TAG, "Characteristic wrote successfully")
                    work.checklist.writeCharacteristic.status = true
                    work.checklist.writeCharacteristic.timePerformed = System.currentTimeMillis()
                }
                else -> {
                    CentralLog.i(TAG, "Failed to write characteristics: $status")
                }
            }

            endWorkConnection(gatt)
        }

    }

    fun terminateConnections() {
        CentralLog.d(TAG, "Cleaning up worker.")

        currentPendingConnection?.gatt?.disconnect()
        currentPendingConnection = null

        timeoutHandler.removeCallbacksAndMessages(null)
        queueHandler.removeCallbacksAndMessages(null)
        blacklistHandler.removeCallbacksAndMessages(null)

        //concurrent modifications?
        workQueue.clear()
        blacklist.clear()
    }

    fun unregisterReceivers() {
        try {
            localBroadcastManager.unregisterReceiver(deviceProcessedReceiver)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Unable to close receivers: ${e.localizedMessage}")
        }

        try {
            localBroadcastManager.unregisterReceiver(workReceiver)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Unable to close receivers: ${e.localizedMessage}")
        }
    }

    inner class DeviceProcessedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_DEVICE_PROCESSED == intent.action) {
                val deviceAddress = intent.getStringExtra(DEVICE_ADDRESS)
                CentralLog.d(TAG, "Adding to blacklist: $deviceAddress")
                val entry = BlacklistEntry(deviceAddress)
                blacklist.add(entry)
                blacklistHandler.postDelayed({
                    CentralLog.i(
                            TAG,
                            "blacklist for ${entry.uniqueIdentifier} removed? : ${blacklist.remove(entry)}"
                    )
                }, blacklistDuration)
            }
        }
    }

    inner class StreetPassWorkReceiver : BroadcastReceiver() {

        private val TAG = "StreetPassWorkReceiver"

        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let {
                if (ACTION_DEVICE_SCANNED == intent.action) {
                    //get data from extras
                    val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val connectable: ConnectablePeripheral? =
                            intent.getParcelableExtra(CONNECTION_DATA)

                    val devicePresent = device != null
                    val connectablePresent = connectable != null

                    CentralLog.i(
                            TAG,
                            "Device received: ${device?.address}. Device present: $devicePresent, Connectable Present: $connectablePresent"
                    )

                    device?.let {
                        connectable?.let {
                            val work = Work(device, connectable, onWorkTimeoutListener)
                            if (addWork(work)) {
                                doWork()
                            }
                        }
                    }
                }
            }
        }
    }
}

