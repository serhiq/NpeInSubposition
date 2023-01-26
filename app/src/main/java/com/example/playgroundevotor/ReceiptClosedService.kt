package com.example.playgroundevotor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.JobIntentService
import ru.evotor.framework.core.action.event.receipt.receipt_edited.ReceiptClosedEvent
import ru.evotor.framework.receipt.ReceiptApi

class ReceiptClosedService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        try {
            val event = ReceiptClosedEvent.create(intent.extras) ?: return
            val receipt = ReceiptApi.getReceipt(this, event.receiptUuid) ?: "Чек отсутсвует"

            Jenny.vvv(receipt)
        } catch (e: Exception) {
            Jenny.v(e.localizedMessage)
        }
    }

    companion object {
        fun start(context: Context, event: Bundle) {
            enqueueWork(context, ReceiptClosedService::class.java, 1, Intent().putExtras(event))
        }
    }
}