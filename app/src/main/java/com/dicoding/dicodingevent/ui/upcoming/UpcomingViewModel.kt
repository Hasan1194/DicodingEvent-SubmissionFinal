@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.upcoming

import androidx.lifecycle.ViewModel
import com.dicoding.dicodingevent.data.repo.EventRepository

class UpcomingViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun findUpcomingEvents() = eventRepository.getUpcomingEvents()
    fun searchUpcomingEvents(query: String) = eventRepository.searchEvents(query, isFinished = false)
}
