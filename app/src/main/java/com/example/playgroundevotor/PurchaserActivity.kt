package com.example.playgroundevotor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playgroundevotor.data.Prefs
import com.example.playgroundevotor.databinding.ActivityPuchaserBinding
import ru.evotor.framework.component.PaymentPerformer
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.print_receipt_command.PrintReceiptCommandResult
import ru.evotor.framework.core.action.command.print_receipt_command.PrintSellReceiptCommand
import ru.evotor.framework.payment.PaymentSystem
import ru.evotor.framework.payment.PaymentType
import ru.evotor.framework.receipt.DocumentType
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Payment
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.PrintGroup
import ru.evotor.framework.receipt.Purchaser
import ru.evotor.framework.receipt.PurchaserType
import ru.evotor.framework.receipt.Receipt
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.UUID

class PurchaserActivity : AppCompatActivity() {

    private val prefs by lazy { Prefs(applicationContext) }

    private lateinit var binding: ActivityPuchaserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPuchaserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Тестовое приложение для Evotor"

        val purchser1 = Purchaser(
            name = "Авдеев",
            documentNumber = "4507 747384",
            type = PurchaserType.NATURAL_PERSON,
            birthDate = createBirthDate(),
            innNumber = null,
            documentType = DocumentType.PASSPORT_RF
        )

        binding.testCase1.setOnClickListener { sellPrint(purchser1, 1) }
    }

    private fun sellPrint(purchaser: Purchaser, i: Int) {
        try {
            val printReceipt = createExampleReceipt(purchaser)

            val callback = IntegrationManagerCallback { future ->
                try {
                    val result = future.result
                    if (result.type == IntegrationManagerFuture.Result.Type.OK) {
                        val commandResult = PrintReceiptCommandResult.create(result.data)

                        notifyUser((commandResult?.receiptUuid) ?: "Отсутствует идентификатор чека в ${result.data}")
                    } else if (result.type == IntegrationManagerFuture.Result.Type.ERROR) {
                        val error = result.error

                        // код ошибки ЗАКРОЙТЕ СМЕНУ
                        if (error.code == PrintReceiptCommandResult.ERROR_CODE_SESSION_TIME_EXPIRED) {
                            notifyUser("Закройте смену")

                        } else {
                            notifyUser("$i. "+ error.message)
                        }
                    }
                } catch (e: Exception) {
                    notifyUser("$i. "+ e.localizedMessage)
                }
            }


            PrintSellReceiptCommand(listOf(printReceipt), null, null, "user@example.com", BigDecimal.ZERO, null, null, null).process(this, callback)
//            PrintSellReceiptCommand(listOf(printReceipt), null, "89130000573", "mail@mail.com", null, null, null, null).process(this, callback)

        } catch (e: Exception) {
            notifyUser(e.localizedMessage)
        }

    }

    private fun createExampleReceipt(purchaser: Purchaser): Receipt.PrintReceipt {

        val positions = positions()
        val printGroup = PrintGroup(UUID.randomUUID().toString(),
            PrintGroup.Type.CASH_RECEIPT,
            null,
            null,
            null,
            null,
            true,
            purchaser,
            null)

        return Receipt.PrintReceipt(printGroup, positions, makePayments(this, BigDecimal(10)),  HashMap(), HashMap())
    }

    private fun makePayments(context: Context, total: BigDecimal): HashMap<Payment, BigDecimal> {
        val payments = HashMap<Payment, BigDecimal>()
            val paymentSystem = PaymentSystem( PaymentType.CASH, "Интернет-платеж", context.packageName)
            payments[Payment(UUID.randomUUID().toString(),
                total, null,
                PaymentPerformer(paymentSystem, context.packageName, PurchaserActivity::class.java.name, context.getString(R.string.app_uuid), context.getString(R.string.app_name)), null, null,
                UUID.randomUUID().toString())] = total

        return payments
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

    private fun createBirthDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(1980, Calendar.JANUARY, 1)
        return calendar.time
    }
}
