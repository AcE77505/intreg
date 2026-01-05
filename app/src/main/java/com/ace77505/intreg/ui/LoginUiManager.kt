package com.ace77505.intreg.ui

import android.os.Build
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ace77505.intreg.AppConfig
import com.ace77505.intreg.R

class LoginUiManager(private val activity: AppCompatActivity) {

    // 视图引用
    lateinit var usernameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var login200Button: Button
    lateinit var login252Button: Button
    lateinit var queryBalanceButton: Button
    lateinit var resultTextView: TextView
    lateinit var additionalMessagesTextView: TextView
    lateinit var leftFlowTextView: TextView
    lateinit var balanceTextView: TextView
    lateinit var autoQueryCheckBox: CheckBox
    lateinit var progressBar: ProgressBar
    lateinit var queryProgressBar: ProgressBar

    // 添加缓存字段
    private var lastUiScale = -1f
    private var lastTextScale = -1f

    fun initViews() {
        usernameEditText = activity.findViewById(R.id.usernameEditText)
        passwordEditText = activity.findViewById(R.id.passwordEditText)
        login200Button = activity.findViewById(R.id.login200Button)
        login252Button = activity.findViewById(R.id.login252Button)
        queryBalanceButton = activity.findViewById(R.id.queryBalanceButton)
        resultTextView = activity.findViewById(R.id.resultTextView)
        additionalMessagesTextView = activity.findViewById(R.id.additionalMessagesTextView)
        leftFlowTextView = activity.findViewById(R.id.leftFlowTextView)
        balanceTextView = activity.findViewById(R.id.balanceTextView)
        autoQueryCheckBox = activity.findViewById(R.id.autoQueryCheckBox)
        progressBar = activity.findViewById(R.id.progressBar)
        queryProgressBar = activity.findViewById(R.id.queryProgressBar)
    }

    fun setupUiScaling() {
        // 检查缩放值是否真正变化
        if (lastUiScale == AppConfig.uiScale && lastTextScale == AppConfig.textScale) {
            return
        }

        lastUiScale = AppConfig.uiScale
        lastTextScale = AppConfig.textScale

        val scale = AppConfig.uiScale
        val density = activity.resources.displayMetrics.density

        // 调整按钮高度
        val baseButtonHeight = 48
        val pixelHeight = (baseButtonHeight * density * scale).toInt()

        login200Button.layoutParams.height = pixelHeight
        login252Button.layoutParams.height = pixelHeight
        queryBalanceButton.layoutParams.height = pixelHeight

        // 调整文字大小
        val textSize = 16f * AppConfig.textScale
        usernameEditText.textSize = textSize
        passwordEditText.textSize = textSize
        login200Button.textSize = textSize
        login252Button.textSize = textSize
        queryBalanceButton.textSize = textSize
        resultTextView.textSize = textSize
        additionalMessagesTextView.textSize = 14f * AppConfig.textScale
        leftFlowTextView.textSize = 14f * AppConfig.textScale
        balanceTextView.textSize = 14f * AppConfig.textScale
        autoQueryCheckBox.textSize = 16f * AppConfig.textScale

        // 强制重新布局
        login200Button.post {
            login200Button.requestLayout()
            login252Button.requestLayout()
            queryBalanceButton.requestLayout()
        }
    }

    fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        login200Button.isEnabled = !loading
        login252Button.isEnabled = !loading
        queryBalanceButton.isEnabled = !loading
    }

    fun showQueryLoading(loading: Boolean) {
        queryProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
        queryBalanceButton.isEnabled = !loading
    }

    fun showResult(message: String, isError: Boolean = false, isNeutral: Boolean = false) {
        resultTextView.text = message

        val color = when {
            isError -> getColorCompat(android.R.color.holo_red_dark)
            isNeutral -> getColorCompat(android.R.color.black)
            else -> getColorCompat(android.R.color.holo_green_dark)
        }
        resultTextView.setTextColor(color)
    }

    fun updateBalanceDisplay(leftFlow: String, balance: String) {
        leftFlowTextView.text = "剩余流量: $leftFlow"
        balanceTextView.text = "余额: $balance"

        val normalColor = getColorCompat(android.R.color.black)
        val successColor = getColorCompat(android.R.color.holo_green_dark)

        leftFlowTextView.setTextColor(
            if (leftFlow == "正在查询" || leftFlow == "请输入账号" || leftFlow == "查询失败" || leftFlow == "未知")
                normalColor else successColor
        )

        balanceTextView.setTextColor(
            if (balance == "正在查询" || balance == "请输入账号" || balance == "查询失败" || balance == "未知")
                normalColor else successColor
        )
    }

    fun getUsername(): String = usernameEditText.text.toString()
    fun getPassword(): String = passwordEditText.text.toString()

    fun setUsername(username: String) {
        usernameEditText.setText(username)
    }

    fun setPassword(password: String) {
        passwordEditText.setText(password)
    }

    fun setAutoQueryChecked(checked: Boolean) {
        autoQueryCheckBox.isChecked = checked
    }

    fun setAdditionalMessage(message: String) {
        additionalMessagesTextView.text = message
    }

    fun focusUsername() {
        usernameEditText.requestFocus()
    }

    /**
     * 兼容低版本的获取颜色方法
     */
    private fun getColorCompat(colorResId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getColor(colorResId)
        } else {
            // 使用 ContextCompat 来兼容低版本
            ContextCompat.getColor(activity, colorResId)
        }
    }
}