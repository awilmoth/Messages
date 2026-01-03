package org.fossify.messages.api

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ApiService : Service() {
    
    private var apiServer: ApiServer? = null
    private val tag = "ApiService"
    private val apiPort = 8080

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "ApiService created - PID: ${android.os.Process.myPid()}")
        Log.d(tag, "Debug build - extra logging enabled")
        
        apiServer = ApiServer(
            context = this,
            port = apiPort
        )
        
        val started = apiServer?.startServer() ?: false
        if (started) {
            Log.d(tag, "API Server started successfully on port $apiPort")
            Log.d(tag, "Service ready to accept connections from WireGuard (10.0.0.1)")
        } else {
            Log.e(tag, "Failed to start API Server")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand called - Service should stay running")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "ApiService destroying")
        apiServer?.stopServer()
        apiServer = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
// rebuild 1767469040
