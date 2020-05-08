package au.gov.health.covidsafe

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_self_isolation.*

class SelfIsolationDoneActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_isolation)
    }

    override fun onResume() {
        super.onResume()
        activity_self_isolation_next.setOnClickListener {
            Preference.setDataIsUploaded(this, false)
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        activity_self_isolation_next.setOnClickListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        root.removeAllViews()
    }
}