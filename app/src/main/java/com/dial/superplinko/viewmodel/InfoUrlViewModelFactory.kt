package com.dial.superplinko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dial.superplinko.repository.InfoUrlRepository

class InfoUrlViewModelFactory(private val infoUrlRepository: InfoUrlRepository, private val phoneName: String,
                              private val locale: String, private val unique: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InfoUrlViewModel(infoUrlRepository, phoneName, locale, unique) as T
    }
}