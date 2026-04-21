package com.example.playgroundevotor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.playgroundevotor.services.ReceiptClosedService
import ru.evotor.framework.core.action.event.receipt.receipt_edited.ReceiptClosedEvent

class EventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        try {
            if (
                ReceiptClosedEvent.BROADCAST_ACTION_SELL_RECEIPT_CLOSED == action ||
                ReceiptClosedEvent.BROADCAST_ACTION_PAYBACK_RECEIPT_CLOSED == action ||
                ReceiptClosedEvent.BROADCAST_ACTION_BUY_RECEIPT_CLOSED == action ||
                ReceiptClosedEvent.BROADCAST_ACTION_BUYBACK_RECEIPT_CLOSED == action
            ) {
                ReceiptClosedService.start(context, intent.extras ?: return)
            }
        } catch (ignored: Exception) {
        }
    }
}
