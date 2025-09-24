package com.example.guardianconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnSOS: Button
    private lateinit var btnManage: Button
    private lateinit var tvStatus: TextView

    private val permissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    )

    private val requestPerms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        // nothing extra
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSOS = findViewById(R.id.btnSOS)
        btnManage = findViewById(R.id.btnManageContacts)
        tvStatus = findViewById(R.id.tvStatus)

        btnSOS.setOnClickListener { triggerSOS() }
        btnManage.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }

        checkAndAskPermissions()
    }

    private fun checkAndAskPermissions() {
        val missing = permissions.filter { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPerms.launch(missing.toTypedArray())
    }

    private fun triggerSOS() {
        tvStatus.text = "Status: SOS triggered"
        // start foreground service
        val svc = Intent(this, SOSService::class.java)
        ContextCompat.startForegroundService(this, svc)

        // call primary guardian
        val primary = ContactsManager.getPrimaryGuardian(this)
        primary?.let {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${it.phone}"))
                startActivity(callIntent)
            } else {
                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                startActivity(dial)
            }
        }

        // send SMS to all guardians with last known location
        val loc = LocationHelper.getLastKnownLocationSync(this)
        val mapLink = loc?.let { "https://maps.google.com/?q=${it.latitude},${it.longitude}" } ?: "Location unavailable"
        val text = "${ContactsManager.getOwnerName(this) ?: "Someone"} needs help! Live: $mapLink"
        ContactsManager.getAllGuardians(this).forEach { g -> sendSms(g.phone, text) }
    }

    private fun sendSms(number: String, text: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return
        try {
            SmsManager.getDefault().sendTextMessage(number, null, text, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
