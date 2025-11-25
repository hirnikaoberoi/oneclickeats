package com.example.secondapp
import com.example.secondapp.ReviewRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/analyze")
    fun analyzeReview(@Body request: ReviewRequest): Call<SentimentResponse>
}
