package com.x.a_technologies.dictionary.retrofit

import com.x.a_technologies.dictionary.models.MyWord
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun getMyWord(@Path("word") word:String): List<MyWord>
}