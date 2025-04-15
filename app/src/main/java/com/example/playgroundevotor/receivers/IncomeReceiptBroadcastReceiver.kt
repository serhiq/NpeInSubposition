package com.example.playgroundevotor.receivers

import android.content.Context
import android.util.Log
import com.example.playgroundevotor.data.Prefs
import com.google.gson.GsonBuilder
import ru.evotor.framework.receipt.ReceiptApi
import ru.evotor.framework.receipt.event.ReceiptCompletedEvent
import ru.evotor.framework.receipt.event.ReceiptCreatedEvent
import ru.evotor.framework.receipt.event.handler.receiver.CorrectionIncomeReceiptBroadcastReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncomeReceiptBroadcastReceiver : CorrectionIncomeReceiptBroadcastReceiver() {
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    override fun handleReceiptCreatedEvent(context: Context, event: ReceiptCreatedEvent) {
        Log.w(context.packageName, "Got ReceiptCreatedEvent")
    }

    override fun handleReceiptCompletedEvent(context: Context, event: ReceiptCompletedEvent) {
        val prefs by lazy { Prefs(context) }
        try {
            val iso8601Formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

            val receipt = ReceiptApi.getReceipt(context,  event.receiptUuid) ?: return

            val message = StringBuilder()
            try {
                message.appendLine("\n----------------------------------")
                message.appendLine("CorrectionIncomeReceiptBroadcastReceiver")
                val date = iso8601Formatter.format(Date())
                message.appendLine("${date}    Чек №${receipt.header.number}")
                message.append(prettyGson.toJson(receipt).toString())

                val fiscalReceipt = try {
                    ReceiptApi.getFiscalReceipts(context, event.receiptUuid)?.toList()
                } catch (e: Exception) {
                    message.appendLine("\n----------------ОШИБКА getFiscalReceipts------------------")
                    prefs.logs += e.localizedMessage
                }

                message.appendLine("fiscalReceipt:")
                message.appendLine(prettyGson.toJson(fiscalReceipt).toString())



            } catch (e: Exception) {
                message.appendLine("\n----------------ОШИБКА------ReceiptApi.getFiscalReceipts(------------")
                prefs.logs += e.localizedMessage
            }

            prefs.logs += message



        } catch (e: Exception) {
            prefs.logs += "\n----------------ОШИБКА handleReceiptCompletedEvent------------------"
            prefs.logs += e.localizedMessage
        }
    }
}