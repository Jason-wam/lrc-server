package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*

fun main(args: Array<String>) {
    var port = 8080
    args.forEach {
        if (it.startsWith("-port")) {
            port = it.removePrefix("-port=").toInt()
        }
    }
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureRouting()
}
