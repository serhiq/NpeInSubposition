package com.example.playgroundevotor

import org.junit.Assert.*
import org.junit.Test
import ru.evotor.framework.kkt.FfdVersion
import ru.evotor.framework.receipt.DocumentType
import ru.evotor.framework.receipt.Purchaser
import ru.evotor.framework.receipt.PurchaserType
import java.util.Date

class PurchaserActivityKtTestExampleUnitTest {

    fun adaptToFFD105Version(purchaser: Purchaser?, ffdVersion: FfdVersion?): Purchaser? {
        if (ffdVersion != FfdVersion.V_1_0_5) {
            return purchaser
        }

        if (purchaser?.type == PurchaserType.NATURAL_PERSON && purchaser.innNumber?.isNotBlank() == true) {
            return Purchaser(name = purchaser.name, innNumber = null, birthDate = null, documentType = null, documentNumber = purchaser.innNumber, type = purchaser.type)
        }

        return purchaser
    }

}