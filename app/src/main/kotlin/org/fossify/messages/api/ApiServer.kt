package org.fossify.messages.api

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class ApiServer(
    private val context: Context,
    private val port: Int = 8080
) {
    private val tag = "ApiServer"
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newFixedThreadPool(5)
    private var isRunning = false
    
    fun startServer(): Boolean {
        return try {
            serverSocket = ServerSocket(port, 50, null) // Bind to all interfaces (0.0.0.0)
            isRunning = true
            Log.d(tag, "API Server started on 0.0.0.0:$port")
            
            executor.execute {
                acceptConnections()
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start server: ${e.message}", e)
            false
        }
    }
    
    private fun acceptConnections() {
        while (isRunning) {
            try {
                val clientSocket = serverSocket?.accept() ?: break
                Log.d(tag, "Client connected from ${clientSocket.inetAddress}")
                executor.execute {
                    handleClient(clientSocket)
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(tag, "Error accepting connection: ${e.message}")
                }
            }
        }
    }
    
    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)
            
            val requestLine = reader.readLine() ?: ""
            Log.d(tag, "Request: $requestLine")
            
            // Read headers
            while (true) {
                val line = reader.readLine()
                if (line.isNullOrBlank()) break
            }
            
            when {
                requestLine.contains("GET /api/status") -> {
                    writer.println("HTTP/1.1 200 OK")
                    writer.println("Content-Type: application/json")
                    writer.println()
                    writer.println("""{"status":"running","port":$port}""")
                }
                else -> {
                    writer.println("HTTP/1.1 404 Not Found")
                    writer.println("Content-Type: application/json")
                    writer.println()
                    writer.println("""{"error":"not found"}""")
                }
            }
            
            writer.flush()
            socket.close()
        } catch (e: Exception) {
            Log.e(tag, "Error handling client: ${e.message}", e)
        }
    }
    
    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
            executor.shutdown()
            Log.d(tag, "API Server stopped")
        } catch (e: Exception) {
            Log.e(tag, "Error stopping server: ${e.message}", e)
        }
    }
}
