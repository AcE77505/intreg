package com.ace77505.intreg.ui

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ace77505.intreg.AppConfig
import com.ace77505.intreg.R

class SettingsUiManager(private val activity: AppCompatActivity) {

    lateinit var uiScaleSeekBar: SeekBar
    lateinit var textScaleSeekBar: SeekBar
    lateinit var uiScaleValue: TextView
    lateinit var textScaleValue: TextView
    lateinit var resetButton: Button

    fun initViews() {
        uiScaleSeekBar = activity.findViewById(R.id.uiScaleSeekBar)
        textScaleSeekBar = activity.findViewById(R.id.textScaleSeekBar)
        uiScaleValue = activity.findViewById(R.id.uiScaleValue)
        textScaleValue = activity.findViewById(R.id.textScaleValue)
        resetButton = activity.findViewById(R.id.resetButton)
    }

    fun setupSeekBars() {
        // UI缩放
        val uiScaleProgress = ((AppConfig.uiScale - 0.5f) * 10).toInt()
        uiScaleSeekBar.progress = uiScaleProgress
        uiScaleValue.text = "当前缩放: ${"%.1f".format(AppConfig.uiScale)}x"

        uiScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = roundToSingleDecimal(0.5f + (progress / 10.0f))
                uiScaleValue.text = "当前缩放: ${"%.1f".format(scale)}x"
                AppConfig.setUiScale(scale, activity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 文字缩放
        val textScaleProgress = ((AppConfig.textScale - 0.8f) * 10).toInt()
        textScaleSeekBar.progress = textScaleProgress
        textScaleValue.text = "当前大小: ${"%.1f".format(AppConfig.textScale)}x"

        textScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = roundToSingleDecimal(0.8f + (progress / 10.0f))
                textScaleValue.text = "当前大小: ${"%.1f".format(scale)}x"
                AppConfig.setTextScale(scale, activity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun updateSeekBars() {
        // UI缩放: 1.0 对应的进度 = (1.0 - 0.5) * 10 = 5
        uiScaleSeekBar.progress = 5
        uiScaleValue.text = "当前缩放: 1.0x"

        // 文字缩放: 1.0 对应的进度 = (1.0 - 0.8) * 10 = 2
        textScaleSeekBar.progress = 2
        textScaleValue.text = "当前大小: 1.0x"
    }

    private fun roundToSingleDecimal(value: Float): Float {
        return (value * 10).toInt() / 10.0f
    }
}