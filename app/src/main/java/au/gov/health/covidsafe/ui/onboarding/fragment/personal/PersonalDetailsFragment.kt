package au.gov.health.covidsafe.ui.onboarding.fragment.personal

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import au.gov.health.covidsafe.ui.onboarding.fragment.enternumber.EnterNumberFragment
import kotlinx.android.synthetic.main.fragment_personal_details.*

class PersonalDetailsFragment : PagerChildFragment() {

    private var picker: NumberPicker? = null

    private var alertDialog: AlertDialog? = null
    override var stepProgress: Int? = 1
    override val navigationIcon: Int = R.drawable.ic_up
    private var ageSelected: Pair<String, String>? = null

    private val presenter = PersonalDetailsPresenter(this)

    private val nameTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            hideNameError()
            updateButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private val postCodeTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter.validateInlinePostCode(s.toString())
            updateButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_personal_details, container, false)

    override fun onResume() {
        super.onResume()
        personal_details_name.addTextChangedListener(nameTextWatcher)
        personal_details_post_code.addTextChangedListener(postCodeTextWatcher)
        personal_details_age.setOnClickListener {
            showAgePicker()
        }
        personal_details_age.text = ageSelected?.second

    }

    override fun onPause() {
        super.onPause()
        personal_details_name.removeTextChangedListener(nameTextWatcher)
        personal_details_post_code.removeTextChangedListener(postCodeTextWatcher)
        personal_details_age.setOnClickListener(null)
        alertDialog?.dismiss()
    }

    override fun getUploadButtonLayout(): UploadButtonLayout = UploadButtonLayout.ContinueLayout(R.string.personal_details_button) {
        presenter.saveInfos(personal_details_name.text.toString(), personal_details_post_code.text.toString(), getMidAgeToSend())
    }

    override fun updateButtonState() {
        if (presenter.validateInputsForButtonUpdate(personal_details_name.text.toString(), personal_details_post_code.text.toString(), getMidAgeToSend())) {
            enableContinueButton()
        } else {
            disableContinueButton()
        }
    }

    fun showGenericError() {
        activity?.let { activity ->
            alertDialog?.dismiss()
            alertDialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.generic_error)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, null).show()
        }
    }

    fun navigateToNextPage(minor: Boolean) {
        if (minor) {
            navigateTo(PersonalDetailsFragmentDirections.actionPersonalDetailsToUnderSixteenFragment().actionId)
        } else {
            val bundle = bundleOf(
                    EnterNumberFragment.ENTER_NUMBER_DESTINATION_ID to R.id.action_otpFragment_to_permissionFragment,
                    EnterNumberFragment.ENTER_NUMBER_PROGRESS to 2)
            navigateTo(PersonalDetailsFragmentDirections.actionPersonalDetailsToEnterNumberFragment().actionId, bundle)
        }
    }

    fun showPostcodeError() {
        personal_details_post_code_error.visibility = VISIBLE
    }

    fun hidePostcodeError() {
        personal_details_post_code_error.visibility = GONE
    }

    fun showNameError() {
        personal_details_name_error.visibility = VISIBLE
    }

    fun hideNameError() {
        personal_details_name_error.visibility = GONE
    }

    fun showAgeError() {
        personal_details_age_error.visibility = VISIBLE
    }

    fun hideAgeError() {
        personal_details_age_error.visibility = GONE
    }

    private fun showAgePicker() {
        activity?.let { activity ->
            val ages = resources.getStringArray(R.array.personal_details_age_array).map {
                it.split(":").let { it[0] to it[1] }
            }
            var selected = ages.firstOrNull { it == ageSelected }?.let {
                ages.indexOf(it)
            } ?: 0

            picker = NumberPicker(activity)
            picker?.minValue = 0
            picker?.maxValue = ages.size - 1
            picker?.displayedValues = ages.map { it.second }.toTypedArray()
            picker?.setOnValueChangedListener { _, _, newVal ->
                selected = newVal
            }
            picker?.value = selected
            alertDialog?.dismiss()
            alertDialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.personal_details_age_dialog_title)
                    .setView(picker)
                    .setPositiveButton(R.string.personal_details_dialog_ok) { _, _ ->
                        ageSelected = ages[selected]
                        personal_details_age.text = ages[selected].second
                        hideAgeError()
                        updateButtonState()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
        }
    }

    private fun getMidAgeToSend(): String? {
        val ages = resources.getStringArray(R.array.personal_details_age_array).map {
            it.split(":").let { it[0] to it[1] }
        }
        val selected = ages.firstOrNull { it == ageSelected }?.let {
            ages.indexOf(it)
        }
        return selected?.let {
            ages[selected].first
        }
    }
}