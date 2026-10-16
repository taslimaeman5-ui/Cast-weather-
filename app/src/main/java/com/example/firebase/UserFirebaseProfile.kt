package com.example.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserFirebaseProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val temperatureUnit: String = "CELSIUS",
    val themeMode: String = "SYSTEM",
    val preferGpsLocation: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
