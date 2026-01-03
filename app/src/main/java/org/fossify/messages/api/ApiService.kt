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
        Log.d(tag, "ApiService created")
        
        apiServer = ApiServer(
            context = this,
            port = apiPort
        )
        
        val started = apiServer?.startServer() ?: false
        if (started) {
            Log.d(tag, "API Server started successfully on port $apiPort")
        } else {
            Log.e(tag, "Failed to start API Server")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand called")
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
