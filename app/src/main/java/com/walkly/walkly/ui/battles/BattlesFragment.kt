package com.walkly.walkly.ui.battles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.walkly.walkly.R

class BattlesFragment : Fragment() {

    private lateinit var battlesViewModel: BattlesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        battlesViewModel =
            ViewModelProviders.of(this).get(BattlesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_battles, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        battlesViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}