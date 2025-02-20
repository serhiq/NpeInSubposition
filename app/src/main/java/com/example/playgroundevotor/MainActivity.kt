package com.example.playgroundevotor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playgroundevotor.databinding.ActivityMainBinding
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.position.Mark
import java.math.BigDecimal
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Тестовое приложение для Evotor"
        binding.applyBtn.setOnClickListener { start(positions()) }
    }

    private fun start(changes: MutableList<PositionAdd>) {
        OpenSellReceiptCommand(changes, null, null).process(
            this,
            IntegrationManagerCallback { integrationManagerFuture ->
                try {
                    val result = integrationManagerFuture.result
                    if (IntegrationManagerFuture.Result.Type.ERROR == result.type) {
                        notifyUser("Ошибка формирования чека: " + result.error.message + ". Код: " + result.error.code)
                        return@IntegrationManagerCallback
                    }

                    val intent = NavigationApi.createIntentForSellReceiptPayment()
                    startActivityForResult(intent, REQUEST_CODE_SELL_PAYMENT)

                } catch (e: Exception) {
                    notifyUser("Exception: ${e.localizedMessage}")
                }
            })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_SELL_PAYMENT -> {
                notifyUser("requestCode == REQUEST_CODE_SELL_PAYMENT")
            }
        }
    }

    private fun positions(): MutableList<PositionAdd> {
        val position = Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Маркированная вода",
            Measure(
                "л",
                3,
                41
            ),
            BigDecimal.TEN,
            BigDecimal.ONE
        ).toWaterMarked(Mark.RawMark("some temp mark"))

        return mutableListOf(PositionAdd(position.build()))
    }

    private fun notifyUser(msg: String) {
        binding.textView.text = msg
    }

    companion object {
        private const val REQUEST_CODE_SELL_PAYMENT = 0
    }
}