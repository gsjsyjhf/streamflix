# StreamFlix ProGuard Rules
# يمنع R8 من حذف كود ضروري للتطبيق

# --- Hilt (Dependency Injection) ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.HiltAndroidApp class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *
-keepnames class * extends androidx.lifecycle.ViewModel
-keepnames class * extends androidx.lifecycle.AndroidViewModel

# --- Retrofit ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking interface retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# --- kotlinx.serialization ---
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.streamflix.app.**$$serializer { *; }
-keepclassmembers class com.streamflix.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.streamflix.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# احفظ كل DTOs (نماذج البيانات)
-keep class com.streamflix.app.data.remote.dto.** { *; }
-keep class com.streamflix.app.domain.model.** { *; }

# --- Coil (Image Loading) ---
-keep class coil.** { *; }
-keep class coil3.** { *; }
-dontwarn coil.**
-dontwarn coil3.**

# --- ExoPlayer / Media3 ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Coroutines ---
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Navigation Compose ---
-keep class androidx.navigation.** { *; }

# --- ViewModels ---
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# --- WorkManager ---
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker
-keep class com.streamflix.app.notifications.NewMoviesWorker { *; }

# --- Keep all app's classes (احتياط) ---
-keep class com.streamflix.app.** { *; }
-keepclassmembers class com.streamflix.app.** { *; }

# --- Suppress warnings ---
-dontwarn org.slf4j.**
-dontwarn org.json.**
-dontwarn java.lang.invoke.**

# --- Keep annotations ---
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault
