@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.finished

import androidx.lifecycle.ViewModel
import com.dicoding.dicodingevent.data.repo.EventRepository

class FinishedViewModel(private val repository: EventRepository) : ViewModel() {

    fun findFinishedEvent() = repository.getFinishedEvents()
    fun searchFinishedEvents(query: String) = repository.searchEvents(query, isFinished = true)

}

