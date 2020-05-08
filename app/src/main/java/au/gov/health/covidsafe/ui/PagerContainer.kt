package au.gov.health.covidsafe.ui

import androidx.annotation.StringRes

interface PagerContainer {
    fun enableNextButton()
    fun disableNextButton()
    fun showLoading()
    fun hideLoading(@StringRes stringRes: Int?)
    fun updateProgressBar(stepProgress: Int?)
    fun setNavigationIcon(navigationIcon: Int?)
    fun refreshButton(updateButtonLayout: UploadButtonLayout)
}