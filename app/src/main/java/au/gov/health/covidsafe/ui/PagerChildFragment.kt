package au.gov.health.covidsafe.ui

import androidx.annotation.StringRes

abstract class PagerChildFragment : BaseFragment() {
    override fun onResume() {
        super.onResume()
        updateToolBar()
        updateButton()
        updateProgressBar()
        updateButtonState()
    }

    private fun updateProgressBar() {
        (parentFragment?.parentFragment as? PagerContainer)?.updateProgressBar(stepProgress)
        (activity as? PagerContainer)?.updateProgressBar(stepProgress)
    }

    private fun updateToolBar() {
        (parentFragment?.parentFragment as? PagerContainer)?.setNavigationIcon(navigationIcon)
        (activity as? PagerContainer)?.setNavigationIcon(navigationIcon)
    }

    private fun updateButton() {
        val updateButtonLayout = getUploadButtonLayout()
        if (updateButtonLayout is UploadButtonLayout.ContinueLayout) {
            updateButtonState()
        }
        (parentFragment?.parentFragment as? PagerContainer)?.refreshButton(updateButtonLayout)
        (activity as? PagerContainer)?.refreshButton(updateButtonLayout)
    }

    fun enableContinueButton() {
        (parentFragment?.parentFragment as? PagerContainer)?.enableNextButton()
        (activity as? PagerContainer)?.enableNextButton()
    }

    fun disableContinueButton() {
        (parentFragment?.parentFragment as? PagerContainer)?.disableNextButton()
        (activity as? PagerContainer)?.disableNextButton()
    }

    fun showLoading() {
        (parentFragment?.parentFragment as? PagerContainer)?.showLoading()
        (activity as? PagerContainer)?.showLoading()
    }

    fun hideLoading() {
        (parentFragment?.parentFragment as? PagerContainer)?.hideLoading((getUploadButtonLayout() as? UploadButtonLayout.ContinueLayout)?.buttonText)
        (activity as? PagerContainer)?.hideLoading((getUploadButtonLayout() as? UploadButtonLayout.ContinueLayout)?.buttonText)
    }

    abstract val navigationIcon: Int?
    abstract var stepProgress: Int?
    abstract fun getUploadButtonLayout(): UploadButtonLayout
    abstract fun updateButtonState()
}

sealed class UploadButtonLayout {
    class ContinueLayout(@StringRes val buttonText: Int, val buttonListener: (() -> Unit)?) : UploadButtonLayout()
    class QuestionLayout(val buttonYesListener: () -> Unit, val buttonNoListener: () -> Unit) : UploadButtonLayout()
}

