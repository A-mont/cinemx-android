# --- kotlinx.serialization ---------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# DTOs y rutas @Serializable de la app
-keep,includedescriptorclasses class com.cinemx.movies.**$$serializer { *; }
-keepclassmembers class com.cinemx.movies.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit / OkHttp ------------------------------------------------------
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# --- Ktor (usado internamente por supabase-kt) -------------------------------
-dontwarn io.ktor.**
-keep class io.ktor.client.engine.okhttp.** { *; }

# --- Supabase ---------------------------------------------------------------
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# --- Credential Manager / Google ID -----------------------------------------
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# --- Navigation Compose type-safe -------------------------------------------
-keep class com.cinemx.movies.navigation.** { *; }
