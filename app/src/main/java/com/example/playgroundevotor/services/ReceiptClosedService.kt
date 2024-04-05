package com.example.playgroundevotor.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.JobIntentService
import com.example.playgroundevotor.data.Prefs
import com.google.gson.GsonBuilder
import ru.evotor.framework.core.action.event.receipt.receipt_edited.ReceiptClosedEvent
import ru.evotor.framework.receipt.Receipt
import ru.evotor.framework.receipt.ReceiptApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptClosedService : JobIntentService() {
    private val prefs by lazy { Prefs(this) }
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    override fun onHandleWork(intent: Intent) {
        try {
            val iso8601Formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

            val event = ReceiptClosedEvent.create(intent.extras) ?: return


            val receipt: Receipt = ReceiptApi.getReceipt(this, event.receiptUuid) ?: return

            val message = StringBuilder()
            try {
                message.appendLine("\n----------------------------------")
                val date = iso8601Formatter.format(Date())
                message.appendLine("${date}    Чек №${receipt.header.number}")
                message.append(prettyGson.toJson(receipt).toString())

            } catch (e: Exception) {
                message.appendLine("\n----------------ОШИБКА------------------")
                prefs.logs += e.localizedMessage
            }

            prefs.logs += message


        } catch (e: Exception) {
            prefs.logs += e.localizedMessage
        }

    }

    companion object {
        fun start(context: Context, event: Bundle) {
            enqueueWork(context, ReceiptClosedService::class.java, 1, Intent().putExtras(event))
        }
    }
}