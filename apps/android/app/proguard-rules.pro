# NextAI Camera ProGuard / R8 Rules
-keep class com.nextai.camera.** { *; }
-keep class ai.next.technology.** { *; }
-keep class com.nextai.solution.** { *; }
-keep class com.nextai.global.** { *; }

# Keep JNI Bridge classes & native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Retrofit & Gson Models
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
