package com.grupo18.twister.core.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.viewmodel.TwistViewModel

class TwistViewModelFactory(
    private val myApp: MyApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TwistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TwistViewModel(myApp) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
