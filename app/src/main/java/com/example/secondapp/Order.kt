package com.example.secondapp

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val restId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending",
    val pickupTime: Timestamp = Timestamp.now(),
    val items: List<OrderItem> = emptyList(),
    val paymentId: String = "",         // Payment ID reference
    val paymentStatus: String = "",     // Payment status
    val transactionId: String = "",     // Transaction ID from UPI
    val paymentMethod: String = "",     // Payment method (upi)
    val upiApp: String = ""             // Which UPI app was used
)