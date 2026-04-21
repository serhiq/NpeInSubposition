package com.example.playgroundevotor.data

import android.content.Context
import android.content.SharedPreferences


class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("test", 0)
    var logs: String
        get() = prefs.getString("logs", "").toString()
        set(value) = prefs.edit().putString("logs", value).apply()

    var scenarioResultsJson: String
        get() = prefs.getString("scenario_results_json", "{}").orEmpty()
        set(value) = prefs.edit().putString("scenario_results_json", value).apply()

    var scenarioLabel: String
        get() = prefs.getString("scenario_label", "").orEmpty()
        set(value) = prefs.edit().putString("scenario_label", value).apply()

    var testEmail: String
        get() = prefs.getString("test_email", "").orEmpty()
        set(value) = prefs.edit().putString("test_email", value).apply()

    var configuredEmail: String
        get() = prefs.getString("configured_email", "").orEmpty()
        set(value) = prefs.edit().putString("configured_email", value).apply()

    var includePurchaser: Boolean
        get() = prefs.getBoolean("include_purchaser", false)
        set(value) = prefs.edit().putBoolean("include_purchaser", value).apply()

    var includePaymentPurpose: Boolean
        get() = prefs.getBoolean("include_payment_purpose", false)
        set(value) = prefs.edit().putBoolean("include_payment_purpose", value).apply()

}
