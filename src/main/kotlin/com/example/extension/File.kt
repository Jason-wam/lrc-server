package com.example.extension

import java.io.File

fun File.listAllFiles(): List<File> {
    return ArrayList<File>().apply {
        listFiles()?.forEach {
            if (it.isDirectory) {
                addAll(it.listAllVideos())
            } else {
                add(it)
            }
        }
    }
}

fun File.listAllVideos(): List<File> {
    val suffixes = listOf("mp4", "mkv", "mp3", "m4a", "wma", "wav", "avi", "rmvb")
    return ArrayList<File>().apply {
        listFiles()?.forEach {
            if (it.isDirectory) {
                addAll(it.listAllVideos())
            } else {
                val suffix = it.name.substringAfterLast(".")
                if (suffixes.contains(suffix)) {
                    add(it)
                }
            }
        }
    }
}

fun File.mediaInfo(): String {
    val params = ArrayList<String>()
    params.add("ffprobe")
    params.add("-i \"${absolutePath}\"")
    params.add("-v quiet")
    params.add("-print_format json")
    params.add("-show_format")
    params.add("-hide_banner")

    val command = params.joinToString(" ")
    val process = Runtime.getRuntime().exec(command)
    val json = process.inputStream.bufferedReader().readText()
    process.inputStream.close()
    process.errorStream.close()
    process.destroy()
    return json.ifBlank { "{}" }
}