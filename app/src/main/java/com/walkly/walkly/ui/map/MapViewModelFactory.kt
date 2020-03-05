package com.walkly.walkly.ui.map


import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class MapViewModelFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(activity) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}
