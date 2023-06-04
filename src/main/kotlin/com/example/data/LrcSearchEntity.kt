package com.example.data

import org.json.JSONObject

data class LrcSearchEntity(val hash: String, val artist: String, val songName: String, val duration: Long) {
    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put("hash", hash)
            put("artist", artist)
            put("songName", songName)
            put("duration", duration)
        }
    }
}