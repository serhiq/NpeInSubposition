package com.example.playgroundevotor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playgroundevotor.databinding.ActivityCrashReportBinding

class CrashReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrashReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrashReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resultTitle.text = getString(R.string.crash_report_title)
        binding.resultMessage.text = buildReport(intent)
        binding.closeButton.setOnClickListener { finishAffinity() }
    }

    private fun buildReport(intent: Intent): String {
        return buildString {
            appendLine("Приложение перехватило необработанное исключение в debug-сборке.")
            appendLine()
            appendLine("Thread: ${intent.getStringExtra(EXTRA_THREAD_NAME).orEmpty()}")
            appendLine("Exception: ${intent.getStringExtra(EXTRA_EXCEPTION_CLASS).orEmpty()}")
            appendLine("Message: ${intent.getStringExtra(EXTRA_EXCEPTION_MESSAGE).orEmpty()}")
            appendLine()
            appendLine("Stacktrace")
            appendLine(intent.getStringExtra(EXTRA_STACKTRACE).orEmpty())
            appendLine()
            appendLine(intent.getStringExtra(EXTRA_PREFS_SNAPSHOT).orEmpty())
        }.trim()
    }

    companion object {
        private const val EXTRA_THREAD_NAME = "thread_name"
        private const val EXTRA_EXCEPTION_CLASS = "exception_class"
        private const val EXTRA_EXCEPTION_MESSAGE = "exception_message"
        private const val EXTRA_STACKTRACE = "stacktrace"
        private const val EXTRA_PREFS_SNAPSHOT = "prefs_snapshot"
        private const val MAX_STACKTRACE_LENGTH = 24_000

        fun createIntent(
            context: Context,
            threadName: String,
            throwable: Throwable,
            prefsSnapshot: String
        ): Intent {
            return Intent(context, CrashReportActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_THREAD_NAME, threadName)
                putExtra(EXTRA_EXCEPTION_CLASS, throwable::class.java.name)
                putExtra(EXTRA_EXCEPTION_MESSAGE, throwable.message.orEmpty())
                putExtra(EXTRA_STACKTRACE, throwable.stackTraceToString().take(MAX_STACKTRACE_LENGTH))
                putExtra(EXTRA_PREFS_SNAPSHOT, prefsSnapshot)
            }
        }
    }
}
