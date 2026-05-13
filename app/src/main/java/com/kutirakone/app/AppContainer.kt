package com.kutirakone.app


import android.content.Context
import com.kutirakone.app.data.repository.AuthRepository
import com.kutirakone.app.data.repository.GeminiRepository
import com.kutirakone.app.data.repository.ScrapRepository
import com.kutirakone.app.data.repository.SwapRepository

/**
 * Simple dependency container — holds all repositories.
 * Accessed via (application as KutiraKoneApp).container
 * This replaces Hilt for now so the app builds without KAPT.
 */
class AppContainer(context: Context) {
    val authRepository  = AuthRepository()
    val scrapRepository = ScrapRepository(context)
    val swapRepository  = SwapRepository()
    val geminiRepository = GeminiRepository(
        apiKey = " AIzaSyCMA5Z-5QC0RZD1kJXoR_Ye0qjI80oxoVY" // Replace with your key
    )
}