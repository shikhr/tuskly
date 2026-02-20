# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.**

# WorkManager – Glance uses WM internally; InputMerger & workers are instantiated via reflection
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Glance widgets – ActionCallback subclasses are resolved by class name via reflection
-keep class * extends androidx.glance.appwidget.action.ActionCallback { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
