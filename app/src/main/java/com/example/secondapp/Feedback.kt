package com.example.secondapp

import com.google.firebase.Timestamp

data class Feedback(
    var feedbackId: String = "", // Auto-generated ID
    val orderId: String = "",    // Reference to the order
    val rating: Int = 0,         // Rating (1-5 stars)
    val comment: String = "",    // Optional comment
    val sentiment: String = "",  // Result of sentiment analysis
    val timestamp: Timestamp = Timestamp.now() // When feedback was submitted
)