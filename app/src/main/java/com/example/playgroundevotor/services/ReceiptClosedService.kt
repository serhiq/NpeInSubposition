package com.example.playgroundevotor.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.JobIntentService
import com.example.playgroundevotor.data.Prefs
import com.google.gson.GsonBuilder
import org.json.JSONObject
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
            updateShortScenarioResult(receipt)

            val message = StringBuilder()
            try {
                val header = receipt.header
                message.appendLine("\n----------------------------------")
                val date = iso8601Formatter.format(Date())
                message.appendLine("${date}    Чек №${receipt.header.number}")
                message.appendLine("Readback: ReceiptApi.getReceipt(...).header.receiptFromInternet")
                message.appendLine("Ожидаемое поведение: receiptFromInternet = true")
                message.appendLine("Фактическое поведение: receiptFromInternet = ${header.receiptFromInternet}")
                message.appendLine("paymentPlace = ${header.paymentPlace}")
                message.appendLine("paymentAddress = ${header.paymentAddress}")
                message.appendLine()
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

    private fun updateShortScenarioResult(receipt: Receipt) {
        // Проверка результата deprecated-сценария выполняется здесь.
        // Ожидаемое поведение: receiptFromInternet = true
        // Фактическое поведение: (оставить место для заполнения)
        val header = receipt.header
        val extraJson = runCatching { JSONObject(header.extra ?: "{}") }.getOrElse { JSONObject() }
        val scenarioCode = extraJson.optString("scenario")
        if (scenarioCode.isBlank()) return

        val json = runCatching {
            JSONObject(prefs.scenarioResultsJson.ifBlank { "{}" })
        }.getOrElse { JSONObject() }

        val expectedType = extraJson.optString("expectedType")
        val expectedPaymentPlace = extraJson.optString("expectedPaymentPlace")
        val expectedPaymentAddress = extraJson.optString("expectedPaymentAddress")
        val expectedInternet = extraJson.optBoolean("expectedInternet", true)
        val checkPaymentAddress = extraJson.optBoolean("checkPaymentAddress", false)
        val checks = mutableListOf<Pair<String, Boolean>>()
        checks += "readback" to true
        checks += "type" to (header.type.name == expectedType)
        checks += "internet" to (header.receiptFromInternet == expectedInternet)
        checks += "paymentPlace" to (header.paymentPlace == expectedPaymentPlace)
        if (checkPaymentAddress) {
            checks += "paymentAddress" to (header.paymentAddress == expectedPaymentAddress)
        }
        checks += "number" to !header.number.isNullOrBlank()

        val isPass = checks.all { it.second }
        val details = buildString {
            appendLine(if (isPass) "PASS" else "FAIL")
            appendLine()
            appendLine("Ожидаемое поведение: receiptFromInternet = true")
            appendLine("Фактическое поведение: receiptFromInternet = ${header.receiptFromInternet}")
            appendLine(if (header.type.name == expectedType) "type=${header.type.name}" else "!!!type=${header.type.name}")
            appendLine(if (header.receiptFromInternet == expectedInternet) "internet=${header.receiptFromInternet}" else "!!!internet=${header.receiptFromInternet}")
            appendLine(if (header.paymentPlace == expectedPaymentPlace) "paymentPlace=${header.paymentPlace}" else "!!!paymentPlace=${header.paymentPlace}")
            if (checkPaymentAddress) {
                appendLine(
                    if (header.paymentAddress == expectedPaymentAddress) {
                        "paymentAddress=${header.paymentAddress}"
                    } else {
                        "!!!paymentAddress=${header.paymentAddress}"
                    }
                )
            }
            appendLine(if (!header.number.isNullOrBlank()) "number=${header.number}" else "!!!number=null")
            appendLine()
        }

        json.put(scenarioCode, details)
        prefs.scenarioResultsJson = json.toString()
    }
}
