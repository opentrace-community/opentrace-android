package io.bluetrace.opentrace.onboarding

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.android.synthetic.main.activity_onboarding.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import io.bluetrace.opentrace.BuildConfig
import io.bluetrace.opentrace.Preference
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.idmanager.TempIDManager
import io.bluetrace.opentrace.logging.CentralLog
import io.bluetrace.opentrace.services.BluetoothMonitoringService
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456
private const val BATTERY_OPTIMISER = 789

class OnboardingActivity : FragmentActivity(),
    SetupFragment.OnFragmentInteractionListener,
    SetupCompleteFragment.OnFragmentInteractionListener,
    RegisterNumberFragment.OnFragmentInteractionListener,
    OTPFragment.OnFragmentInteractionListener,
    TOUFragment.OnFragmentInteractionListener {

    private var TAG: String = "OnboardingActivity"
    private var pagerAdapter: ScreenSlidePagerAdapter? = null
    private var bleSupported = false
    private var speedUp = false
    private var resendingCode = false

    private val functions = FirebaseFunctions.getInstance(BuildConfig.FIREBASE_REGION)
    private var credential: PhoneAuthCredential by Delegates.notNull()
    private var verificationId: String by Delegates.notNull()
    private var resendToken: PhoneAuthProvider.ForceResendingToken by Delegates.notNull()
    private val phoneNumberVerificationCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(receivedCredential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                CentralLog.d(TAG, "onVerificationCompleted: $receivedCredential")
                credential = receivedCredential
                signInWithPhoneAuthCredential(credential)
                speedUp = true
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    CentralLog.d(TAG, "FirebaseAuthInvalidCredentialsException", e)
//                    alertDialog(getString(R.string.verification_failed))
                    updatePhoneNumberError(getString(R.string.invalid_number))

                } else if (e is FirebaseTooManyRequestsException) {
                    CentralLog.d(TAG, "FirebaseTooManyRequestsException", e)
                    alertDialog(getString(R.string.too_many_requests))
                }

                enableFragmentbutton()

                CentralLog.d(TAG, "On Verification failure: ${e.message}")
                onboardingActivityLoadingProgressBarFrame.visibility = View.GONE
            }

            override fun onCodeSent(
                receivedVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                verificationId = receivedVerificationId
                resendToken = token

                CentralLog.d(TAG, "onCodeSent: $receivedVerificationId")
                if (resendingCode) {
                    onboardingActivityLoadingProgressBarFrame.visibility = View.GONE
                } else {
                    navigateToNextPage()
                }

            }
        }

    private fun enableFragmentbutton() {
        var interfaceObject: OnboardingFragmentInterface? = pagerAdapter?.getItem(pager.currentItem)
        interfaceObject?.enableButton()
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(desc)
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    CentralLog.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    if (BluetoothMonitoringService.broadcastMessage == null || TempIDManager.needToUpdate(
                            applicationContext
                        )
                    ) {
                        getTemporaryID()
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    CentralLog.d(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        onboardingActivityLoadingProgressBarFrame.visibility = View.GONE
                        updateOTPError(getString(R.string.invalid_otp))
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        alertDialog(getString(R.string.invalid_user))
                    }
                    onboardingActivityLoadingProgressBarFrame.visibility = View.GONE
                }
            }
    }

    private fun getTemporaryID(): Task<HttpsCallableResult> {
        return TempIDManager.getTemporaryIDs(this, functions)
            .addOnCompleteListener {
                CentralLog.d(TAG, "Retrieved Temporary ID successfully")
                Utils.getHandShakePin(this, functions).addOnCompleteListener {
                    if (it.isSuccessful) {
                        CentralLog.d(TAG, "Retrieved HandShakePin successfully")
                        navigateToNextPage()
                    } else {
                        CentralLog.e(
                            TAG,
                            "Failed to retrieve HandShakePin ${it.exception?.message}"
                        )
                        updateOTPError(getString(R.string.verification_failed))
                        onboardingActivityLoadingProgressBarFrame.visibility = View.GONE
                    }
                }

            }
    }

    private var mIsOpenSetting = false
    private var mIsResetup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter

        tabDots.setupWithViewPager(pager, true)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                CentralLog.d(TAG, "OnPageScrollStateChanged")
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                CentralLog.d(TAG, "OnPageScrolled")
            }

            override fun onPageSelected(position: Int) {
                CentralLog.d(TAG, "position: $position")
                val onboardingFragment: OnboardingFragmentInterface =
                    pagerAdapter!!.getItem(position)
                onboardingFragment.becomesVisible()
                when (position) {
                    0 -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    1 -> {
                        //Cannot put check point at this page without triggering OTP
                    }
                    2 -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    3 -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    4 -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                }

            }
        })

        //disable swiping
        pager.setPagingEnabled(false)
        pager.offscreenPageLimit = 5

        val extras = intent.extras
        if (extras != null) {
            mIsResetup = true
            var page = extras.getInt("page", 0)
            navigateTo(page)
        } else {
            var checkPoint = Preference.getCheckpoint(this)
            navigateTo(checkPoint)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsOpenSetting) {
            Handler().postDelayed(Runnable { setupPermissionsAndSettings() }, 1000)
        }
    }

    override fun onBackPressed() {
        if (pager.currentItem > 0 && pager.currentItem != 2) {
            navigateToPreviousPage()
            return
        }
        super.onBackPressed()
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun enableBluetooth() {
        CentralLog.d(TAG, "[enableBluetooth]")
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.let {
            if (it.isDisabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            } else {
                setupPermissionsAndSettings()
            }
        }
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ACCESS_LOCATION)
    fun setupPermissionsAndSettings() {
        CentralLog.d(TAG, "[setupPermissionsAndSettings]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var perms = Utils.getRequiredPermissions()

            if (EasyPermissions.hasPermissions(this, *perms)) {
                // Already have permission, do the thing
                initBluetooth()
                excludeFromBatteryOptimization()
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                    this, getString(R.string.permission_location_rationale),
                    PERMISSION_REQUEST_ACCESS_LOCATION, *perms
                )
            }
        } else {
            initBluetooth()
            navigateToNextPage()
        }
    }

    private fun initBluetooth() {
        checkBLESupport()
    }

    private fun checkBLESupport() {
        CentralLog.d(TAG, "[checkBLESupport] ")
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported) {
            bleSupported = false
            Utils.stopBluetoothMonitoringService(this)
        } else {
            bleSupported = true
        }
    }

    private fun excludeFromBatteryOptimization() {
        CentralLog.d(TAG, "[excludeFromBatteryOptimization] ")
        val powerManager =
            this.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val packageName = this.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent =
                Utils.getBatteryOptimizerExemptionIntent(
                    packageName
                )

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                CentralLog.d(TAG, "Not on Battery Optimization whitelist")
                //check if there's any activity that can handle this
                if (Utils.canHandleIntent(
                        intent,
                        packageManager
                    )
                ) {
                    this.startActivityForResult(
                        intent,
                        BATTERY_OPTIMISER
                    )
                } else {
                    //no way of handling battery optimizer
                    navigateToNextPage()
                }
            } else {
                CentralLog.d(TAG, "On Battery Optimization whitelist")
                navigateToNextPage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        CentralLog.d(TAG, "requestCode $requestCode resultCode $resultCode")
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
                return
            } else {
                setupPermissionsAndSettings()
            }
        } else if (requestCode == BATTERY_OPTIMISER) {
            if (resultCode != Activity.RESULT_CANCELED) {

//                Utils.keepServicesInChineseDevices(this)
                Handler().postDelayed({
                    navigateToNextPage()
                }, 1000)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLog.d(TAG, "[onRequestPermissionsResult] requestCode $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_LOCATION -> {
                for (x in 0 until permissions.size) {
                    var permission = permissions[x]
                    if (grantResults[x] == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            var showRationale = shouldShowRequestPermissionRationale(permission)
                            if (!showRationale) {

                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(this)
                                // set message of alert dialog
                                dialogBuilder.setMessage(getString(R.string.open_location_setting))
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton(
                                        getString(R.string.ok),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            CentralLog.d(TAG, "user also CHECKED never ask again")
                                            mIsOpenSetting = true
                                            var intent =
                                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            var uri: Uri =
                                                Uri.fromParts("package", packageName, null)
                                            intent.data = uri
                                            startActivity(intent)

                                        })
                                    // negative button text and action
                                    .setNegativeButton(
                                        getString(R.string.cancel),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            dialog.cancel()
                                        })

                                // create dialog box
                                val alert = dialogBuilder.create()

                                // show alert dialog
                                alert.show()

                            } else if (Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                                CentralLog.d(TAG, "user did not CHECKED never ask again")
                            } else {
                                excludeFromBatteryOptimization()
                            }
                        }
                    } else if (grantResults[x] == PackageManager.PERMISSION_GRANTED) {
                        excludeFromBatteryOptimization()
                    }
                }
            }
        }
    }

    fun navigateToNextPage() {
        CentralLog.d(TAG, "Navigating to next page")
        onboardingActivityLoadingProgressBarFrame.visibility = View.GONE

        if (!speedUp) {
            pager.currentItem = pager.currentItem + 1
            pagerAdapter!!.notifyDataSetChanged()
        } else {
            pager.currentItem = pager.currentItem + 2
            pagerAdapter!!.notifyDataSetChanged()
            speedUp = false
        }
    }

    fun navigateToPreviousPage() {
        CentralLog.d(TAG, "Navigating to previous page")
        if (mIsResetup) {
            if (pager.currentItem >= 4) {
                pager.currentItem = pager.currentItem - 1
                pagerAdapter!!.notifyDataSetChanged()
            } else {
                finish()
            }
        } else {
            pager.currentItem = pager.currentItem - 1
            pagerAdapter!!.notifyDataSetChanged()
        }
    }

    private fun navigateTo(page: Int) {
        CentralLog.d(TAG, "Navigating to page")
        pager.currentItem = page
        pagerAdapter!!.notifyDataSetChanged()
    }

    fun requestForOTP(phoneNumber: String) {
        onboardingActivityLoadingProgressBarFrame.visibility = View.VISIBLE
        speedUp = false
        resendingCode = false
        PhoneAuthProvider
            .getInstance()
            .verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                phoneNumberVerificationCallbacks
            )
    }

    fun validateOTP(otp: String) {
        if (TextUtils.isEmpty(otp) || otp.length < 6) {
            updateOTPError(getString(R.string.must_be_six_digit))
            return
        }
        onboardingActivityLoadingProgressBarFrame.visibility = View.VISIBLE

        credential = PhoneAuthProvider.getCredential(
            verificationId,
            otp
        )
        signInWithPhoneAuthCredential(credential)
    }

    fun resendCode(phoneNumber: String) {
        onboardingActivityLoadingProgressBarFrame.visibility = View.VISIBLE
        speedUp = false
        resendingCode = true
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,        // Phone number to verify
            60,                 // Timeout duration
            TimeUnit.SECONDS,   // Unit of timeout
            this,               // Activity (for callback binding)
            phoneNumberVerificationCallbacks,         // OnVerificationStateChangedCallbacks
            resendToken
        )             // ForceResendingToken from callbacks
    }

    fun updatePhoneNumber(num: String) {
        val onboardingFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(1)
        onboardingFragment.onUpdatePhoneNumber(num)
    }

    fun updatePhoneNumberError(error: String) {
        val registerNumberFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(0)
        registerNumberFragment.onError(error)
    }

    private fun updateOTPError(error: String) {
        val onboardingFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(1)
        onboardingFragment.onError(error)
    }

    override fun onFragmentInteraction(uri: Uri) {
        CentralLog.d(TAG, "########## fragment interaction: $uri")
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        val fragmentMap: MutableMap<Int, OnboardingFragmentInterface> = HashMap()

        override fun getCount(): Int = 5

        override fun getItem(position: Int): OnboardingFragmentInterface {
            return fragmentMap.getOrPut(position, { createFragAtIndex(position) })
        }

        private fun createFragAtIndex(index: Int): OnboardingFragmentInterface {
            return when (index) {
                0 -> return RegisterNumberFragment()
                1 -> return OTPFragment()
                2 -> return TOUFragment()
                3 -> return SetupFragment()
                4 -> return SetupCompleteFragment()
                else -> {
                    RegisterNumberFragment()
                }
            }
        }

    }

}
