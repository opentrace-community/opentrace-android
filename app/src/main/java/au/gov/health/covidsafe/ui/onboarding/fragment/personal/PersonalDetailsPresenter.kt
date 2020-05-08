package au.gov.health.covidsafe.ui.onboarding.fragment.personal

import au.gov.health.covidsafe.Preference
import java.util.regex.Pattern

class PersonalDetailsPresenter(private val personalDetailsFragment: PersonalDetailsFragment) {

    private val TAG = this.javaClass.simpleName

    private val POST_CODE_REGEX = Pattern.compile("^(?:(?:[2-8]\\d|9[0-7]|0?[28]|0?9(?=09))(?:\\d{2}))$")

    fun saveInfos(name: String?, postCode: String?, age: String?) {
        personalDetailsFragment.showLoading()
        personalDetailsFragment.context?.let { context ->
            val ageInt = age?.toIntOrNull()
            val nameValid = name.isNullOrBlank().not()
            val postCodeValid = postCode.isNullOrBlank().not() && isPostCodeValid(postCode)
            val ageValid = age.isNullOrBlank().not()

            if (nameValid && postCodeValid && ageValid) {
                val valid = (name?.let { name ->
                    Preference.putName(context, name)
                } ?: false) &&
                        (age?.let { age ->
                            Preference.putAge(context, age)
                        } ?: false) &&
                        (postCode?.let { postCode ->
                            Preference.putPostCode(context, postCode)
                        } ?: false)

                if (valid) {
                    personalDetailsFragment.hideLoading()
                    personalDetailsFragment.navigateToNextPage(ageInt?.let { it < 16 } ?: false)
                } else {
                    personalDetailsFragment.hideLoading()
                    personalDetailsFragment.showGenericError()
                }
            } else {
                showFieldsError(name, postCode, age)
                personalDetailsFragment.hideLoading()
            }
        } ?: run {
            personalDetailsFragment.hideLoading()
            personalDetailsFragment.showGenericError()
        }
    }

    private fun showFieldsError(name: String?, postCode: String?, age: String?) {
        updateNameFieldError(name)
        updateAgeFieldError(age)
        updatePostcodeFieldError(postCode)
    }

    private fun updateAgeFieldError(age: String?) {
        return if (age.isNullOrBlank()) {
            personalDetailsFragment.showAgeError()
        } else {
            personalDetailsFragment.hideAgeError()
        }
    }

    private fun updateNameFieldError(name: String?) {
        return if (name.isNullOrBlank()) {
            personalDetailsFragment.showNameError()
        } else {
            personalDetailsFragment.hideNameError()
        }
    }

    private fun updatePostcodeFieldError(postCode: String?) {
        return if (postCode.isNullOrBlank()) {
            personalDetailsFragment.showPostcodeError()
        } else {
            personalDetailsFragment.hidePostcodeError()
        }
    }

    fun validateInputsForButtonUpdate(name: String?, postCode: String?, age: String?): Boolean {
        val nameValid = name.isNullOrBlank().not()
        val postCodeValid = postCode.isNullOrBlank().not() && isPostCodeValid(postCode)
        val ageValid = age.isNullOrBlank().not()

        return nameValid && postCodeValid && ageValid
    }

    internal fun validateInlinePostCode(postCode: String?) {
        if (!postCode.isNullOrEmpty() && postCode.length == 4 && !isPostCodeValid(postCode)) {
            personalDetailsFragment.showPostcodeError()
        } else {
            personalDetailsFragment.hidePostcodeError()
        }
    }

    private fun isPostCodeValid(postCode: String?) = POST_CODE_REGEX.matcher(postCode.toString()).matches()

}