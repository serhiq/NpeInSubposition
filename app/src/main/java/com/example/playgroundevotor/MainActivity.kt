package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.receipt.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    private val iso8601Formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<Button>(R.id.textView)

        findViewById<Button>(R.id.createPosition).setOnClickListener {
            createPositionFree()
       }
    }

    private fun createPositionFree() {
        val changes = positionAdds()
        OpenSellReceiptCommand(changes, null, null).process(this, IntegrationManagerCallback { integrationManagerFuture ->
            try {
                val result = integrationManagerFuture.result
                if (IntegrationManagerFuture.Result.Type.ERROR == result.type) {
                    displayLogOnTextView("Ошибка формирования чека: " + result.error.message + ". Код: " + result.error.code)
                    return@IntegrationManagerCallback
                }

                if (true) {
                    val intent = NavigationApi.createIntentForSellReceiptPayment()
                    startActivityForResult(intent, 1)
                } else {
                    startActivity(NavigationApi.createIntentForSellReceiptEdit())
                }

            } catch (e: Exception) {
                displayLogOnTextView("Exception: ${e.localizedMessage}")
                Jenny.e(e)
            }
        })
    }

    private fun positionAdds(): List<PositionAdd> {
        val position = Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Позиция по свободной цене",
            Measure(
                "шт",
                3,
                0
            ),
            BigDecimal.TEN,
            BigDecimal.ONE
        ).build()
        return listOf(PositionAdd(position))
    }

    private fun displayLogOnTextView(s: String?) {
        s ?: return
        val oldText = textView.text
        val date = iso8601Formatter.format(Date())
        textView.text = "$oldText \n $date  ${s} "
    }
}