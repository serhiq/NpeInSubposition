package com.example.playgroundevotor

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.playgroundevotor.data.Prefs
import com.example.playgroundevotor.databinding.ActivityTerminalChecksBinding
import com.example.playgroundevotor.utils.PositionService
import org.json.JSONObject
import ru.evotor.framework.component.PaymentPerformer
import ru.evotor.framework.component.PaymentPerformerApi
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.IntegrationManagerImpl
import ru.evotor.framework.core.action.command.open_receipt_command.OpenBuyReceiptCommand
import ru.evotor.framework.core.action.command.open_receipt_command.OpenBuybackReceiptCommand
import ru.evotor.framework.core.action.command.open_receipt_command.OpenPaybackReceiptCommand
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.command.print_receipt_command.PrintBuyReceiptCommand
import ru.evotor.framework.core.action.command.print_receipt_command.PrintBuybackReceiptCommand
import ru.evotor.framework.core.action.command.print_receipt_command.PrintPaybackReceiptCommand
import ru.evotor.framework.core.action.command.print_receipt_command.PrintSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.receipt.SetExtra
import ru.evotor.framework.core.action.event.receipt.changes.receipt.SetInternetRequisites
import ru.evotor.framework.core.action.event.receipt.changes.receipt.SetPurchaserContactData
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.payment.PaymentType
import ru.evotor.framework.receipt.Payment
import ru.evotor.framework.receipt.PrintGroup
import ru.evotor.framework.receipt.Receipt
import ru.evotor.framework.receipt.Position
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TerminalChecksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTerminalChecksBinding
    private val prefs by lazy { Prefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTerminalChecksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.terminal_checks_title)
        binding.emailInputEditText.setText(storedEmailOrDefault())
        binding.emailInputEditText.doAfterTextChanged { editable ->
            prefs.configuredEmail = editable?.toString().orEmpty()
        }
        bindActions()
        renderIdleState()
        listenBackStack()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.resultContainer.isVisible) {
                    closeResult()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun bindActions() {
        binding.printSellReceiptButton.setOnClickListener {
            runPrintScenario(Scenario.PRINT_SELL_RECEIPT)
        }
        binding.printPaybackReceiptButton.setOnClickListener {
            runPrintScenario(Scenario.PRINT_PAYBACK_RECEIPT)
        }
        binding.printBuyReceiptButton.setOnClickListener {
            runPrintScenario(Scenario.PRINT_BUY_RECEIPT)
        }
        binding.printBuybackReceiptButton.setOnClickListener {
            runPrintScenario(Scenario.PRINT_BUYBACK_RECEIPT)
        }
        binding.openSellReceiptButton.setOnClickListener {
            runOpenScenario(Scenario.OPEN_SELL_RECEIPT)
        }
        binding.openPaybackReceiptButton.setOnClickListener {
            runOpenScenario(Scenario.OPEN_PAYBACK_RECEIPT)
        }
        binding.openBuyReceiptButton.setOnClickListener {
            runOpenScenario(Scenario.OPEN_BUY_RECEIPT)
        }
        binding.openBuybackReceiptButton.setOnClickListener {
            runOpenScenario(Scenario.OPEN_BUYBACK_RECEIPT)
        }
        binding.summaryLogsButton.setOnClickListener {
            showLogsSummary()
        }
        binding.shortLogButton.setOnClickListener {
            showShortLog()
        }
        binding.clearLogsButton.setOnClickListener {
            clearLogs()
        }
    }

    private fun listenBackStack() {
        supportFragmentManager.addOnBackStackChangedListener {
            binding.resultContainer.isVisible = supportFragmentManager.backStackEntryCount > 0
        }
    }

    private fun runOpenScenario(scenario: Scenario) {
        runScenario(scenario) {
            val email = configuredEmail()
            val changes = PositionService.positions(scenario.itemName)
            val contacts = SetPurchaserContactData.createForEmail(email)
            val internetRequisites = SetInternetRequisites(true, scenario.paymentPlace)
            resetTestOptions(scenario = scenario, email = email)

            when (scenario) {
                Scenario.OPEN_SELL_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, OpenSellReceiptCommand.NAME, "Открытие SELL чека недоступно на этом устройстве.")) {
                        return@runScenario
                    }
                    OpenSellReceiptCommand(changes, scenarioExtra(scenario), contacts, internetRequisites).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildOpenSuccessDetails(scenario, email, changes.size),
                            onSuccess = { openReceiptPaymentScreen(scenario) }
                        )
                    }
                }

                Scenario.OPEN_PAYBACK_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, OpenPaybackReceiptCommand.NAME, "Открытие PAYBACK чека недоступно на этом устройстве.")) {
                        return@runScenario
                    }
                    OpenPaybackReceiptCommand(changes, scenarioExtra(scenario), contacts, null, internetRequisites).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildOpenSuccessDetails(scenario, email, changes.size),
                            onSuccess = { openReceiptPaymentScreen(scenario) }
                        )
                    }
                }

                Scenario.OPEN_BUY_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, OpenBuyReceiptCommand.NAME, "Открытие BUY чека недоступно на этом устройстве.")) {
                        return@runScenario
                    }
                    OpenBuyReceiptCommand(changes, scenarioExtra(scenario), contacts, internetRequisites).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildOpenSuccessDetails(scenario, email, changes.size),
                            onSuccess = { openReceiptPaymentScreen(scenario) }
                        )
                    }
                }

                Scenario.OPEN_BUYBACK_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, OpenBuybackReceiptCommand.NAME, "Открытие BUYBACK чека недоступно на этом устройстве.")) {
                        return@runScenario
                    }
                    OpenBuybackReceiptCommand(changes, scenarioExtra(scenario), contacts, internetRequisites).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildOpenSuccessDetails(scenario, email, changes.size),
                            onSuccess = { openReceiptPaymentScreen(scenario) }
                        )
                    }
                }

                else -> error("Unexpected scenario for open runner: $scenario")
            }
        }
    }

    private fun runPrintScenario(scenario: Scenario) {
        runScenario(scenario) {
            val email = configuredEmail()
            val positions = PositionService.receiptPositions(scenario.itemName)
            val paymentPerformer = defaultCashPaymentPerformer()
            val payments = listOf(createPayment(positions, paymentPerformer))
            val printReceipt = buildPrintReceipt(positions, payments)
            resetTestOptions(scenario = scenario, email = email)

            when (scenario) {
                Scenario.PRINT_SELL_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, PrintSellReceiptCommand.NAME, "Печать SELL чека недоступна на этом устройстве.")) {
                        return@runScenario
                    }
                    PrintSellReceiptCommand(
                        listOf(printReceipt),
                        scenarioExtra(scenario),
                        null,
                        email,
                        BigDecimal.ZERO,
                        scenario.paymentAddress,
                        scenario.paymentPlace,
                        null,
                        true
                    ).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildPrintSuccessDetails(scenario, paymentPerformer, positions.size, payments.first().value, email)
                        )
                    }
                }

                Scenario.PRINT_PAYBACK_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, PrintPaybackReceiptCommand.NAME, "Печать PAYBACK чека недоступна на этом устройстве.")) {
                        return@runScenario
                    }
                    PrintPaybackReceiptCommand(
                        listOf(printReceipt),
                        scenarioExtra(scenario),
                        null,
                        email,
                        BigDecimal.ZERO,
                        null,
                        scenario.paymentAddress,
                        scenario.paymentPlace,
                        null,
                        true
                    ).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildPrintSuccessDetails(scenario, paymentPerformer, positions.size, payments.first().value, email)
                        )
                    }
                }

                Scenario.PRINT_BUY_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, PrintBuyReceiptCommand.NAME, "Печать BUY чека недоступна на этом устройстве.")) {
                        return@runScenario
                    }
                    PrintBuyReceiptCommand(
                        listOf(printReceipt),
                        scenarioExtra(scenario),
                        null,
                        email,
                        BigDecimal.ZERO,
                        scenario.paymentAddress,
                        scenario.paymentPlace,
                        null,
                        true
                    ).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildPrintSuccessDetails(scenario, paymentPerformer, positions.size, payments.first().value, email)
                        )
                    }
                }

                Scenario.PRINT_BUYBACK_RECEIPT -> {
                    if (!ensureActionAvailable(scenario, PrintBuybackReceiptCommand.NAME, "Печать BUYBACK чека недоступна на этом устройстве.")) {
                        return@runScenario
                    }
                    PrintBuybackReceiptCommand(
                        listOf(printReceipt),
                        scenarioExtra(scenario),
                        null,
                        email,
                        BigDecimal.ZERO,
                        scenario.paymentAddress,
                        scenario.paymentPlace,
                        null,
                        true
                    ).process(this) { future ->
                        handleIntegrationResult(
                            scenario = scenario,
                            future = future,
                            successMessage = buildPrintSuccessDetails(scenario, paymentPerformer, positions.size, payments.first().value, email)
                        )
                    }
                }

                else -> error("Unexpected scenario for print runner: $scenario")
            }
        }
    }

    private fun resetTestOptions(scenario: Scenario, email: String) {
        prefs.scenarioLabel = scenario.label
        prefs.testEmail = email
        prefs.includePurchaser = false
        prefs.includePaymentPurpose = false
    }

    private fun storedEmailOrDefault(): String {
        return prefs.configuredEmail.takeIf { it.isNotBlank() } ?: DEFAULT_EMAIL
    }

    private fun configuredEmail(): String {
        val currentInput = binding.emailInputEditText.text?.toString()?.trim().orEmpty()
        val resolved = currentInput.ifBlank { DEFAULT_EMAIL }
        if (currentInput != resolved) {
            binding.emailInputEditText.setText(resolved)
            binding.emailInputEditText.setSelection(resolved.length)
        }
        prefs.configuredEmail = resolved
        return resolved
    }

    private fun ensureActionAvailable(scenario: Scenario, action: String, unavailableMessage: String): Boolean {
        val components = IntegrationManagerImpl.convertImplicitIntentToExplicitIntent(action, applicationContext)
        if (components.isNullOrEmpty()) {
            renderError(
                scenario = scenario,
                message = buildString {
                    appendLine(unavailableMessage)
                    appendLine("Action: $action")
                    appendLine("Обычно это означает, что приложение запущено не на терминале Evotor или на устройстве нет нужного Evotor-компонента.")
                }
            )
            return false
        }
        return true
    }

    private fun runScenario(scenario: Scenario, block: () -> Unit) {
        renderRunningState(scenario)
        appendLogEntry("${scenario.label}: запуск сценария")
        runCatching(block).onFailure { throwable ->
            renderError(
                scenario = scenario,
                message = buildString {
                    appendLine("Исключение на стороне приложения.")
                    appendLine(throwable::class.java.simpleName)
                    appendLine(throwable.localizedMessage ?: "Без сообщения")
                }
            )
        }
    }

    private fun handleIntegrationResult(
        scenario: Scenario,
        future: IntegrationManagerFuture,
        successMessage: String,
        onSuccess: (() -> Unit)? = null
    ) {
        try {
            val result = future.result
            if (IntegrationManagerFuture.Result.Type.ERROR == result.type) {
                renderError(
                    scenario = scenario,
                    message = buildString {
                        appendLine("SDK вернул ошибку.")
                        appendLine("Код: ${result.error.code}")
                        appendLine("Сообщение: ${result.error.message}")
                    }
                )
                return
            }

            if (onSuccess != null) {
                onSuccess()
            } else {
                renderSuccess(scenario, successMessage)
            }
        } catch (throwable: Throwable) {
            renderError(
                scenario = scenario,
                message = buildString {
                    appendLine("Не удалось обработать ответ SDK.")
                    appendLine(throwable::class.java.simpleName)
                    appendLine(throwable.localizedMessage ?: "Без сообщения")
                }
            )
        }
    }

    private fun openReceiptPaymentScreen(scenario: Scenario) {
        val intent = when (scenario) {
            Scenario.OPEN_SELL_RECEIPT -> NavigationApi.createIntentForSellReceiptPayment(context = this)
            Scenario.OPEN_PAYBACK_RECEIPT -> NavigationApi.createIntentForPaybackReceiptPayment(context = this)
            Scenario.OPEN_BUY_RECEIPT -> NavigationApi.createIntentForBuyReceiptPayment(context = this)
            Scenario.OPEN_BUYBACK_RECEIPT -> NavigationApi.createIntentForBuybackReceiptPayment(context = this)
            else -> error("Unexpected scenario for payment screen: $scenario")
        }
        renderIdleState()
        appendLogEntry("${scenario.label}: открываю экран оплаты Evotor UI")
        startActivityForResult(intent, REQUEST_CODE_OPEN_RECEIPT_PAYMENT)
    }

    private fun defaultCashPaymentPerformer(): PaymentPerformer {
        return PaymentPerformerApi.getAllPaymentPerformers(packageManager)
            .firstOrNull { it.paymentSystem?.paymentType == PaymentType.CASH }
            ?: throw IllegalStateException("На терминале не найден исполнитель оплаты наличными.")
    }

    private fun createPayment(
        positions: List<Position>,
        paymentPerformer: PaymentPerformer
    ): Payment {
        val total = positions.fold(BigDecimal.ZERO) { acc, position ->
            acc.add(position.totalWithSubPositionsAndWithoutDocumentDiscount)
        }
        return Payment(
            UUID.randomUUID().toString(),
            total,
            paymentPerformer.paymentSystem,
            paymentPerformer,
            null,
            null,
            null
        )
    }

    private fun buildPrintReceipt(
        positions: List<Position>,
        payments: List<Payment>
    ): Receipt.PrintReceipt {
        return Receipt.PrintReceipt(
            PrintGroup(
                UUID.randomUUID().toString(),
                PrintGroup.Type.CASH_RECEIPT,
                null,
                null,
                null,
                null,
                true,
                null,
                null
            ),
            positions,
            payments.associate { it to it.value },
            calculateChanges(positions, payments),
            hashMapOf()
        )
    }

    private fun calculateChanges(
        positions: List<Position>,
        payments: List<Payment>
    ): Map<Payment, BigDecimal> {
        val total = positions.fold(BigDecimal.ZERO) { acc, position ->
            acc.add(position.totalWithSubPositionsAndWithoutDocumentDiscount)
        }
        val payment = payments.firstOrNull() ?: return emptyMap()
        val change = if (payment.paymentPerformer.paymentSystem?.paymentType == PaymentType.CASH) {
            payment.value.subtract(total).max(BigDecimal.ZERO)
        } else {
            BigDecimal.ZERO
        }
        return mapOf(payment to change)
    }

    private fun scenarioExtra(scenario: Scenario): SetExtra {
        val payload = JSONObject()
            .put("scenario", scenario.code)
            .put("command", scenario.commandName)
            .put("expectedType", scenario.expectedType)
            .put("expectedPaymentPlace", scenario.paymentPlace)
            .put("expectedPaymentAddress", scenario.paymentAddress)
            .put("expectedInternet", true)
            .put("checkPaymentAddress", scenario.checkPaymentAddress)
            .put("timestamp", System.currentTimeMillis())
            .put("packageName", packageName)
        return SetExtra(payload)
    }

    private fun buildOpenSuccessDetails(
        scenario: Scenario,
        email: String,
        positionsCount: Int
    ): String {
        return buildString {
            appendLine("Сценарий: ${scenario.label}")
            appendLine("Позиций: $positionsCount")
            appendLine("Email: $email")
            appendLine("Команда: ${scenario.commandName}")
            appendLine("Товар: ${scenario.itemName}")
            appendLine("SetInternetRequisites(receiptFromInternet=true, paymentPlace=${scenario.paymentPlace})")
            append("Команда открытия чека отправлена. После закрытия чека readback попадет в логи.")
        }
    }

    private fun buildPrintSuccessDetails(
        scenario: Scenario,
        paymentPerformer: PaymentPerformer,
        positionsCount: Int,
        total: BigDecimal,
        email: String
    ): String {
        return buildString {
            appendLine("Сценарий: ${scenario.label}")
            appendLine("Позиций: $positionsCount")
            appendLine("Сумма: $total")
            appendLine("Оплата: ${paymentPerformer.paymentSystem?.userDescription ?: "не определена"}")
            appendLine("Email: $email")
            appendLine("Товар: ${scenario.itemName}")
            appendLine("Команда: ${scenario.commandName}")
            appendLine("receiptFromInternet=true")
            appendLine("paymentPlace=${scenario.paymentPlace}")
            appendLine("paymentAddress=${scenario.paymentAddress}")
            append("Команда печати отправлена. После закрытия чека readback попадет в логи.")
        }
    }

    private fun renderRunningState(scenario: Scenario) {
        setButtonsEnabled(false)
        binding.statusTitle.text = getString(R.string.terminal_checks_running_title)
        binding.statusMessage.text = getString(R.string.terminal_checks_running_message, scenario.label)
    }

    private fun renderIdleState() {
        setButtonsEnabled(true)
        binding.statusTitle.text = getString(R.string.terminal_checks_idle_title)
        binding.statusMessage.text = getString(R.string.terminal_checks_idle_message)
    }

    private fun renderSuccess(scenario: Scenario, details: String) {
        setButtonsEnabled(true)
        binding.statusTitle.text = getString(R.string.terminal_checks_success_title)
        binding.statusMessage.text = getString(R.string.terminal_checks_success_message, scenario.label)
        storeScenarioResult(scenario, "команда отправлена", details.lineSequence().firstOrNull().orEmpty())
        appendLogEntry("${scenario.label}: успех\n$details")
        showResult(
            title = getString(R.string.terminal_checks_result_success, scenario.label),
            message = details
        )
    }

    private fun renderError(scenario: Scenario, message: String) {
        setButtonsEnabled(true)
        binding.statusTitle.text = getString(R.string.terminal_checks_error_title)
        binding.statusMessage.text = getString(R.string.terminal_checks_error_message, scenario.label)
        storeScenarioResult(scenario, "ошибка", message.lineSequence().firstOrNull().orEmpty())
        appendLogEntry("${scenario.label}: ошибка\n$message")
        showResult(
            title = getString(R.string.terminal_checks_result_error, scenario.label),
            message = message
        )
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.printSellReceiptButton.isEnabled = enabled
        binding.printPaybackReceiptButton.isEnabled = enabled
        binding.printBuyReceiptButton.isEnabled = enabled
        binding.printBuybackReceiptButton.isEnabled = enabled
        binding.openSellReceiptButton.isEnabled = enabled
        binding.openPaybackReceiptButton.isEnabled = enabled
        binding.openBuyReceiptButton.isEnabled = enabled
        binding.openBuybackReceiptButton.isEnabled = enabled
        binding.summaryLogsButton.isEnabled = enabled
        binding.shortLogButton.isEnabled = enabled
        binding.clearLogsButton.isEnabled = enabled
    }

    private fun showLogsSummary() {
        renderIdleState()
        showResult(
            title = SUMMARY_SCENARIO.label,
            message = buildLogsSummaryReport()
        )
    }

    private fun showShortLog() {
        renderIdleState()
        showResult(
            title = SHORT_LOG_SCENARIO.label,
            message = buildShortLogReport()
        )
    }

    private fun clearLogs() {
        prefs.logs = ""
        prefs.scenarioResultsJson = "{}"
        renderIdleState()
        showResult(
            title = CLEAR_LOGS_SCENARIO.label,
            message = "Полные логи и короткие итоги по сценариям очищены."
        )
    }

    private fun showResult(title: String, message: String) {
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        binding.resultContainer.isVisible = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.result_container, ScenarioResultFragment.newInstance(title, message))
            .addToBackStack(ScenarioResultFragment.TAG)
            .commit()
    }

    private fun closeResult() {
        supportFragmentManager.popBackStack()
    }

    private fun buildLogsSummaryReport(): String {
        val shortResults = readScenarioResults()
        return buildString {
            appendLine("Терминал")
            appendLine("Модель: ${Build.MODEL}")
            appendLine("Производитель: ${Build.MANUFACTURER}")
            appendLine("Бренд: ${Build.BRAND}")
            appendLine("Устройство: ${Build.DEVICE}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine()
            appendLine("Тестовое приложение")
            appendLine("Package: $packageName")
            appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine()
            appendLine("Evotor компоненты")
            for (appInfo in terminalAppsInfo()) {
                appendLine("${appInfo.name}: ${installedVersion(appInfo.packageName) ?: "не установлено"}")
            }
            appendLine("FFD: не определен")
            appendLine()
            appendLine("Короткий итог")
            for (scenario in TEST_SCENARIOS) {
                val result = shortResults.optString(scenario.code).ifBlank { "не запускался" }
                appendLine("${scenario.label} -> $result")
            }
            appendLine()
            appendLine("Полные логи")
            val logs = prefs.logs.trim()
            if (logs.isBlank()) {
                appendLine("Логов пока нет.")
            } else {
                appendLine(logs)
            }
        }
    }

    private fun buildShortLogReport(): String {
        val shortResults = readScenarioResults()
        return buildString {
            appendLine("Короткий итог по сценариям")
            appendLine()
            for (scenario in TEST_SCENARIOS) {
                val result = shortResults.optString(scenario.code).ifBlank { "NOT RUN" }
                appendLine("${scenario.label} -> $result")
            }
        }.trim()
    }

    private fun storeScenarioResult(scenario: Scenario, status: String, details: String) {
        val json = readScenarioResults()
        json.put(scenario.code, "$status${details.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}")
        prefs.scenarioResultsJson = json.toString()
    }

    private fun readScenarioResults(): JSONObject {
        val raw = prefs.scenarioResultsJson.ifBlank { "{}" }
        return runCatching { JSONObject(raw) }.getOrElse { JSONObject() }
    }

    private fun appendLogEntry(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date())
        val current = prefs.logs
        val entry = buildString {
            appendLine()
            appendLine("----------------------------------")
            appendLine(timestamp)
            appendLine(message.trim())
        }
        prefs.logs = current + entry
    }

    @Suppress("DEPRECATION")
    private fun installedVersion(packageName: String): String? {
        return runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName
        }.getOrNull()
    }

    private fun terminalAppsInfo(): List<AppInfo> = listOf(
        AppInfo("Алкоголь", "ru.evotor.egais.ui"),
        AppInfo("Evotor POS", "ru.atol.tabletpos"),
        AppInfo("EvoKkmDriver", "ru.evotor.drivers.kkm"),
        AppInfo("PaidUpdates", "ru.evotor.paidupdates")
    )

    private data class AppInfo(
        val name: String,
        val packageName: String
    )

    private enum class Scenario(
        val number: Int,
        val code: String,
        val commandName: String,
        val expectedType: String,
        val itemName: String,
        val paymentPlace: String,
        val paymentAddress: String,
        val checkPaymentAddress: Boolean
    ) {
        PRINT_SELL_RECEIPT(
            1,
            "print_sell_receipt",
            "PrintSellReceipt",
            "SELL",
            "SELL PrintSellReceipt internet happy",
            "SELL PrintSellReceipt payment place",
            "SELL PrintSellReceipt payment address",
            true
        ),
        PRINT_PAYBACK_RECEIPT(
            2,
            "print_payback_receipt",
            "PrintPaybackReceipt",
            "PAYBACK",
            "PAYBACK PrintPaybackReceipt internet happy",
            "PAYBACK PrintPaybackReceipt payment place",
            "PAYBACK PrintPaybackReceipt payment address",
            true
        ),
        PRINT_BUY_RECEIPT(
            3,
            "print_buy_receipt",
            "PrintBuyReceipt",
            "BUY",
            "BUY PrintBuyReceipt internet happy",
            "BUY PrintBuyReceipt payment place",
            "BUY PrintBuyReceipt payment address",
            true
        ),
        PRINT_BUYBACK_RECEIPT(
            4,
            "print_buyback_receipt",
            "PrintBuybackReceipt",
            "BUYBACK",
            "BUYBACK PrintBuybackReceipt internet happy",
            "BUYBACK PrintBuybackReceipt payment place",
            "BUYBACK PrintBuybackReceipt payment address",
            true
        ),
        OPEN_SELL_RECEIPT(
            5,
            "open_sell_receipt",
            "OpenSellReceipt",
            "SELL",
            "SELL OpenSellReceipt internet happy",
            "SELL OpenSellReceipt payment place",
            "SELL OpenSellReceipt payment address",
            false
        ),
        OPEN_PAYBACK_RECEIPT(
            6,
            "open_payback_receipt",
            "OpenPaybackReceipt",
            "PAYBACK",
            "PAYBACK OpenPaybackReceipt internet happy",
            "PAYBACK OpenPaybackReceipt payment place",
            "PAYBACK OpenPaybackReceipt payment address",
            false
        ),
        OPEN_BUY_RECEIPT(
            7,
            "open_buy_receipt",
            "OpenBuyReceipt",
            "BUY",
            "BUY OpenBuyReceipt internet happy",
            "BUY OpenBuyReceipt payment place",
            "BUY OpenBuyReceipt payment address",
            false
        ),
        OPEN_BUYBACK_RECEIPT(
            8,
            "open_buyback_receipt",
            "OpenBuybackReceipt",
            "BUYBACK",
            "BUYBACK OpenBuybackReceipt internet happy",
            "BUYBACK OpenBuybackReceipt payment place",
            "BUYBACK OpenBuybackReceipt payment address",
            false
        );

        val label: String
            get() = "$number. $commandName"
    }

    companion object {
        private const val DEFAULT_EMAIL = "example@gmail.com"
        private const val REQUEST_CODE_OPEN_RECEIPT_PAYMENT = 1001
        private val TEST_SCENARIOS = listOf(
            Scenario.PRINT_SELL_RECEIPT,
            Scenario.PRINT_PAYBACK_RECEIPT,
            Scenario.PRINT_BUY_RECEIPT,
            Scenario.PRINT_BUYBACK_RECEIPT,
            Scenario.OPEN_SELL_RECEIPT,
            Scenario.OPEN_PAYBACK_RECEIPT,
            Scenario.OPEN_BUY_RECEIPT,
            Scenario.OPEN_BUYBACK_RECEIPT
        )
        private val SUMMARY_SCENARIO = object {
            val label = "9. Итоговые логи"
        }
        private val SHORT_LOG_SCENARIO = object {
            val label = "10. short_log"
        }
        private val CLEAR_LOGS_SCENARIO = object {
            val label = "11. Сбросить логи"
        }
    }
}
