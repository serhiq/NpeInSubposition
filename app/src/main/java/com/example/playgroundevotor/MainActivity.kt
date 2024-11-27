package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.playgroundevotor.databinding.ActivityMainBinding
import com.example.playgroundevotor.data.Prefs
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.receipt.ExtraKey
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Position
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
        binding.applyBtn.setOnClickListener { start() }
    }

    private fun start() {

        val changes = positions()
        binding.loadingFl.root.isVisible = true

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
            } finally {
                binding.loadingFl.root.isVisible = false

            }
        })
    }
    private fun positions(): List<PositionAdd> {
//        return (1..10).map {
        return (1..60).map {
            val position = Position.Builder.newInstance(
                UUID.randomUUID().toString(),
                null,
                "Универсальное заполнительное название продукта для использования в каталогах и базах данных, адаптированное под широкий ассортимент товаров разных категорий",
                Measure(
                    "л",
                    3,
                    41
                ),
                BigDecimal.ONE,
                BigDecimal.TEN
            )

            position.setExtraKeys(setOf(ExtraKey(randomString(), "***", randomString())))
            PositionAdd(position.build())
        }
    }

    private fun randomString(): String {
        return (1..20)
            .map { ('a'..'z').random() }
            .joinToString("")
    }



    override fun onResume() {
        super.onResume()
        binding.textView.text = prefs.logs
    }

    private fun notifyUser(msg: String) {
        binding.textView.text = binding.textView.text.toString() + "\n\n" + msg
    }
}