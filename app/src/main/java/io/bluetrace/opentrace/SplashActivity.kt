package io.bluetrace.opentrace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import io.bluetrace.opentrace.onboarding.PreOnboardingActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 2000
    var needToUpdateApp = false

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler = Handler()

        //check if the intent was from notification and its a update notification
        intent.extras?.let {
            val notifEvent: String? = it.getString("event", null)

            notifEvent?.let {
                if (it.equals("update")) {
                    needToUpdateApp = true
                    intent = Intent(Intent.ACTION_VIEW);
                    //Copy App URL from Google Play Store.
                    intent.data = Uri.parse(BuildConfig.STORE_URL)

                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        if (!needToUpdateApp) {
            mHandler.postDelayed({
                goToNextScreen()
                finish()
            }, SPLASH_TIME)
        }
    }

    private fun goToNextScreen() {
        if (!Preference.isOnBoarded(this)) {
            startActivity(Intent(this, PreOnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
