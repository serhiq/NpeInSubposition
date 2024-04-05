package com.example.playgroundevotor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.playgroundevotor.databinding.ActivityPaymentScreenBinding
import com.google.gson.GsonBuilder
import ru.evotor.framework.core.IntegrationAppCompatActivity
import ru.evotor.framework.core.action.event.receipt.discount.ReceiptDiscountEvent
import ru.evotor.framework.receipt.ReceiptApi

class ShowReceiptActivity : IntegrationAppCompatActivity() {

    private val sourceEvent by lazy {
        ReceiptDiscountEvent.create(sourceBundle)
            ?: throw RuntimeException("Отсутствует исходное событие в $sourceBundle")
    }
    private val receipt by lazy {
        ReceiptApi.getReceipt(this, sourceEvent.receiptUuid)
            ?: throw java.lang.RuntimeException("Чек с идентификатором ${sourceEvent.receiptUuid} не найден.")
    }

    private lateinit var binding: ActivityPaymentScreenBinding
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonClose.setOnClickListener { finish() }

        showReceipt()
    }

    private fun showReceipt() {
        binding.textView.text = buildString {
            receipt.header.number
            receipt.header.number
            append(prettyGson.toJson(receipt).toString())
        }
    }

    companion object {
        fun start(context: Context): Intent {
            return Intent(context, ShowReceiptActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}