package com.example.playgroundevotor.data

import android.content.Context
import android.content.SharedPreferences


class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("test", 0)
    var logs: String
        get() = prefs.getString("logsss", "").toString()
        set(value) = prefs.edit().putString("logsss", value).apply()

}
