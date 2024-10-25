package com.dicoding.dicodingevent.ui.setting

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.databinding.ActivitySettingBinding
import com.dicoding.dicodingevent.ui.adapter.EventWorker
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class SettingActivity : AppCompatActivity() {
    private lateinit var workManager: WorkManager
    private lateinit var binding: ActivitySettingBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    getString(R.string.notifications_permission_granted), Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.notifications_permission_rejected), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        workManager = WorkManager.getInstance(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(
                ContextCompat.getDrawable(
                    this@SettingActivity,
                    R.drawable.arrow_back
                )
            )
            title = getString(R.string.setting_title)
        }

        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        val switchTheme = findViewById<SwitchMaterial>(R.id.switch_theme)
        val switchNotifications = findViewById<SwitchMaterial>(R.id.switch_notifications)

        val pref = SettingPreferences.getInstance(application.dataStore)
        val settingsViewModel =
            ViewModelProvider(this, SettingViewModelFactory(pref))[SettingsViewModel::class.java]

        settingsViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            AppCompatDelegate.setDefaultNightMode(if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            switchTheme.isChecked = isDarkModeActive
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveThemeSetting(isChecked)
        }

        settingsViewModel.getNotificationSettings().observe(this) { isNotificationActive: Boolean ->
            switchNotifications.isChecked = isNotificationActive
        }

        settingsViewModel.getNotificationSettings().observe(this) { isNotificationActive: Boolean ->
            switchNotifications.isChecked = isNotificationActive
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveNotificationSetting(isChecked)
            if (isChecked) {
                schedulePeriodicEventNotification()
            } else {
                cancelPeriodicTask()
            }
        }
    }

    private fun schedulePeriodicEventNotification() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(EventWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "EventNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun cancelPeriodicTask() {
        workManager.cancelUniqueWork("EventNotificationWork")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
