package com.example.playgroundevotor

import android.util.Log
import com.google.gson.GsonBuilder

object Jenny {
    private const val TAG = "com.example.playgroundevotor"
    private const val space ="      "
    private const val spaceBefore = "   "
    private const val sizeOfColumn = 90
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    fun vvv (ll: Any) {
        Log.v(TAG, tag() + prettyGson.toJson(ll).toString())
    }

    private fun tag(): String? {
        return Thread.currentThread().stackTrace[4].let {
            val link = "(${it.fileName}:${it.lineNumber})"
            val path = "$spaceBefore${it.className.substringAfterLast(".")}.${it.methodName}"
            if (path.length + link.length > 80) {
                "${path.take(80 - link.length)}...${link}"
            } else {
                "$path$link"
            }
        }
    }

    fun v() {
        Log.v(TAG, tag() +"$space")
    }

    fun v(msg: String) {
        Log.v(TAG, aroundSpace(tag()) +"$msg")
    }

    private fun aroundSpace(tag: String?): String? {
        val delta = sizeOfColumn - (tag?.length ?: 0)

        if (delta > 0) {
            return  buildString() {
                append(tag)
                append(" ".repeat(delta))
            }
        }
        return tag
    }

    fun v(v1: String, v2: String) {
        Log.v(TAG, aroundSpace(tag()) +"$v1 = $v2")
    }

    fun d(msg: String?) {
        Log.d(tag(), "💙 $msg")
    }

    fun i(msg: String?) {
        Log.i(tag(), "💚 $msg")
    }

    fun w(msg: String?) {
        Log.w(tag(), "💛 $msg")
    }

    fun w(e: Throwable?) {
        Log.w(tag(), "💛 ${e?.localizedMessage}")
        e?.printStackTrace()
    }

    fun w(e: Exception?) {
        Log.w(tag(), "💛 ${e?.localizedMessage}")
        e?.printStackTrace()
    }

    fun w(e: LinkageError?) {
        Log.w(tag(), "💛 ${e?.localizedMessage}")
        e?.printStackTrace()
    }

    fun e(msg: String?) {
        Log.e(tag(), "💔 $msg")
    }

    fun e(e: Throwable?) {
        Log.e(tag(), "💔 ${e?.localizedMessage}")
        e?.printStackTrace()
    }

    fun e(e: java.lang.Exception?) {
        Log.e(tag(), "💔 ${e?.localizedMessage}")
        e?.printStackTrace()
    }
}
