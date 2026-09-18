package com.willam.chatnotes

import android.content.Context

object InterceptorJs {
    fun source(context: Context): String =
        context.assets.open("interceptor.js").bufferedReader().use { it.readText() }
}
