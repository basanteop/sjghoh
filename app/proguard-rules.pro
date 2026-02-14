# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Gson model classes
-keepattributes Signature
-keepattributes *Annotation*

# Keep ARCore
-keep class com.google.ar.** { *; }

# Keep Sceneform
-keep class com.google.ar.sceneform.** { *; }
-keep class com.google.ar.sceneform.ux.** { *; }

# Keep Room entities
-keep class com.ar.education.progress.** { *; }
-keep class com.ar.education.data.** { *; }
