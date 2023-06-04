package com.example.data

import org.json.JSONObject
import java.util.Base64

data class LrcEntity(val artist: String, val songName: String, val duration: Long, val lrc: String) {
    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put("artist", artist)
            put("songName", songName)
            put("duration", duration)
            put("lrc", Base64.getEncoder().encodeToString(lrc.toByteArray()))
        }
    }
}