@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.favorite

import androidx.lifecycle.ViewModel
import com.dicoding.dicodingevent.data.repo.EventRepository

class FavoriteViewModel(private val repository: EventRepository) : ViewModel() {

    fun findFavoriteEvent() = repository.getFavoriteEvents()

}

