package au.gov.health.covidsafe.ui.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.Preference
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.WebViewActivity
import au.gov.health.covidsafe.extensions.*
import au.gov.health.covidsafe.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home_external_links.*
import kotlinx.android.synthetic.main.fragment_home_setup_complete_header.*
import kotlinx.android.synthetic.main.fragment_home_setup_incomplete_content.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var presenter: HomePresenter

    private var mIsBroadcastListenerRegistered = false

    private var counter: Int = 0

    private val mBroadcastListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_OFF -> {
                        bluetooth_card_view.render(formatBlueToothTitle(false), false)
                        refreshSetupCompleteOrIncompleteUi()
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        bluetooth_card_view.render(formatBlueToothTitle(false), false)
                        refreshSetupCompleteOrIncompleteUi()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        bluetooth_card_view.render(formatBlueToothTitle(true), true)
                        refreshSetupCompleteOrIncompleteUi()
                    }
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        presenter = HomePresenter(this)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.home_header_help.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHelpFragment())
        }
        if (BuildConfig.ENABLE_DEBUG_SCREEN) {
            view.header_background.setOnClickListener {
                counter++
                if (counter >= 2) {
                    counter = 0
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPeekActivity())
                }
            }
        }
        home_version_number.text = getString(R.string.home_version_number, BuildConfig.VERSION_NAME)
    }

    override fun onResume() {
        super.onResume()
        bluetooth_card_view.setOnClickListener { requestBlueToothPermissionThenNextPermission() }
        location_card_view.setOnClickListener { askForLocationPermission() }
        battery_card_view.setOnClickListener { excludeFromBatteryOptimization() }
        home_been_tested_button.setOnClickListener {
            navigateTo(R.id.action_home_to_selfIsolate)
        }
        home_setup_complete_share.setOnClickListener {
            shareThisApp()
        }
        home_setup_complete_news.setOnClickListener {
            goToNewsWebsite()
        }
        home_setup_complete_app.setOnClickListener {
            goToCovidApp()
        }

        if (!mIsBroadcastListenerRegistered) {
            registerBroadcast()
        }
        refreshSetupCompleteOrIncompleteUi()
    }

    override fun onPause() {
        super.onPause()
        bluetooth_card_view.setOnClickListener(null)
        location_card_view.setOnClickListener(null)
        battery_card_view.setOnClickListener(null)
        home_been_tested_button.setOnClickListener(null)
        home_setup_complete_share.setOnClickListener(null)
        home_setup_complete_news.setOnClickListener(null)
        home_setup_complete_app.setOnClickListener(null)
        activity?.let { activity ->
            if (mIsBroadcastListenerRegistered) {
                activity.unregisterReceiver(mBroadcastListener)
                mIsBroadcastListenerRegistered = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        home_root.removeAllViews()
    }

    private fun refreshSetupCompleteOrIncompleteUi() {
        val isUploaded = context?.let {
            Preference.isDataUploaded(it)
        } ?: run {
            false
        }
        home_been_tested_button.visibility = if (isUploaded) GONE else VISIBLE
        when {
            !allPermissionsEnabled() -> {
                home_header_setup_complete_header_uploaded.visibility = GONE
                home_header_setup_complete_header_divider.visibility = GONE
                home_header_setup_complete_header.setText(R.string.home_header_inactive_title)
                home_header_picture_setup_complete.setImageResource(R.drawable.ic_logo_home_inactive)
                home_header_help.setImageResource(R.drawable.ic_help_outline_black)
                context?.let { context ->
                    val backGroundColor = ContextCompat.getColor(context, R.color.grey)
                    header_background.setBackgroundColor(backGroundColor)
                    header_background_overlap.setBackgroundColor(backGroundColor)

                    val textColor = ContextCompat.getColor(context, R.color.slack_black)
                    home_header_setup_complete_header_uploaded.setTextColor(textColor)
                    home_header_setup_complete_header.setTextColor(textColor)
                }
                content_setup_incomplete_group.visibility = VISIBLE
                updateBlueToothStatus()
                updatePushNotificationStatus()
                updateBatteryOptimizationStatus()
                updateLocationStatus()
            }
            isUploaded -> {
                home_header_setup_complete_header_uploaded.visibility = VISIBLE
                home_header_setup_complete_header_divider.visibility = VISIBLE
                home_header_setup_complete_header.setText(R.string.home_header_active_title)
                home_header_picture_setup_complete.setImageResource(R.drawable.ic_logo_home_uploaded)
                home_header_picture_setup_complete.setAnimation("spinner_home_upload_complete.json")
                home_header_help.setImageResource(R.drawable.ic_help_outline_white)
                content_setup_incomplete_group.visibility = GONE
                context?.let { context ->
                    val backGroundColor = ContextCompat.getColor(context, R.color.dark_green)
                    header_background.setBackgroundColor(backGroundColor)
                    header_background_overlap.setBackgroundColor(backGroundColor)

                    val textColor = ContextCompat.getColor(context, R.color.white)
                    home_header_setup_complete_header_uploaded.setTextColor(textColor)
                    home_header_setup_complete_header.setTextColor(textColor)
                }
            }

            else -> {
                home_header_setup_complete_header_uploaded.visibility = GONE
                home_header_setup_complete_header_divider.visibility = GONE
                home_header_setup_complete_header.setText(R.string.home_header_active_title)
                home_header_help.setImageResource(R.drawable.ic_help_outline_black)
                home_header_picture_setup_complete.setAnimation("spinner_home.json")
                content_setup_incomplete_group.visibility = GONE
                context?.let { context ->
                    val backGroundColor = ContextCompat.getColor(context, R.color.lighter_green)
                    header_background.setBackgroundColor(backGroundColor)
                    header_background_overlap.setBackgroundColor(backGroundColor)

                    val textColor = ContextCompat.getColor(context, R.color.slack_black)
                    home_header_setup_complete_header_uploaded.setTextColor(textColor)
                    home_header_setup_complete_header.setTextColor(textColor)
                }
            }
        }
    }

    private fun allPermissionsEnabled(): Boolean {
        val bluetoothEnabled = isBlueToothEnabled() ?: true
        val pushNotificationEnabled = isPushNotificationEnabled() ?: true
        val nonBatteryOptimizationAllowed = isNonBatteryOptimizationAllowed() ?: true
        val locationStatusAllowed = isFineLocationEnabled() ?: true

        return bluetoothEnabled &&
                pushNotificationEnabled &&
                nonBatteryOptimizationAllowed &&
                locationStatusAllowed
    }

    private fun registerBroadcast() {
        activity?.let { activity ->
            var f = IntentFilter()
            activity.registerReceiver(mBroadcastListener, f)
            // bluetooth on/off
            f = IntentFilter()
            f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            activity.registerReceiver(mBroadcastListener, f)
            mIsBroadcastListenerRegistered = true
        }
    }

    private fun shareThisApp() {
        val newIntent = Intent(Intent.ACTION_SEND)
        newIntent.type = "text/plain"
        newIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_this_app_content))
        newIntent.putExtra(Intent.EXTRA_HTML_TEXT, getString(R.string.share_this_app_content_html))
        startActivity(Intent.createChooser(newIntent, null))
    }

    private fun updateBlueToothStatus() {
        isBlueToothEnabled()?.let {
            bluetooth_card_view.visibility = VISIBLE
            bluetooth_card_view.render(formatBlueToothTitle(it), it)
        } ?: run {
            bluetooth_card_view.visibility = GONE
        }
    }

    private fun updatePushNotificationStatus() {
        isPushNotificationEnabled()?.let {
            push_card_view.visibility = VISIBLE
            push_card_view.render(formatPushNotificationTitle(it), it)
        } ?: run {
            push_card_view.visibility = GONE
        }
    }

    private fun updateBatteryOptimizationStatus() {
        isNonBatteryOptimizationAllowed()?.let {
            battery_card_view.visibility = VISIBLE
            battery_card_view.render(formatNonBatteryOptimizationTitle(!it), it)
        } ?: run {
            battery_card_view.visibility = GONE
        }
    }

    private fun updateLocationStatus() {
        isFineLocationEnabled()?.let {
            location_card_view.visibility = VISIBLE
            location_card_view.render(formatLocationTitle(it), it)
        } ?: run {
            location_card_view.visibility = VISIBLE
        }
    }

    private fun formatBlueToothTitle(on: Boolean): String {
        return resources.getString(R.string.home_bluetooth_permission, getPermissionEnabledTitle(on))
    }

    private fun formatLocationTitle(on: Boolean): String {
        return resources.getString(R.string.home_location_permission, getPermissionEnabledTitle(on))
    }

    private fun formatNonBatteryOptimizationTitle(on: Boolean): String {
        return resources.getString(R.string.home_non_battery_optimization_permission, getPermissionEnabledTitle(on))
    }

    private fun formatPushNotificationTitle(on: Boolean): String {
        return resources.getString(R.string.home_push_notification_permission, getPermissionEnabledTitle(on))
    }

    private fun getPermissionEnabledTitle(on: Boolean): String {
        return resources.getString(if (on) R.string.home_permission_on else R.string.home_permission_off)
    }

    private fun goToNewsWebsite() {
        val url = getString(R.string.home_set_complete_external_link_news_url)
        try {
            Intent(Intent.ACTION_VIEW).run {
                data = Uri.parse(url)
                startActivity(this)
            }
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(activity, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.URL_ARG, url)
            startActivity(intent)
        }
    }

    private fun goToCovidApp() {
        val url = getString(R.string.home_set_complete_external_link_app_url)
        try {
            Intent(Intent.ACTION_VIEW).run {
                data = Uri.parse(url)
                startActivity(this)
            }
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(activity, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.URL_ARG, url)
            startActivity(intent)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION && EasyPermissions.somePermissionPermanentlyDenied(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION) {
            checkBLESupport()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}
