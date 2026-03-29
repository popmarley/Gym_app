package com.example.gym_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkoutTimerSnapshot(
    val currentDayId: String? = null,
    val currentDayTitle: String = "",
    val isRunning: Boolean = false,
    val startedAtElapsedMs: Long? = null,
    val accumulatedElapsedMs: Long = 0L
) {
    fun elapsedMs(nowElapsedMs: Long = SystemClock.elapsedRealtime()): Long {
        val runningElapsed = if (isRunning && startedAtElapsedMs != null) {
            (nowElapsedMs - startedAtElapsedMs).coerceAtLeast(0L)
        } else {
            0L
        }
        return (accumulatedElapsedMs + runningElapsed).coerceAtLeast(0L)
    }

    fun hasSession(): Boolean = currentDayId != null && elapsedMs() > 0L || (isRunning && currentDayId != null)

    fun belongsTo(dayId: String): Boolean = currentDayId == dayId
}

object WorkoutTimerController {
    private const val PREFS_NAME = "workout_timer_store"
    private const val KEY_DAY_ID = "day_id"
    private const val KEY_DAY_TITLE = "day_title"
    private const val KEY_RUNNING = "running"
    private const val KEY_STARTED_AT = "started_at_elapsed_ms"
    private const val KEY_ACCUMULATED = "accumulated_elapsed_ms"

    private val mutableState = MutableStateFlow(WorkoutTimerSnapshot())
    val state: StateFlow<WorkoutTimerSnapshot> = mutableState.asStateFlow()

    fun sync(context: Context) {
        mutableState.value = readSnapshot(context)
    }

    fun start(context: Context, dayId: String, dayTitle: String) {
        val intent = Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_START
            putExtra(WorkoutTimerService.EXTRA_DAY_ID, dayId)
            putExtra(WorkoutTimerService.EXTRA_DAY_TITLE, dayTitle)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pause(context: Context) {
        context.startService(Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_PAUSE
        })
    }

    fun reset(context: Context) {
        context.startService(Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_RESET
        })
    }

    internal fun readSnapshot(context: Context): WorkoutTimerSnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WorkoutTimerSnapshot(
            currentDayId = prefs.getString(KEY_DAY_ID, null),
            currentDayTitle = prefs.getString(KEY_DAY_TITLE, "").orEmpty(),
            isRunning = prefs.getBoolean(KEY_RUNNING, false),
            startedAtElapsedMs = prefs.getLong(KEY_STARTED_AT, -1L).takeIf { it >= 0L },
            accumulatedElapsedMs = prefs.getLong(KEY_ACCUMULATED, 0L)
        )
    }

    internal fun writeSnapshot(context: Context, snapshot: WorkoutTimerSnapshot) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DAY_ID, snapshot.currentDayId)
            .putString(KEY_DAY_TITLE, snapshot.currentDayTitle)
            .putBoolean(KEY_RUNNING, snapshot.isRunning)
            .putLong(KEY_STARTED_AT, snapshot.startedAtElapsedMs ?: -1L)
            .putLong(KEY_ACCUMULATED, snapshot.accumulatedElapsedMs)
            .apply()
        mutableState.value = snapshot
    }

    internal fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        mutableState.value = WorkoutTimerSnapshot()
    }
}

class WorkoutTimerService : Service() {
    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        WorkoutTimerController.sync(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESET -> handleReset()
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val dayId = intent.getStringExtra(EXTRA_DAY_ID) ?: return
        val dayTitle = intent.getStringExtra(EXTRA_DAY_TITLE).orEmpty().ifBlank { "Antrenman" }
        val now = SystemClock.elapsedRealtime()
        val current = WorkoutTimerController.readSnapshot(this)
        val carryElapsed = if (current.currentDayId == dayId) current.elapsedMs(now) else 0L
        val snapshot = WorkoutTimerSnapshot(
            currentDayId = dayId,
            currentDayTitle = dayTitle,
            isRunning = true,
            startedAtElapsedMs = now,
            accumulatedElapsedMs = carryElapsed
        )
        WorkoutTimerController.writeSnapshot(this, snapshot)
        startForegroundWithNotification(snapshot)
    }

    private fun handlePause() {
        val current = WorkoutTimerController.readSnapshot(this)
        if (!current.isRunning) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val snapshot = current.copy(
            isRunning = false,
            startedAtElapsedMs = null,
            accumulatedElapsedMs = current.elapsedMs()
        )
        WorkoutTimerController.writeSnapshot(this, snapshot)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleReset() {
        WorkoutTimerController.clear(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundWithNotification(snapshot: WorkoutTimerSnapshot) {
        val notification = buildNotification(snapshot)
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
        }
    }

    private fun buildNotification(snapshot: WorkoutTimerSnapshot): android.app.Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val startedWallClock = System.currentTimeMillis() - snapshot.elapsedMs()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("${snapshot.currentDayTitle} sayacı")
            .setContentText("Antrenman süresi arka planda çalışıyor")
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setWhen(startedWallClock)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = NotificationManagerCompat.from(this)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Antrenman sayacı",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Arka planda çalışan antrenman sayacını gösterir."
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.gym_app.action.START_WORKOUT_TIMER"
        const val ACTION_PAUSE = "com.example.gym_app.action.PAUSE_WORKOUT_TIMER"
        const val ACTION_RESET = "com.example.gym_app.action.RESET_WORKOUT_TIMER"
        const val EXTRA_DAY_ID = "extra_day_id"
        const val EXTRA_DAY_TITLE = "extra_day_title"

        private const val CHANNEL_ID = "workout_timer_channel"
        private const val NOTIFICATION_ID = 2006
    }
}
