package com.kutirakone.app.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.data.model.KutiraUser
import com.kutirakone.app.data.model.Language
import com.kutirakone.app.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    object Loading          : AuthResult()
    object OtpSent          : AuthResult()
    object Success          : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var verificationId: String = ""

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean        get() = auth.currentUser != null

    // ── Send OTP ──────────────────────────────────────────────────
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onResult: (AuthResult) -> Unit
    ) {
        onResult(AuthResult.Loading)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification on some devices
                    signInWithCredential(credential, onResult)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onResult(AuthResult.Error(e.message ?: "OTP failed. Check your number."))
                }

                override fun onCodeSent(
                    vId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = vId
                    onResult(AuthResult.OtpSent)
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ── Verify OTP ────────────────────────────────────────────────
    fun verifyOtp(otp: String, onResult: (AuthResult) -> Unit) {
        if (verificationId.isEmpty()) {
            onResult(AuthResult.Error("Please request OTP first"))
            return
        }
        onResult(AuthResult.Loading)
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential, onResult)
    }

    // ── Sign in with credential ───────────────────────────────────
    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onResult: (AuthResult) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener
                // Save user to Firestore if first time
                saveUserIfNew(user)
                onResult(AuthResult.Success)
            }
            .addOnFailureListener { e ->
                onResult(AuthResult.Error(e.message ?: "Verification failed"))
            }
    }

    // ── Save new user to Firestore ────────────────────────────────
    private fun saveUserIfNew(firebaseUser: FirebaseUser) {
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val newUser = KutiraUser(
                    userId    = firebaseUser.uid,
                    phone     = firebaseUser.phoneNumber ?: "",
                    name      = "",
                    role      = UserRole.BOTH,
                    preferredLanguage = Language.ENGLISH
                )
                userRef.set(newUser)
            }
        }
    }

    // ── Sign out ──────────────────────────────────────────────────
    fun signOut() = auth.signOut()

    // ── Get current user profile ──────────────────────────────────
    suspend fun getUserProfile(userId: String): KutiraUser? {
        return try {
            firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(KutiraUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ── Update user profile ───────────────────────────────────────
    suspend fun updateProfile(userId: String, name: String, role: UserRole): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update(mapOf("name" to name, "role" to role.name))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}