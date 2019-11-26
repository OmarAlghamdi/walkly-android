package com.walkly.walkly.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BattlesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is battles Fragment"
    }
    val text: LiveData<String> = _text
}