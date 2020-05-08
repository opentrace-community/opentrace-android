package au.gov.health.covidsafe.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment

fun Fragment.navigateTo(actionId: Int, bundle: Bundle? = null, navigatorExtras: Navigator.Extras? = null) = NavHostFragment.findNavController(this).navigate(actionId, bundle, null, navigatorExtras)
