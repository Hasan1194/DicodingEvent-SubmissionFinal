@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.data.remote.retrofit

import com.dicoding.dicodingevent.data.remote.response.DetailResponse
import com.dicoding.dicodingevent.data.remote.response.EventResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("events?active=1")
    suspend fun getUpcomingEvent(): EventResponse

    @GET("events?active=0")
    suspend fun getFinishedEvent(): EventResponse

    @GET("events")
    suspend fun getEvent(): EventResponse

    @GET("events/{id}")
    suspend fun getDetailEvent(@Path("id") eventId: String): DetailResponse

    @GET("events")
    suspend fun searchFinishedEvents(
        @Query("q") query: String,
        @Query("active") active: Int = 0
    ): EventResponse

    @GET("events")
    suspend fun searchUpcomingEvents(
        @Query("q") query: String,
        @Query("active") active: Int = 1
    ): EventResponse
}
