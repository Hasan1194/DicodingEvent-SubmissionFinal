@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.repo.EventRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: EventRepository) : ViewModel() {

    fun getDetailEvent(query: String) = repository.getDetailEvent(query)
    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.setBookmarkedEvent(event, true)
        }
    }

    fun getBookmarkedNews() = repository.getFavoriteEvents()
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.setBookmarkedEvent(event, false)
        }
    }


}