package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playgroundevotor.databinding.ActivityMainBinding
import com.example.playgroundevotor.data.Prefs
import com.example.playgroundevotor.utils.PositionService
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.TaxNumber
import java.math.BigDecimal
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { Prefs(applicationContext) }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Тестовое приложение для Evotor"
        binding.applyBtn.setOnClickListener { start(positions()) }
        binding.exampleFromEvoBtn.setOnClickListener { start(PositionService.positions()) }
    }

    private fun start(changes: MutableList<PositionAdd>) {
        OpenSellReceiptCommand(changes, null, null).process(this, IntegrationManagerCallback { integrationManagerFuture ->
            try {
                val result = integrationManagerFuture.result
                if (IntegrationManagerFuture.Result.Type.ERROR == result.type) {
                    notifyUser("Ошибка формирования чека: " + result.error.message + ". Код: " + result.error.code)
                    return@IntegrationManagerCallback
                }

                val intent = NavigationApi.createIntentForSellReceiptPayment()
                startActivityForResult(intent, 1)

            } catch (e: Exception) {
                notifyUser("Exception: ${e.localizedMessage}")
            }
        })
    }

    private fun positions(): MutableList<PositionAdd> {
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
        )

        val subPosition = Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Горный мох",
            Measure(
                "л",
                3,
                41
            ),
            BigDecimal.TEN,
            BigDecimal.TEN
        ).build()

        position.setSubPositions(mutableListOf(subPosition))


        return mutableListOf(PositionAdd(position.build()))
    }

    override fun onResume() {
        super.onResume()
        binding.textView.text = prefs.logs
    }
    private fun notifyUser(msg: String) {
        binding.textView.text = binding.textView.text.toString() + "\n\n" + msg
    }
}