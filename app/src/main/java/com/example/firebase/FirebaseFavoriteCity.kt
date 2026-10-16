package com.example.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirebaseFavoriteCity(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @ServerTimestamp val addedAt: Timestamp? = null
)
