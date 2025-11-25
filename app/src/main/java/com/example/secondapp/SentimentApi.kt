package com.example.secondapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Define request and response models
data class SentimentRequest(val review: String)
data class SentimentResponse(val sentiment: String)

// Retrofit API Interface
interface SentimentApi {
    @POST("/analyze")
    fun analyzeReview(@Body request: SentimentRequest): Call<SentimentResponse>
}