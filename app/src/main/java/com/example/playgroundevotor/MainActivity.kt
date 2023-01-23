package com.example.playgroundevotor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import ru.evotor.framework.receipt.Measure
import ru.evotor.framework.receipt.Position
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    private val iso8601Formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<Button>(R.id.textView)

        findViewById<Button>(R.id.button).setOnClickListener {
            try {
                printSubPositions(requireNotNull(createPositionWithSubPosition()))
            } catch (e: Exception) {
                displayLogOnTextView("Exception: ${e.localizedMessage}")
                Jenny.v(e.localizedMessage)
            }
        }
    }

    private fun createPositionWithSubPosition(): Position? {
        val subPositions = listOf<Position>(
            Position.Builder.newInstance(
                UUID.randomUUID().toString(),
                null,
                "Субпозиция",
                Measure("дроб", 3, 255),
                BigDecimal.ONE,
                BigDecimal.TEN
            ).build()
        )

        return Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Позиция c субпозициями",
            Measure("дроб", 3, 255),
            BigDecimal.ONE,
            BigDecimal.TEN
        ).setSubPositions(subPositions).build()
    }

    private fun createPosition(): Position {
        return Position.Builder.newInstance(
            UUID.randomUUID().toString(),
            null,
            "Позиция",
            Measure("дроб", 3, 255),
            BigDecimal.ONE,
            BigDecimal.TEN
        ).build()
    }

    private fun printSubPositions (position: Position) {

        logPosition(position)

        position.subPositions.forEach{ p ->
            printSubPositions(p)
        }
    }

    private fun displayLogOnTextView(s: String?) {
        s ?: return
        val oldText = textView.text
        val date = iso8601Formatter.format(Date())
        textView.text = "$oldText \n $date  ${s} "

    }

    private fun logPosition(position: Position) {
        Jenny.v("печатаем позицию: " + position.name)
        Jenny.vvv(position)
        val haveSubPositions = position.subPositions?.size.toString() ?: "nil"
        Jenny.v("есть субпозиции: $haveSubPositions")

        /////////////////////
        displayLogOnTextView("${position.name}\nsubPositions size $haveSubPositions\n-------------\n")
    }
}