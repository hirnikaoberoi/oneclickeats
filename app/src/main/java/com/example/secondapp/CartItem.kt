package com.example.secondapp

data class CartItem(
    val itemId: String,
    val itemName: String,
    val price: String,
    val quantity: String,
    val uid:String,
    val restId: String  // ✅ Added restId
)

