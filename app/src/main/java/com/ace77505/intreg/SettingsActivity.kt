package com.ace77505.intreg

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ace77505.intreg.ui.SettingsUiManager
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var uiManager: SettingsUiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 初始化 UI 管理器
        uiManager = SettingsUiManager(this)
        uiManager.initViews()

        setupViews()
        uiManager.setupSeekBars()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        uiManager.resetButton.setOnClickListener {
            resetToDefault()
        }
    }

    private fun resetToDefault() {
        AppConfig.resetToDefault(this)
        uiManager.updateSeekBars()
        Toast.makeText(this, "已重置为默认设置", Toast.LENGTH_SHORT).show()
    }
}