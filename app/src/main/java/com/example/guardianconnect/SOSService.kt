package com.example.guardianconnect

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.util.*

class SOSService : Service() {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
        startRecordingAudio()
        // You can add location updates and additional features here (follow-me timer etc.)
    }

    private fun startForegroundServiceWithNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val chanId = "sos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(chanId, "SOS Service", NotificationManager.IMPORTANCE_HIGH))
        }
        val notif: Notification = NotificationCompat.Builder(this, chanId)
            .setContentTitle("Guardian Connect: SOS active")
            .setContentText("Sharing live location and audio")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
        startForeground(1, notif)
    }

    private fun startRecordingAudio() {
        try {
            val f = File(cacheDir, "sos_${UUID.randomUUID()}.aac")
            audioFile = f
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(f.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { recorder?.stop() } catch (e: Exception) {}
        recorder?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
