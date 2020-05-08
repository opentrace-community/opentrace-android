package au.gov.health.covidsafe.ui.onboarding.fragment.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import au.gov.health.covidsafe.HomeActivity
import au.gov.health.covidsafe.Preference
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.TracerApp
import au.gov.health.covidsafe.extensions.*
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_permission.*
import pub.devrel.easypermissions.EasyPermissions

class PermissionFragment : PagerChildFragment(), EasyPermissions.PermissionCallbacks {

    companion object {

        val requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override val navigationIcon: Int? = R.drawable.ic_up
    override var stepProgress: Int? = 5

    private var navigationStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_permission, container, false)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                excludeFromBatteryOptimization { navigateToNextPage() }
                return
            } else {
                requestAllPermissions { navigateToNextPage() }
            }
        } else if (requestCode == BATTERY_OPTIMISER) {
            Handler().postDelayed({
                navigateToNextPage()
            }, 1000)
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToNextPage() {
        navigationStarted = false
        if (hasAllPermissionsAndBluetoothOn()) {
            navigateTo(R.id.action_permissionFragment_to_permissionSuccessFragment)
        } else {
            navigateToMainActivity()
        }
    }

    private fun hasAllPermissionsAndBluetoothOn(): Boolean {
        val context = TracerApp.AppContext
        return isBlueToothEnabled() == true
                && requiredPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
                && ContextCompat.getSystemService(context, PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity?.startActivity(intent)
        activity?.finish()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION) {
            excludeFromBatteryOptimization { navigateToNextPage() }
        } else {
            requestAllPermissions { navigateToNextPage() }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        requestAllPermissions { navigateToNextPage() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.permission_button) {
        disableContinueButton()
        navigationStarted = true
        activity?.let {
            Preference.putIsOnBoarded(it, true)
        }
        requestAllPermissions {
            navigateToNextPage()
        }
    }

    override fun updateButtonState() {
        if (navigationStarted) {
            disableContinueButton()
        } else {
            enableContinueButton()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }
}