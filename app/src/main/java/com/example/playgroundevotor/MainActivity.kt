package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playgroundevotor.databinding.ActivityMainBinding
import com.example.playgroundevotor.data.Prefs
import ru.evotor.framework.component.PaymentPerformer
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.print_receipt_command.PrintCorrectionIncomeReceiptCommand
import ru.evotor.framework.payment.PaymentSystem
import ru.evotor.framework.payment.PaymentType
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Payment
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.PrintGroup
import ru.evotor.framework.receipt.Receipt
import ru.evotor.framework.receipt.correction.CorrectionType
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { Prefs(applicationContext) }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Тестовое приложение для Evotor"

        binding.printReceiptButton.setOnClickListener {
            printCorrectionIncome(generateReceipt())
        }
    }

    private fun generateReceipt(): List<Receipt.PrintReceipt> {
        val printGroup = PrintGroup(
            UUID.randomUUID().toString(),
            PrintGroup.Type.CASH_RECEIPT,
            null,
            null,
            null,
            null,
            true,
            null,
            null
        )

        val paymentSystem = PaymentSystem(
            PaymentType.CASH,
            "cash",
            "ru.evotor.paymentSystem.cash.base"
        )
        val performer = PaymentPerformer(paymentSystem, null, null, null, PaymentType.CASH.name)


        val payments = HashMap<Payment, BigDecimal>()
        payments[Payment(
            UUID.randomUUID().toString(),
            BigDecimal.TEN,
            null,
            performer,
            null,
            null,
            UUID.randomUUID().toString()
        )] = BigDecimal.TEN


        return listOf(
            Receipt.PrintReceipt(
                printGroup,
                positions = positions(),
                discounts = null,
                payments = payments, changes = mapOf()
            )
        )
    }

    private fun printCorrectionIncome(
        printReceipt: List<Receipt.PrintReceipt>,
    ) {

        val managerCallback = IntegrationManagerCallback { future ->
            try {
                val result = future.result

                if (result.type == IntegrationManagerFuture.Result.Type.ERROR) {
                    notifyUser("Ошибка формирования чека: " + result.error.message + ". Код: " + result.error.code)
                    return@IntegrationManagerCallback
                } else {

                }
            } catch (e: Exception) {
                notifyUser("Exception: ${e.localizedMessage}")
            } finally {
            }
        }

        PrintCorrectionIncomeReceiptCommand(
            printReceipts = printReceipt,
            extra = null,
            clientPhone = null,
            clientEmail = null,
            receiptDiscount = null,
            paymentAddress = null,
            paymentPlace = null,
            userUuid = null,
            correctionDate = Date(),    /* 1178 */
            correctionType = CorrectionType.BY_SELF,
            prescription = null
        ).process(this, managerCallback)
    }


    private fun positions(): List<Position> {
        val position = Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Позиция по свободной цене",
            Measure(
                "л",
                3,
                41
            ),
            BigDecimal.TEN,
            BigDecimal.ONE
        ).build()
        return listOf((position))
    }

    override fun onResume() {
        super.onResume()
        binding.textView.text = prefs.logs
    }

    private fun notifyUser(msg: String) {
        binding.textView.text = binding.textView.text.toString() + "\n\n" + msg
    }
}