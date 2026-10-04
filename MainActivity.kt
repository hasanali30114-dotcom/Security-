package com.example.adapp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var logTextView: TextView

    // Optional device lock from the supplied instructions.
    // Keep false for a normal build. Set true and replace the ID before enabling it.
    private val ENFORCE_DEVICE_LOCK = false
    private val MY_SECURE_DEVICE_ID = "dummy_id_replace_me"

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            addLog(if (granted) "Notification permission granted." else "Notification permission not granted.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        logTextView = findViewById(R.id.logTextView)
        logTextView.text = ""

        requestNotificationPermissionIfNeeded()
        verifyDeviceIfRequired()
        checkNetworkAndStartDownload()
    }

    override fun onResume() {
        super.onResume()
        if (::statusText.isInitialized) {
            checkNetworkAndStartDownload()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun verifyDeviceIfRequired(): Boolean {
        @Suppress("DEPRECATION")
        val currentDeviceId = Secure.getString(contentResolver, Secure.ANDROID_ID)

        if (!ENFORCE_DEVICE_LOCK) {
            addLog("Device lock is disabled in this build.")
            addLog("Android ID: $currentDeviceId")
            return true
        }

        if (MY_SECURE_DEVICE_ID == "dummy_id_replace_me") {
            addLog("ALERT: Copy this Android ID into MY_SECURE_DEVICE_ID if you want device lock.")
            addLog("Your Android ID: $currentDeviceId")
            statusText.text = "Device lock is not configured"
            return false
        }

        return if (currentDeviceId == MY_SECURE_DEVICE_ID) {
            addLog("Device verified.")
            true
        } else {
            addLog("ACCESS DENIED: Unauthorized device.")
            Toast.makeText(this, "System Locked!", Toast.LENGTH_LONG).show()
            statusText.text = "Access denied"
            false
        }
    }

    private fun checkNetworkAndStartDownload() {
        if (!verifyDeviceIfRequired()) return

        when {
            !NetworkHelper.isNetworkAvailable(this) -> {
                statusText.text = "Internet is OFF"
                addLog("Internet is OFF.")
                promptMobileData()
            }
            !NetworkHelper.isWifiConnected(this) -> {
                statusText.text = "Wi-Fi is OFF"
                addLog("Wi-Fi is OFF. Mobile data is available.")
                promptMobileData()
            }
            else -> {
                statusText.text = "Wi-Fi connected — starting download"
                addLog("Wi-Fi connected.")
                startBackgroundDownload()
            }
        }
    }

    private fun promptMobileData() {
        AlertDialog.Builder(this)
            .setTitle("Network Alert")
            .setMessage("Wi-Fi is not available. Open network settings and turn on Mobile Data if needed.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun startBackgroundDownload() {
        val videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4"
        val fileName = "AD_Auto_Video_${System.currentTimeMillis()}.mp4"

        val serviceIntent = Intent(this, ADBackgroundService::class.java).apply {
            action = ADBackgroundService.ACTION_DOWNLOAD
            putExtra(ADBackgroundService.EXTRA_URL, videoUrl)
            putExtra(ADBackgroundService.EXTRA_FILE_NAME, fileName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }

        addLog("Download queued: $fileName")
        addLog("Destination: Movies/A_D_Downloads/")
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        val old = logTextView.text?.toString().orEmpty()
        logTextView.text = "$old[$time] $message\n"
    }
}
