package com.ace77505.intreg

import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ace77505.intreg.ui.LoginUiManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    
    private lateinit var uiManager: LoginUiManager
    private lateinit var prefs: SharedPreferences
    private val coroutineScope = MainScope()
    private var lastSavedUsername: String? = null
    private var lastSavedPassword: String? = null
    private var autoQueryBalance = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // 初始化 UI 管理器
        uiManager = LoginUiManager(this)
        uiManager.initViews()
        
        prefs = getSharedPreferences("credentials", MODE_PRIVATE)
        AppConfig.initialize(this)
        
        setupViews()
        uiManager.setupUiScaling()
        loadCredentials()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        uiManager.login200Button.setOnClickListener { 
            performLogin("192.168.100.200")
        }
        
        uiManager.login252Button.setOnClickListener { 
            performLogin("192.168.4.252")
        }
        
        uiManager.queryBalanceButton.setOnClickListener { 
            queryBalance()
        }
        
        uiManager.resultTextView.setOnClickListener {
            copyResultToClipboard()
        }
        
        uiManager.autoQueryCheckBox.setOnCheckedChangeListener { _, isChecked ->
            autoQueryBalance = isChecked
            saveCredentials()
            if (isChecked && uiManager.getUsername().isNotEmpty()) {
                queryBalance()
            }
        }
        
        if (!prefs.contains("username") && uiManager.getUsername().isEmpty()) {
            uiManager.focusUsername()
        }
    }

    override fun onResume() {
        super.onResume()
        uiManager.setupUiScaling()
    }

    private fun loadCredentials() {
        val username = prefs.getString("username", "")
        val password = prefs.getString("password", "")
        autoQueryBalance = prefs.getBoolean("autoQueryBalance", false)
        
        uiManager.setUsername(username ?: "")
        uiManager.setPassword(password ?: "")
        lastSavedUsername = username
        lastSavedPassword = password
        uiManager.setAutoQueryChecked(autoQueryBalance)
        
        if (autoQueryBalance && username?.isNotEmpty() == true) {
            queryBalance()
        }
    }

    private fun saveCredentials() {
        val username = uiManager.getUsername()
        val password = uiManager.getPassword()

        prefs.edit {
            putString("username", username)
            putString("password", password)
            putBoolean("autoQueryBalance", autoQueryBalance)
        }
            
        lastSavedUsername = username
        lastSavedPassword = password
    }

    private fun shouldSaveCredentials(username: String, password: String): Boolean {
        return lastSavedUsername != username || lastSavedPassword != password
    }

    private fun performLogin(ip: String) {
        val username = uiManager.getUsername()
        val password = uiManager.getPassword()
        
        if (username.isEmpty() || password.isEmpty()) {
            uiManager.showResult("请输入账号和密码", isError = true)
            return
        }
        
        if (!isNetworkAvailable()) {
            uiManager.showResult("网络连接已断开", isError = true)
            return
        }
        
        uiManager.showLoading(true)
        uiManager.showResult("正在登录到 $ip...", isNeutral = true)
        uiManager.setAdditionalMessage("")
        
        coroutineScope.launch {
            val result = LoginService.login(ip, username, password)
            
            uiManager.showLoading(false)
            
            if (result == "登录成功") {
                uiManager.showResult(result, isError = false)
                
                if (shouldSaveCredentials(username, password)) {
                    saveCredentials()
                    uiManager.setAdditionalMessage("账号密码已保存")
                    if (autoQueryBalance) {
                        queryBalance()
                    }
                }
            } else {
                uiManager.showResult(result, isError = true)
                when {
                    result.contains("超时") -> uiManager.setAdditionalMessage("连接超时，请重试")
                    result.contains("无法连接") -> uiManager.setAdditionalMessage("无法连接到服务器")
                    result.contains("网络错误") -> uiManager.setAdditionalMessage("网络请求失败")
                    else -> uiManager.setAdditionalMessage("请检查账号密码和服务器地址")
                }
            }
        }
    }

    private fun queryBalance() {
        val username = uiManager.getUsername()
        
        if (username.isEmpty()) {
            uiManager.updateBalanceDisplay("请输入账号", "请输入账号")
            return
        }
        
        if (!isNetworkAvailable()) {
            uiManager.showResult("网络不可用", isError = true)
            return
        }
        
        uiManager.showQueryLoading(true)
        uiManager.updateBalanceDisplay("正在查询", "正在查询")
        
        coroutineScope.launch {
            val result = LoginService.queryBalance(username)
            
            uiManager.showQueryLoading(false)
            
            if (result.containsKey("error")) {
                uiManager.updateBalanceDisplay("查询失败", "查询失败")
                uiManager.showResult("查询失败: ${result["error"]}", isError = true)
            } else {
                uiManager.updateBalanceDisplay(
                    result["left_flow"] ?: "未知",
                    result["balance"] ?: "未知"
                )
                uiManager.showResult("查询完成", isError = false)
            }
        }
    }

    private fun copyResultToClipboard() {
        val resultText = uiManager.resultTextView.text.toString()
        if (resultText.isNotEmpty() && resultText != "等待登录") {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("登录结果", resultText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "已复制: $resultText", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                )
            } else {
                // 对于 Android 6.0 以下版本，使用已弃用但必要的 API
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                networkInfo != null && networkInfo.isConnected
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}