package au.gov.health.covidsafe.ui.home

import androidx.lifecycle.LifecycleObserver

class HomePresenter(fragment: HomeFragment) : LifecycleObserver {

    init {
        fragment.lifecycle.addObserver(this)
    }
}