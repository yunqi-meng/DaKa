-keep class com.baidu.** { *; }
-keep class vi.com.** { *; }
-keep class com.daka.footprint.data.db.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}