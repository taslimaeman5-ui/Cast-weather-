package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreWeatherRepo"

class FirestoreWeatherRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor that resolves the named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid ?: error("User must be authenticated for Firestore operations")
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    /**
     * Observes real-time favorite cities for the currently authenticated user
     */
    fun observeFavoriteCities(): Flow<List<FirebaseFavoriteCity>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionPath = "users/$uid/favorite_cities"
        val collectionRef = db.collection("users")
            .document(uid)
            .collection("favorite_cities")

        val registration: ListenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, collectionPath)
                Log.w(TAG, "Listen failed for favorite cities", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val cities = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirebaseFavoriteCity::class.java)
                }
                trySend(cities)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Adds or updates a favorite city in the cloud
     */
    suspend fun addFavoriteCity(
        cityId: String,
        name: String,
        country: String,
        latitude: Double,
        longitude: Double
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users")
            .document(uid)
            .collection("favorite_cities")
            .document(cityId)

        val existing = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }

        val addedAtVal = if (existing != null && existing.exists() && existing.getTimestamp("addedAt") != null) {
            existing.getTimestamp("addedAt")!!
        } else {
            FieldValue.serverTimestamp()
        }

        val cityData = hashMapOf(
            "userId" to uid,
            "name" to name,
            "country" to country,
            "latitude" to latitude,
            "longitude" to longitude,
            "addedAt" to addedAtVal
        )

        try {
            docRef.set(cityData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    /**
     * Removes a favorite city from the cloud
     */
    suspend fun removeFavoriteCity(cityId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users")
            .document(uid)
            .collection("favorite_cities")
            .document(cityId)

        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    /**
     * Syncs user preferences to the user profile document in Firestore
     */
    suspend fun syncUserProfile(
        displayName: String,
        email: String,
        temperatureUnit: String,
        themeMode: String,
        preferGps: Boolean
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)

        val existing = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }

        val createdAtVal = if (existing != null && existing.exists() && existing.getTimestamp("createdAt") != null) {
            existing.getTimestamp("createdAt")!!
        } else {
            FieldValue.serverTimestamp()
        }

        val profileData = hashMapOf(
            "userId" to uid,
            "displayName" to displayName,
            "email" to email,
            "temperatureUnit" to temperatureUnit,
            "themeMode" to themeMode,
            "preferGpsLocation" to preferGps,
            "createdAt" to createdAtVal,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(profileData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    /**
     * Retrieves the stored user profile from Firestore
     */
    suspend fun getUserProfile(): UserFirebaseProfile? {
        val uid = currentUserId ?: return null
        val docRef = db.collection("users").document(uid)
        return try {
            val doc = docRef.get().await()
            doc.toObject(UserFirebaseProfile::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }
}
