package org.fossify.messages.api

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class ApiServer(
    private val context: Context,
    private val port: Int = 8080
) {
    
    private var serverSocket: ServerSocket? = null
    private var serverRunning = false
    private val executor = Executors.newFixedThreadPool(5)
    private val tag = "ApiServer"

    fun startServer(): Boolean {
        return try {
            serverSocket = ServerSocket(port)
            serverRunning = true
            Log.d(tag, "API Server started on port $port")
            
            // Start accepting connections in background
            executor.execute {
                while (serverRunning && !Thread.currentThread().isInterrupted) {
                    try {
                        val socket = serverSocket?.accept()
                        if (socket != null) {
                            executor.execute { handleClient(socket) }
                        }
                    } catch (e: Exception) {
                        if (serverRunning) {
                            Log.e(tag, "Error accepting connection: ${e.message}")
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start API server: ${e.message}", e)
            serverRunning = false
            false
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(socket.getInputStream().bufferedReader())
            val writer = PrintWriter(socket.getOutputStream(), true)
            
            // Read HTTP request line
            val requestLine = reader.readLine() ?: return
            Log.d(tag, "Request: $requestLine")
            
            val response = when {
                requestLine.contains("/api/status") -> statusResponse()
                requestLine.contains("/api/send-sms") -> smsResponse()
                else -> notFoundResponse()
            }
            
            writer.print(response)
            writer.flush()
            socket.close()
        } catch (e: Exception) {
            Log.e(tag, "Error handling client: ${e.message}")
        }
    }

    private fun statusResponse(): String {
        val body = """{"status":"running","port":$port}"""
        return httpResponse(200, "OK", body, "application/json")
    }

    private fun smsResponse(): String {
        val body = """{"status":"queued"}"""
        return httpResponse(200, "OK", body, "application/json")
    }

    private fun notFoundResponse(): String {
        val body = """{"error":"endpoint not found"}"""
        return httpResponse(404, "Not Found", body, "application/json")
    }

    private fun httpResponse(code: Int, status: String, body: String, contentType: String): String {
        return """HTTP/1.1 $code $status
Content-Type: $contentType
Content-Length: ${body.length}
Connection: close

$body
"""
    }

    fun stopServer() {
        try {
            serverRunning = false
            serverSocket?.close()
            executor.shutdown()
            Log.d(tag, "API Server stopped")
        } catch (e: Exception) {
            Log.e(tag, "Error stopping server: ${e.message}", e)
        }
    }

    fun isRunning(): Boolean = serverRunning
}
