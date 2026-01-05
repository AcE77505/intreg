package com.ace77505.intreg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LoginService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val LOGIN_RESULT_REGEX = Regex("""dr1003\(\{"result":(\d+)""")
    private val JSON_EXTRACT_REGEX = Regex("""dr1002\((\{.*\})\)""")
    private val LEFT_FLOW_REGEX = Regex(""""left_flow"\\s*:\\s*([^,}]+)""")
    private val BALANCE_REGEX = Regex(""""balance"\\s*:\\s*([^,}]+)""")


    suspend fun login(ip: String, username: String, password: String): String {
        return try {
            val url = "http://$ip/drcom/login?callback=dr1003&DDDDD=$username&upass=$password&0MKKey=123456&R1=0&R2=&R3=0&R6=0&para=00&v6ip=&terminal_type=1&lang=zh-cn&jsVersion=4.1.3&v=6994&lang=zh"

            println("尝试登录: $url")

            val request = Request.Builder()
                .url(url)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .header("Referer", "http://$ip/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            println("响应状态: ${response.code}")
            val responseBody = response.body.string()
            println("响应内容: $responseBody")

            if (response.isSuccessful) {
                val match = LOGIN_RESULT_REGEX.find(responseBody)

                if (match != null) {
                    val resultCode = match.groupValues[1]
                    if (resultCode == "1") "登录成功" else "登录失败"
                } else {
                    "响应格式错误"
                }
            } else {
                "网络错误: ${response.code}"
            }
        } catch (e: Exception) {
            println("登录异常: ${e.message}")
            when (e) {
                is java.net.SocketTimeoutException -> "登录超时"
                is java.net.UnknownHostException -> "无法连接服务器"
                else -> "登录失败: ${e.message}"
            }
        }
    }

    suspend fun queryBalance(username: String): Map<String, String> {
        return try {
            val url = "http://192.168.4.252:801/eportal/portal/page/basic_information?callback=dr1002&lang=zh-cn&program_index=OwLQBU1636080080&page_index=zqULIS1636080177&user_account=$username&wlan_user_ip=168310874&wlan_user_mac=000000000000&jsVersion=4.1.3&v=2373&lang=zh"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "*/*")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val responseBody = response.body.string()

                // 方法1: JSON解析
                try {
                    val jsonMatch = JSON_EXTRACT_REGEX.find(responseBody)
                    if (jsonMatch != null) {
                        val jsonString = jsonMatch.groupValues[1]
                        val jsonObject = JSONObject(jsonString)
                        if (jsonObject.getInt("code") == 1) {
                            val data = jsonObject.getJSONObject("data")
                            val leftFlow = data.optString("left_flow", "未知")
                            val balance = data.optString("balance", "未知")
                            return mapOf(
                                "left_flow" to leftFlow,
                                "balance" to balance
                            )
                        }
                    }
                } catch (_: Exception) {
                    // JSON解析失败，使用正则表达式
                }

                // 方法2: 正则表达式
                var leftFlow = "未知"
                var balance = "未知"

                val leftFlowMatch = LEFT_FLOW_REGEX.find(responseBody)
                val balanceMatch = BALANCE_REGEX.find(responseBody)

                if (leftFlowMatch != null) {
                    leftFlow = leftFlowMatch.groupValues[1].trim().replace("\"", "").replace("}", "")
                }

                if (balanceMatch != null) {
                    balance = balanceMatch.groupValues[1].trim().replace("\"", "").replace("}", "")
                }

                return mapOf(
                    "left_flow" to leftFlow,
                    "balance" to balance
                )
            } else {
                mapOf("error" to "网络错误: ${response.code}")
            }
        } catch (e: Exception) {
            when (e) {
                is java.net.SocketTimeoutException -> mapOf("error" to "查询超时")
                else -> mapOf("error" to "查询失败")
            }
        }
    }
}