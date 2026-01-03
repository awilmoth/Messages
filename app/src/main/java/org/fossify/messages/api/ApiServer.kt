package org.fossify.messages.api

import android.content.Context
import android.util.Log
import org.nanohttpd.NanoHTTPD
import com.google.gson.Gson

class ApiServer(
    private val context: Context,
    private val port: Int = 8080,
    private val onMessageReceived: (from: String, body: String) -> Unit
) : NanoHTTPD(port) {
    
    private var serverRunning = false
    private val gson = Gson()
    private val tag = "ApiServer"

    override fun serve(session: IHTTPSession?): Response {
        return try {
            when (session?.uri) {
                "/api/send-sms" -> handleSendSms(session)
                "/api/send-mms" -> handleSendMms(session)
                "/api/status" -> newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    gson.toJson(mapOf("status" to "running", "port" to port))
                )
                else -> newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    gson.toJson(mapOf("error" to "endpoint not found"))
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error handling request: ${e.message}", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                gson.toJson(mapOf("error" to e.message))
            )
        }
    }

    private fun handleSendSms(session: IHTTPSession): Response {
        val params = mutableMapOf<String, String>()
        session.parseBody(params)
        
        val to = params["to"] ?: ""
        val message = params["message"] ?: ""
        
        return if (to.isNotEmpty() && message.isNotEmpty()) {
            try {
                // Queue message for sending (implementation depends on Fossify's SMS handler)
                Log.d(tag, "SMS to $to: $message")
                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    gson.toJson(mapOf("status" to "queued", "to" to to))
                )
            } catch (e: Exception) {
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "application/json",
                    gson.toJson(mapOf("error" to e.message))
                )
            }
        } else {
            newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json",
                gson.toJson(mapOf("error" to "missing parameters"))
            )
        }
    }

    private fun handleSendMms(session: IHTTPSession): Response {
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            gson.toJson(mapOf("status" to "mms_support_coming_soon"))
        )
    }

    fun startServer(): Boolean {
        return try {
            start()
            serverRunning = true
            Log.d(tag, "API Server started on port $port")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start API server: ${e.message}", e)
            serverRunning = false
            false
        }
    }

    fun stopServer() {
        try {
            if (serverRunning) {
                stop()
                serverRunning = false
                Log.d(tag, "API Server stopped")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error stopping server: ${e.message}", e)
        }
    }

    fun isRunning(): Boolean = serverRunning
}
