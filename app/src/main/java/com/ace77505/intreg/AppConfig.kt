package com.ace77505.intreg

import android.content.Context
import androidx.core.content.edit

object AppConfig {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_UI_SCALE = "ui_scale"
    private const val KEY_TEXT_SCALE = "text_scale"

    var uiScale: Float = 1.0f
        private set
    var textScale: Float = 1.0f
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        uiScale = roundToSingleDecimal(prefs.getFloat(KEY_UI_SCALE, 1.0f))
        textScale = roundToSingleDecimal(prefs.getFloat(KEY_TEXT_SCALE, 1.0f))
    }

    fun setUiScale(scale: Float, context: Context) {
        uiScale = roundToSingleDecimal(scale.coerceIn(0.5f, 2.0f))
        saveSettings(context)
    }

    fun setTextScale(scale: Float, context: Context) {
        textScale = roundToSingleDecimal(scale.coerceIn(0.8f, 2.0f))
        saveSettings(context)
    }

    private fun saveSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putFloat(KEY_UI_SCALE, uiScale)
                .putFloat(KEY_TEXT_SCALE, textScale)
        }
    }

    fun resetToDefault(context: Context) {
        uiScale = 1.0f
        textScale = 1.0f
        saveSettings(context)
    }

    // 修复浮点数精度问题
    private fun roundToSingleDecimal(value: Float): Float {
        return (value * 10).toInt() / 10.0f
    }
}