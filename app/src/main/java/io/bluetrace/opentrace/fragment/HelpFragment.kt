package io.bluetrace.opentrace.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.bluetrace.opentrace.R


class HelpFragment : Fragment() {
    private var privateContext: Context? = null
    private lateinit var inflater: LayoutInflater

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        val view = inflater.inflate(R.layout.fragment_help, container, false)


        return view
    }

}
