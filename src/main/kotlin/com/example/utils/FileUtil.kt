package com.example.utils

import java.io.File

object FileUtil {
    fun getDataDirectory(): File {
        val directory = File(System.getProperty("user.dir") + File.separator + "Data")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }
}