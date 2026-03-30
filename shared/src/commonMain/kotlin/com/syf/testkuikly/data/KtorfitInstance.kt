package com.syf.testkuikly.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

object KtorfitInstance {

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpCookies)
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[KtorLog] $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    val api: WanApiService by lazy {
        createWanApiServiceImpl(httpClient, getBaseUrl())
    }
}
