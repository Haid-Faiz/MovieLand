package com.example.movieland

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieland.data.repositories.BaseRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

open class BaseViewModel(private val baseRepo: BaseRepo) : ViewModel() {

    fun saveSessionToken(sessionToken: String) = viewModelScope.launch {
        baseRepo.saveSessionToken(sessionToken)
    }

    fun getSessionToken(): Flow<String?> = baseRepo.getSessionToken()

}