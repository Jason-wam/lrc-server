package com.example.extension

import java.security.MessageDigest

fun String?.orDefault(value: String): String {
    return if (this.isNullOrBlank()) {
        value
    } else {
        this
    }
}

fun String.toMd5String(): String {
    val md: MessageDigest = MessageDigest.getInstance("MD5")
    md.update(this.toByteArray())
    val b: ByteArray = md.digest()
    var i: Int
    val buf = StringBuffer()
    for (offset in b.indices) {
        i = b[offset].toInt()
        if (i < 0) {
            i += 256
        }
        if (i < 16) {
            buf.append("0")
        }
        buf.append(Integer.toHexString(i))
    }
    return buf.toString()
}