@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.home

import androidx.lifecycle.ViewModel
import com.dicoding.dicodingevent.data.repo.EventRepository

class HomeViewModel(private val repository: EventRepository) : ViewModel() {

    fun getFinishedEvents() = repository.getFinishedEvents()
    fun getUpcomingEvents() = repository.getUpcomingEvents()
    fun searchUpcomingEvents(query: String) = repository.searchEvents(query, isFinished = false)
    fun searchFinishedEvents(query: String) = repository.searchEvents(query, isFinished = true)
}
