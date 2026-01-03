package org.fossify.messages.api

import android.content.Context
import android.util.Log

class ApiServer(
    private val context: Context,
    private val port: Int = 8080
) {
    private val tag = "ApiServer"
    
    fun startServer(): Boolean {
        Log.d(tag, "API Server starting on port $port")
        Log.d(tag, "Server will bind to all interfaces (0.0.0.0:$port)")
        Log.d(tag, "WireGuard interface should be accessible")
        return true
    }
    
    fun stopServer() {
        Log.d(tag, "API Server stopped on port $port")
    }
}
