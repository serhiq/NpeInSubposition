package com.example.playgroundevotor.services

import com.example.playgroundevotor.data.Prefs
import ru.evotor.framework.core.IntegrationService
import ru.evotor.framework.core.action.event.receipt.changes.position.SetPrintGroup
import ru.evotor.framework.core.action.event.receipt.print_group.PrintGroupRequiredEvent
import ru.evotor.framework.core.action.event.receipt.print_group.PrintGroupRequiredEventProcessor
import ru.evotor.framework.core.action.event.receipt.print_group.PrintGroupRequiredEventResult
import ru.evotor.framework.core.action.processor.ActionProcessor
import ru.evotor.framework.receipt.PrintGroup
import ru.evotor.framework.receipt.Receipt
import ru.evotor.framework.receipt.ReceiptApi
import java.util.UUID

class TestBuyPrintGroupService : IntegrationService() {

    private val processor = object : PrintGroupRequiredEventProcessor() {
        override fun call(action: String, event: PrintGroupRequiredEvent, callback: Callback) {
            val prefs = Prefs(applicationContext)
            if (!prefs.includePaymentPurpose) {
                callback.skip()
                return
            }

            val receipt = ReceiptApi.getReceipt(applicationContext, Receipt.Type.BUY)
            if (receipt == null) {
                callback.onError(-1, "BUY receipt not found for print-group processing.")
                return
            }

            val purposeIds = receipt.getPayments().mapNotNull { it.purposeIdentifier }
            val printReceipts = arrayListOf(
                SetPrintGroup(
                    printGroup = PrintGroup(
                        UUID.randomUUID().toString(),
                        PrintGroup.Type.CASH_RECEIPT,
                        "Test Purpose Group",
                        null,
                        null,
                        null,
                        true
                    ),
                    paymentPurposeIds = purposeIds,
                    positionUuids = receipt.getPositions().map { it.uuid }
                )
            )
            callback.onResult(PrintGroupRequiredEventResult(null, printReceipts))
        }
    }

    override fun createProcessors(): Map<String, ActionProcessor> {
        return hashMapOf(
            PrintGroupRequiredEvent.NAME_BUY_RECEIPT to processor
        )
    }
}
