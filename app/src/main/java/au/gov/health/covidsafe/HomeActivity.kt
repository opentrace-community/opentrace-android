package au.gov.health.covidsafe

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class HomeActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Utils.startBluetoothMonitoringService(this)

    }
}
