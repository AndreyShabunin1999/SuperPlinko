package com.dial.superplinko.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dial.superplinko.model.InfoUrl
import com.dial.superplinko.repository.InfoUrlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoUrlViewModel(private val infoUrlRepository: InfoUrlRepository, private val phoneName: String,
                       private val locale: String, private val unique: String): ViewModel() {
    init {
        viewModelScope.launch (Dispatchers.IO){
            infoUrlRepository.getInfoUrl(phoneName, locale, unique)
        }
    }

    val info: LiveData<InfoUrl>
        get() = infoUrlRepository.urlInfo
}