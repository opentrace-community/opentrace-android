package au.gov.health.covidsafe.ui.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.upload.presentation.VerifyUploadPinFragment
import kotlinx.android.synthetic.main.dialog_error_uploading.*

class UploadingErrorDialog(context: Context, private val listener: VerifyUploadPinFragment.OnUploadErrorInterface) : Dialog(context) {

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_error_uploading)
        val window: Window? = this.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog_error_positive.setOnClickListener {
            listener.onPositiveClicked()
        }
        dialog_error_negative.setOnClickListener {
            listener.onNegativeClicked()
        }
    }
}