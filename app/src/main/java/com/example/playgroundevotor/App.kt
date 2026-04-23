package com.example.playgroundevotor

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.example.playgroundevotor.data.Prefs
import java.util.concurrent.atomic.AtomicBoolean

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) return

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (crashHandled.compareAndSet(false, true)) {
                runCatching {
                    scheduleCrashReport(thread, throwable)
                }
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(10)
        }
    }

    private fun scheduleCrashReport(thread: Thread, throwable: Throwable) {
        val intent = CrashReportActivity.createIntent(
            context = this,
            threadName = thread.name,
            throwable = throwable,
            prefsSnapshot = buildPrefsSnapshot()
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            intent,
            pendingIntentFlags()
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + 200L,
            pendingIntent
        )
    }

    private fun buildPrefsSnapshot(): String {
        val prefs = Prefs(this)
        return buildString {
            appendLine("SharedPreferences(test)")
            appendLine("configuredEmail=${prefs.configuredEmail}")
            appendLine("testEmail=${prefs.testEmail}")
            appendLine("scenarioLabel=${prefs.scenarioLabel}")
            appendLine("includePurchaser=${prefs.includePurchaser}")
            appendLine("includePaymentPurpose=${prefs.includePaymentPurpose}")
            appendLine("scenarioResultsJson=${prefs.scenarioResultsJson.take(MAX_PREF_VALUE_LENGTH)}")
            appendLine("logs=${prefs.logs.take(MAX_PREF_VALUE_LENGTH)}")
        }.trim()
    }

    companion object {
        private const val MAX_PREF_VALUE_LENGTH = 4_000
        private val crashHandled = AtomicBoolean(false)

        private fun pendingIntentFlags(): Int {
            val immutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            return PendingIntent.FLAG_CANCEL_CURRENT or immutable
        }
    }
}

private fun exitProcess(status: Int): Nothing {
    kotlin.system.exitProcess(status)
}
