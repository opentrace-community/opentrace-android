package au.gov.health.covidsafe

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object Preference {
    private const val PREF_ID = "Tracer_pref"
    private const val IS_ONBOARDED = "IS_ONBOARDED"
    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val HANDSHAKE_PIN = "HANDSHAKE_PIN"
    private const val DEVICE_ID = "DEVICE_ID"
    private const val JWT_TOKEN = "JWT_TOKEN"
    private const val IS_DATA_UPLOADED = "IS_DATA_UPLOADED"
    private const val DATA_UPLOADED_DATE_MS = "DATA_UPLOADED_DATE_MS"
    private const val UPLOADED_MORE_THAN_24_HRS = "UPLOADED_MORE_THAN_24_HRS"

    private const val NEXT_FETCH_TIME = "NEXT_FETCH_TIME"
    private const val EXPIRY_TIME = "EXPIRY_TIME"
    private const val NAME = "NAME"
    private const val IS_MINOR = "IS_MINOR"
    private const val POST_CODE = "POST_CODE"
    private const val AGE = "AGE"

    fun putDeviceID(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(DEVICE_ID, value)?.apply()
    }

    fun getDeviceID(context: Context?): String {
        return context?.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                ?.getString(DEVICE_ID, "") ?: ""
    }

    fun putEncrypterJWTToken(context: Context?, jwtToken: String?) {
        context?.let {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                    PREF_ID,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).edit()?.putString(JWT_TOKEN, jwtToken)?.apply()
        }
    }

    fun getEncrypterJWTToken(context: Context?): String? {
        return context?.let {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                    PREF_ID,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString(JWT_TOKEN, null)
        }
    }

    fun putHandShakePin(context: Context?, value: String?) {
        context?.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                ?.edit()?.putString(HANDSHAKE_PIN, value)?.apply()
    }

    fun putIsOnBoarded(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putBoolean(IS_ONBOARDED, value).apply()
    }

    fun isOnBoarded(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getBoolean(IS_ONBOARDED, false)
    }

    fun putPhoneNumber(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(PHONE_NUMBER, value).apply()
    }

    fun putNextFetchTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(NEXT_FETCH_TIME, time).apply()
    }

    fun getNextFetchTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        NEXT_FETCH_TIME, 0
                )
    }

    fun putExpiryTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(EXPIRY_TIME, time).apply()
    }

    fun getExpiryTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        EXPIRY_TIME, 0
                )
    }

    fun isDataUploaded(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).getBoolean(IS_DATA_UPLOADED, false)
    }

    fun setDataIsUploaded(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).edit().also { editor ->
            editor.putBoolean(IS_DATA_UPLOADED, value)
            if (value) {
                editor.putLong(DATA_UPLOADED_DATE_MS, System.currentTimeMillis())
            } else {
                editor.remove(DATA_UPLOADED_DATE_MS)
            }
        }.apply()
    }

    fun getDataUploadedDateMs(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).getLong(DATA_UPLOADED_DATE_MS, System.currentTimeMillis())
    }

    fun putName(context: Context, name: String): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(NAME, name).commit()
    }

    fun getName(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).getString(NAME, null)
    }

    fun putIsMinor(context: Context, minor: Boolean): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putBoolean(IS_MINOR, minor).commit()
    }

    fun isMinor(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).getBoolean(IS_MINOR, false)
    }

    fun putPostCode(context: Context, state: String): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(POST_CODE, state).commit()
    }

    fun getPostCode(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(POST_CODE, null)
    }

    fun putAge(context: Context, age: String): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(AGE, age).commit()
    }

    fun getAge(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(AGE, null)
    }

}
