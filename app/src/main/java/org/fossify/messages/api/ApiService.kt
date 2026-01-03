package org.fossify.messages.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class ApiService : Service() {
    
    private var apiServer: ApiServer? = null
    private val tag = "ApiService"
    private val apiPort = 8080
    private val notificationId = 1001
    private val channelId = "api_service_channel"

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "ApiService created - PID: ${android.os.Process.myPid()}")
        Log.d(tag, "Debug build - extra logging enabled")
        
        createNotificationChannel()
        startForeground(notificationId, createNotification())
        
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
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "API Service"
            val descriptionText = "SMS Bridge API Server"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SMS Bridge API")
            .setContentText("API server running on port $apiPort")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
// rebuild 1767469040
