package com.example.playgroundevotor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.evotor.framework.core.action.event.receipt.receipt_edited.ReceiptClosedEvent

class EventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        try {
            if (ReceiptClosedEvent.BROADCAST_ACTION_SELL_RECEIPT_CLOSED == action) {
                ReceiptClosedService.start(context, intent.extras ?: return)
            }
        } catch (e: Exception) {
            Jenny.e(e)
        }
    }
}