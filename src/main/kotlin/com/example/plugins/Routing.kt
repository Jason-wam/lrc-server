package com.example.plugins

import com.example.utils.LrcDatabase
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }

    routing {
        get("/search") {
            val response = JSONObject()
            val artist = call.parameters["artist"].orEmpty()
            val songName = call.parameters["songName"].orEmpty()
            if (artist.isBlank()) {
                LoggerFactory.getLogger("Search").error("Artist can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "Artist can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@get
            }
            if (songName.isBlank()) {
                LoggerFactory.getLogger("Search").error("SongName can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "songName can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@get
            }

            val list = LrcDatabase.instance.search(artist, songName)
            response.put("status", 200)
            response.put("list", JSONArray().apply {
                list.forEach {
                    put(it.toJSONObject())
                }
            })

            call.respondText(response.toString(2), ContentType.Application.Json)
        }
    }

    routing {
        get("/get") {
            val response = JSONObject()
            val hash = call.parameters["hash"].orEmpty()
            if (hash.isBlank()) {
                LoggerFactory.getLogger("Get").error("Hash can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "Hash can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@get
            }

            val lrc = LrcDatabase.instance.get(hash)
            if (lrc == null) {
                LoggerFactory.getLogger("Get").error("LRC not found!")
                response.put("status", 404)
                response.put("message", "LRC not found!")
            } else {
                LoggerFactory.getLogger("Get").info("Get LRC succeed!")
                response.put("status", 200)
                response.put("message", "Get LRC succeed!")
                response.put("data", lrc.toJSONObject())
            }
            call.respondText(response.toString(2), ContentType.Application.Json)
        }
    }

    routing {
        get("/delete") {
            val response = JSONObject()
            val hash = call.parameters["hash"].orEmpty()
            if (hash.isBlank()) {
                LoggerFactory.getLogger("Get").error("Hash can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "Hash can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@get
            }

            val lrc = LrcDatabase.instance.delete(hash)
            if (lrc) {
                LoggerFactory.getLogger("Get").info("Delete LRC succeed!")
                response.put("status", 200)
                response.put("message", "Delete LRC succeed!")
            } else {
                LoggerFactory.getLogger("Get").error("LRC not found!")
                response.put("status", 404)
                response.put("message", "LRC not found!")
            }
            call.respondText(response.toString(2), ContentType.Application.Json)
        }
    }

    routing {
        post("/public") {
            val response = JSONObject()
            val parameters = call.receiveParameters()
            val artist = parameters["artist"].orEmpty()
            val songName = parameters["songName"].orEmpty()
            val lrcValue = parameters["lrcValue"].orEmpty()
            val duration = parameters["duration"] ?: "0"
            if (artist.isBlank()) {
                LoggerFactory.getLogger("Public").error("Artist can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "Artist can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@post
            }
            if (songName.isBlank()) {
                LoggerFactory.getLogger("Public").error("SongName can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "songName can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@post
            }
            if (lrcValue.isBlank()) {
                LoggerFactory.getLogger("Public").error("LrcValue can't be blank!")
                response.put("status", HttpStatusCode.BadRequest.value)
                response.put("message", "lrcValue can't be blank!")
                call.respondText(response.toString(2), ContentType.Application.Json)
                return@post
            }

            val result = LrcDatabase.instance.put(artist, songName, duration.toLong(), lrcValue)
            response.put("status", HttpStatusCode.OK.value)
            response.put("message", "Public result is $result")
            call.respondText(response.toString(2), ContentType.Application.Json)
        }
    }
}
