package au.gov.health.covidsafe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import au.gov.health.covidsafe.ui.onboarding.OnboardingActivity
import java.util.*

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 2000

    private var retryProviderInstall: Boolean = false
    private val ERROR_DIALOG_REQUEST_CODE = 1

    private var updateFlag = false

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        hideSystemUI()
        mHandler = Handler()

        Preference.putDeviceID(this, Settings.Secure.getString(this.contentResolver,
                Settings.Secure.ANDROID_ID))

    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        if (!updateFlag) {
            mHandler.postDelayed({
                goToNextScreen()
                finish()
            }, SPLASH_TIME)
        }
    }

    private fun goToNextScreen() {
        val dateUploaded = Calendar.getInstance().also {
            it.timeInMillis = Preference.getDataUploadedDateMs(this)
        }
        val fourteenDaysAgo = Calendar.getInstance().also {
            it.add(Calendar.DATE, -14)
        }
        startActivity(Intent(this, if (!Preference.isOnBoarded(this)) {
            OnboardingActivity::class.java
        } else if (dateUploaded.before(fourteenDaysAgo)) {
            SelfIsolationDoneActivity::class.java
        } else {
            HomeActivity::class.java
        }))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            retryProviderInstall = true
        }
    }

    // This snippet hides the system bars.
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}