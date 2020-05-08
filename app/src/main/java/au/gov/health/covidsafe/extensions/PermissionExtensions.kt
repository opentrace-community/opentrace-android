package au.gov.health.covidsafe.extensions

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.Utils

const val REQUEST_ENABLE_BT = 123
const val LOCATION = 345
const val BATTERY_OPTIMISER = 789

fun Fragment.requestAllPermissions(onEndCallback: () -> Unit) {
    if (isBlueToothEnabled() ?: true) {
        requestFineLocationAndCheckBleSupportThenNextPermission(onEndCallback)
    } else {
        requestBlueToothPermissionThenNextPermission()
    }
}

fun Fragment.requestBlueToothPermissionThenNextPermission() {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
}

fun Fragment.checkBLESupport() {
    if (BluetoothAdapter.getDefaultAdapter()?.isMultipleAdvertisementSupported?.not() ?: false) {
        activity?.let {
            Utils.stopBluetoothMonitoringService(it)
        }
    }
}

private fun Fragment.requestFineLocationAndCheckBleSupportThenNextPermission(onEndCallback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity?.let {
            when {
                EasyPermissions.hasPermissions(it, ACCESS_FINE_LOCATION) -> {
                    checkBLESupport()
                    excludeFromBatteryOptimization(onEndCallback)
                }
                else -> {
                    EasyPermissions.requestPermissions(
                            PermissionRequest.Builder(this, LOCATION, ACCESS_FINE_LOCATION)
                                    .setRationale(R.string.permission_location_rationale)
                                    .build())
                }
            }
        }
    } else {
        checkBLESupport()
    }
}

fun Fragment.excludeFromBatteryOptimization(onEndCallback: (() -> Unit)? = null) {
    activity?.let {
        val powerManager =
                it.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val packageName = it.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Utils.getBatteryOptimizerExemptionIntent(packageName)
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                //check if there's any activity that can handle this
                if (Utils.canHandleIntent(intent, it.packageManager)) {
                    this.startActivityForResult(intent, BATTERY_OPTIMISER)
                } else {
                    //no way of handling battery optimizer
                    onEndCallback?.invoke()
                }
            } else {
                onEndCallback?.invoke()
            }
        }
    }

}

fun Fragment.isBlueToothEnabled(): Boolean? {
    val bluetoothManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    return bluetoothManager?.adapter?.isEnabled
}

fun Fragment.isPushNotificationEnabled(): Boolean? {
    return activity?.let { activity ->
        NotificationManagerCompat.from(activity).areNotificationsEnabled()
    }
}

fun Fragment.isFineLocationEnabled(): Boolean? {
    return activity?.let { activity ->
        EasyPermissions.hasPermissions(activity, ACCESS_FINE_LOCATION)
    }
}

fun Fragment.isNonBatteryOptimizationAllowed(): Boolean? {
    return activity?.let { activity ->
        val powerManager = activity.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager?
        val packageName = activity.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(packageName) ?: true
        } else {
            null
        }
    } ?: run {
        null
    }
}

fun Fragment.askForLocationPermission() {
    activity?.let {
        when {
            EasyPermissions.hasPermissions(it, ACCESS_FINE_LOCATION) -> {

            }
            EasyPermissions.somePermissionPermanentlyDenied(this, listOf(ACCESS_FINE_LOCATION)) -> {
                AppSettingsDialog.Builder(this).build().show()
            }
            else -> {
                EasyPermissions.requestPermissions(
                        PermissionRequest.Builder(this, LOCATION, ACCESS_FINE_LOCATION)
                                .setRationale(R.string.permission_location_rationale)
                                .build())
            }
        }
    }
}
