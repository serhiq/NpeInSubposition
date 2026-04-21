package com.example.playgroundevotor.services

import com.example.playgroundevotor.data.Prefs
import ru.evotor.framework.receipt.Purchaser
import ru.evotor.framework.receipt.PurchaserType
import ru.evotor.framework.receipt.formation.event.ReturnPurchaserRequisitesForPrintGroupRequestedEvent
import ru.evotor.framework.receipt.formation.event.handler.service.BuyIntegrationService

class TestBuyPurchaserService : BuyIntegrationService() {

    override fun handleEvent(
        event: ReturnPurchaserRequisitesForPrintGroupRequestedEvent
    ): ReturnPurchaserRequisitesForPrintGroupRequestedEvent.Result {
        val prefs = Prefs(this)
        if (!prefs.includePurchaser) {
            return EMPTY_RESULT
        }

        val purchaser = Purchaser(
            name = "OOO Test Pokupatel",
            innNumber = "7701234567",
            birthDate = null,
            documentType = null,
            documentNumber = null,
            type = PurchaserType.LEGAL_ENTITY
        )

        return ReturnPurchaserRequisitesForPrintGroupRequestedEvent.Result(
            event.printGroups.associateWith { purchaser }
        )
    }

    companion object {
        private val EMPTY_RESULT = ReturnPurchaserRequisitesForPrintGroupRequestedEvent.Result(null)
    }
}
