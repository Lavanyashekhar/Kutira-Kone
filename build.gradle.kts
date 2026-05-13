
// Root build.gradle.kts
// Use explicit id+version instead of alias() to avoid "plugin already on classpath" errors
plugins {
    id("com.android.application")        version "8.5.2"  apply false
    id("org.jetbrains.kotlin.android")   version "1.9.24" apply false
    id("com.google.gms.google-services") version "4.4.2"  apply false
}