package com.kutirakone.app.ui.screens.auth
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.repository.AuthResult
import com.kutirakone.app.ui.theme.Green50
import com.kutirakone.app.ui.theme.Green600

// Language strings map
val langStrings = mapOf(
    "English" to mapOf(
        "title"        to "Kutira-Kone",
        "tagline"      to "Zero-waste fabric exchange\nfor your community",
        "selectLang"   to "Select language",
        "mobileNum"    to "Mobile number",
        "mobilePlaceholder" to "+91 98765 43210",
        "enterOtp"     to "Enter OTP",
        "otpPlaceholder" to "6-digit OTP",
        "sendOtp"      to "Send OTP",
        "verifyLogin"  to "Verify & Login",
        "browseGuest"  to "Browse as Guest"
    ),
    "ಕನ್ನಡ" to mapOf(
        "title"        to "ಕುಟಿರ-ಕೋಣೆ",
        "tagline"      to "ನಿಮ್ಮ ಸಮುದಾಯಕ್ಕಾಗಿ ಬಟ್ಟೆ ತ್ಯಾಜ್ಯ ವಿನಿಮಯ",
        "selectLang"   to "ಭಾಷೆ ಆಯ್ಕೆ ಮಾಡಿ",
        "mobileNum"    to "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ",
        "mobilePlaceholder" to "+91 98765 43210",
        "enterOtp"     to "OTP ನಮೂದಿಸಿ",
        "otpPlaceholder" to "6-ಅಂಕಿ OTP",
        "sendOtp"      to "OTP ಕಳುಹಿಸಿ",
        "verifyLogin"  to "ಪರಿಶೀಲಿಸಿ & ಲಾಗಿನ್",
        "browseGuest"  to "ಅತಿಥಿಯಾಗಿ ಬ್ರೌಸ್ ಮಾಡಿ"
    ),
    "हिन्दी" to mapOf(
        "title"        to "कुटिर-कोने",
        "tagline"      to "आपके समुदाय के लिए कपड़ा विनिमय",
        "selectLang"   to "भाषा चुनें",
        "mobileNum"    to "मोबाइल नंबर",
        "mobilePlaceholder" to "+91 98765 43210",
        "enterOtp"     to "OTP दर्ज करें",
        "otpPlaceholder" to "6-अंक OTP",
        "sendOtp"      to "OTP भेजें",
        "verifyLogin"  to "सत्यापित करें & लॉगिन",
        "browseGuest"  to "अतिथि के रूप में ब्राउज़ करें"
    )
)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context   = LocalContext.current
    val activity  = context as Activity
    val container = (context.applicationContext as KutiraKoneApp).container

    var phone        by remember { mutableStateOf("+91 ") }
    var otp          by remember { mutableStateOf("") }
    var otpSent      by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("English") }

    // Get strings for current language
    val s = langStrings[selectedLang] ?: langStrings["English"]!!



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text("🧵", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))

        Text(s["title"]!!, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, color = Green600)
        Text(s["tagline"]!!, fontSize = 14.sp,
            color = Color.Gray, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp))

        Spacer(Modifier.height(32.dp))

        // Language selector
        Text(s["selectLang"]!!, fontSize = 12.sp, color = Color.Gray,
            modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("English", "ಕನ್ನಡ", "हिन्दी").forEach { lang ->
                val selected = selectedLang == lang
                OutlinedButton(
                    onClick = { selectedLang = lang },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) Green50 else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, if (selected) Green600 else Color.LightGray
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(lang, fontSize = 12.sp,
                        color = if (selected) Green600 else Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Phone input
        Text(s["mobileNum"]!!, fontSize = 12.sp, color = Color.Gray,
            modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(s["mobilePlaceholder"]!!) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            enabled = !otpSent
        )

        if (otpSent) {
            Spacer(Modifier.height(16.dp))
            Text(s["enterOtp"]!!, fontSize = 12.sp, color = Color.Gray,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s["otpPlaceholder"]!!) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = Color.Red, fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                errorMsg = ""
                if (!otpSent) {
                    container.authRepository.sendOtp(phone.trim(), activity) { result ->
                        when (result) {
                            is AuthResult.Loading -> isLoading = true
                            is AuthResult.OtpSent -> { isLoading = false; otpSent = true }
                            is AuthResult.Error   -> { isLoading = false; errorMsg = result.message }
                            else -> {}
                        }
                    }
                } else {
                    container.authRepository.verifyOtp(otp.trim()) { result ->
                        when (result) {
                            is AuthResult.Loading -> isLoading = true
                            is AuthResult.Success -> { isLoading = false; onLoginSuccess() }
                            is AuthResult.Error   -> { isLoading = false; errorMsg = result.message }
                            else -> {}
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White,
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (!otpSent) s["sendOtp"]!! else s["verifyLogin"]!!,
                    fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Green600)
        ) {
            Text(s["browseGuest"]!!, color = Green600,
                fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}





