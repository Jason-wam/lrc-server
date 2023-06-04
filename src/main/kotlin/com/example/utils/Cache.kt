package com.example.utils

import com.example.extension.asJSONObject
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

object Cache {
    private val directory = System.getProperty("user.dir")
    private val cacheDirectory = File(directory, "cache")

    fun set(key: String, value: String) {
        File(cacheDirectory, "$key.cache").outputStream().bufferedWriter().use {
            it.write(JSONObject().put("time", System.currentTimeMillis()).put("value", value).toString())
        }
    }

    fun get(key: String, timeout: Long = TimeUnit.HOURS.toMillis(5)): String? {
        val file = File(cacheDirectory, "$key.cache")
        if (file.exists().not()) {
            return null
        }
        file.inputStream().bufferedReader().use {
            val obj = it.readText().asJSONObject()
            val time = obj.getLong("time")
            val value = obj.getString("value")
            if (System.currentTimeMillis() - time > timeout && timeout != -1L) {
                LoggerFactory.getLogger("Cache").debug("cache timeout: ${System.currentTimeMillis() - time} > ${TimeUnit.HOURS.toMillis(5)}")
                return null
            } else {
                return value
            }
        }
    }

    fun clear() {
        cacheDirectory.deleteRecursively()
    }
}