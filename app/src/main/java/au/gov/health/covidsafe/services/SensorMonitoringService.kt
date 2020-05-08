package au.gov.health.covidsafe.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import au.gov.health.covidsafe.logging.CentralLog
import kotlin.math.sqrt

class SensorMonitoringService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var _light: FloatArray? = null
    private var _proximity: FloatArray? = null
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (proximitySensor != null) {
            CentralLog.d(TAG, "Proximity sensor: $proximitySensor")
            sensorManager.registerListener(this, proximitySensor, SENSOR_DELAY_SUPER_SLOW)

        } else {
            CentralLog.d(TAG, "Proximity sensor not available")
        }

        if (lightSensor != null) {
            CentralLog.d(TAG, "Light sensor: $lightSensor")
            sensorManager.registerListener(this, lightSensor, SENSOR_DELAY_SUPER_SLOW)
        } else {
            CentralLog.d(TAG, "Light sensor not available")
        }

        CentralLog.d(TAG, "SensorMonitoringService started")
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        CentralLog.d(TAG, "SensorMonitoringService destroyed")
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): SensorMonitoringService = this@SensorMonitoringService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        CentralLog.d(TAG, "Sensor accuracy changed! $sensor")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                _proximity = event.values
            }
            Sensor.TYPE_LIGHT -> {
                _light = event.values
            }
            else -> {
                CentralLog.w(TAG, "Unexpected sensor type changed: ${event.sensor.type}")
            }
        }
    }

    val proximity
        get() = if (_proximity != null) {
            sqrt((_proximity as FloatArray).reduce { acc: Float, n: Float -> acc + n * n })
        } else {
            -1.0f
        }

    val light
        get() = if (_light != null) {
            sqrt((_light as FloatArray).reduce { acc: Float, n: Float -> acc + n * n })
        } else {
            -1.0f
        }

    companion object {
        const val TAG = "SensorMonitoringService"
        const val SENSOR_DELAY_SUPER_SLOW = 3_000_000
    }
}
