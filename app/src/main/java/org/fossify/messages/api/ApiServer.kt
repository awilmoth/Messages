package org.fossify.messages.api

import android.content.Context
import android.util.Log

class ApiServer(
    private val context: Context,
    private val port: Int = 8080
) {
    private val tag = "ApiServer"
    
    fun startServer(): Boolean {
        Log.d(tag, "API Server initialized on port $port")
        return true
    }
    
    fun stopServer() {
        Log.d(tag, "API Server stopped")
    }
}
