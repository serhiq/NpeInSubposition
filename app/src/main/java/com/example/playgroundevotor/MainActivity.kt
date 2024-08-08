package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    private val iso8601Formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<Button>(R.id.textView)

        findViewById<Button>(R.id.start).setOnClickListener {
            lifecycleScope.launch {
                runAllRequests()

            }
        }
//
//        findViewById<Button>(R.id.open_chuncker).setOnClickListener {
//            startActivity(Chucker.getLaunchIntent(this))
//        }
    }

    private suspend fun runAllRequests() {

        try {
            val apiUrl = "[enter your server]/queue/"
            val requestBody = (ChangeQueueStateRequest(ChangeQueueStateRequest.RemoteQueue(QueueState.pending)))
            val response = ServerAPI.API.changeQueueState(apiUrl, requestBody)
            displayLogOnTextView("1. response code: ${response.code()}")
        } catch (e: Exception) {
            displayLogOnTextView("1. exception: ${e.localizedMessage}")
        }

        try {
            val apiUrlWithContent = "[enter_your_server]/queue/content"
            val requestBody = ChangeQueueStateRequest(ChangeQueueStateRequest.RemoteQueue(QueueState.pending))
            val response = ServerAPI.API.changeQueueStateWithContent(apiUrlWithContent, requestBody)
            displayLogOnTextView("2. response code: ${response.code()}")
        } catch (e: Exception) {
            displayLogOnTextView("2. exception: ${e.localizedMessage}")
        }

        try {
            val greetUrl = "[enter_your server]/greet"
            val response = ServerAPI.API.greet(greetUrl)
            displayLogOnTextView("3. response code: ${response.code()}")
        } catch (e: Exception) {
            displayLogOnTextView("3. exception: ${e.localizedMessage}")
        }
    }

    private fun displayLogOnTextView(s: String?) {
        s ?: return
        val oldText = textView.text
        val date = iso8601Formatter.format(Date())
        textView.text = "$oldText \n $date  ${s} "
    }
}