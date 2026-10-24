# Supabase / ktor
-keepattributes Signature,*Annotation*
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-keep class io.github.jan_tenner.supabase.** { *; }

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class org.udmurtlang.learn.** {
    *** Companion;
}
-keepclasseswithmembers class org.udmurtlang.learn.** {
    kotlinx.serialization.KSerializer serializer(...);
}
