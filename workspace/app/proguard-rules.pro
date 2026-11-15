# Room generates references used at runtime; keep the generated database implementation.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Compose is largely safe to shrink, but keep runtime annotations used by tooling.
-keepattributes *Annotation*
