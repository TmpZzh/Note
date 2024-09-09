package com.example.note

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var logFile: File
    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initLogFile()

        logTextView = findViewById(R.id.logTextView)
        scrollView = findViewById(R.id.scrollView)

        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        val button5 = findViewById<Button>(R.id.button5)
        val button6 = findViewById<Button>(R.id.button6)
        val undoButton = findViewById<Button>(R.id.undoButton)
        val copyButton = findViewById<Button>(R.id.copyButton)

        button1.setOnClickListener { writeLog("碰撞风险-变道") }
        button2.setOnClickListener { writeLog("碰撞风险-cut in") }
        button3.setOnClickListener { writeLog("猛打方向") }
        button4.setOnClickListener { writeLog("异常退出自动驾驶") }
        button5.setOnClickListener { writeLog("异常刹车") }
//        button6.setOnClickListener { showInputDialog() }
        button6.setOnClickListener { showInputDialog(System.currentTimeMillis()) }
        undoButton.setOnClickListener { undoButton.setOnClickListener { showUndoConfirmationDialog() } }
        copyButton.setOnClickListener { copyLogToClipboard() }

        updateLogDisplay()
    }

    private fun initLogFile() {
        logFile = File(filesDir, "log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

//    private fun writeLog(message: String) {
//        try {
//            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
//            val logEntry = "$timestamp: $message\n"
//
//            FileWriter(logFile, true).use { writer ->
//                writer.append(logEntry)
//            }
//            updateLogDisplay()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            showToast("保存日志失败: ${e.message}")
//        }
//    }
    private fun writeLog(message: String, timestamp: Long = System.currentTimeMillis()) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val formattedTimestamp = dateFormat.format(Date(timestamp))
            val logEntry = "$formattedTimestamp: $message\n"

            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
            updateLogDisplay()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("保存日志失败: ${e.message}")
        }
    }

//    private fun showInputDialog() {
//        val input = EditText(this)
//        input.inputType = InputType.TYPE_CLASS_TEXT
//
//        AlertDialog.Builder(this)
//            .setTitle("输入日志内容")
//            .setView(input)
//            .setPositiveButton("确定") { _, _ ->
//                val logContent = input.text.toString()
//                if (logContent.isNotEmpty()) {
//                    writeLog("其他: $logContent")
//                } else {
//                    showToast("日志内容不能为空")
//                }
//            }
//            .setNegativeButton("取消", null)
//            .show()
//    }
    private fun showInputDialog(timestamp: Long) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("输入日志内容")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val logContent = input.text.toString()
                if (logContent.isNotEmpty()) {
                    writeLog(logContent, timestamp)
                } else {
                    showToast("日志内容不能为空")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showUndoConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认撤回")
            .setMessage("您确定要撤回最后一条日志吗？")
            .setPositiveButton("确定") { _, _ ->
                undoLastLog()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun undoLastLog() {
        try {
            val lines = logFile.readLines().toMutableList()
            if (lines.isNotEmpty()) {
                lines.removeAt(lines.size - 1)
                logFile.writeText(lines.joinToString("\n") + if (lines.isNotEmpty()) "\n" else "")
                showToast("最后一条日志已撤回")
                updateLogDisplay()
            } else {
                showToast("没有日志可以撤回")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("撤回日志失败: ${e.message}")
        }
    }

    private fun copyLogToClipboard() {
        try {
            val logContent = logFile.readText()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("日志内容", logContent)
            clipboard.setPrimaryClip(clip)
            showToast("日志内容已复制到剪贴板")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("复制日志失败: ${e.message}")
        }
    }

    private fun updateLogDisplay() {
        try {
            val logContent = logFile.readText()
            logTextView.text = logContent
            // 滚动到底部
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("读取日志失败: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_logs -> {
                clearLogs()
                true
            }
            R.id.add_custom_buttons -> {
//                TODO
                true
            }
            R.id.fault_code_analysis -> {
                parseFaultCodes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearLogs() {
        // 实现清空日志的逻辑
        AlertDialog.Builder(this)
            .setTitle("确认清空")
            .setMessage("您确定要清空所有日志吗？")
            .setPositiveButton("确定") { _, _ ->
                logFile.writeText("")
                updateLogDisplay()
                showToast("日志已清空")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun parseFaultCodes() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "请输入故障码"

        AlertDialog.Builder(this)
            .setTitle("故障码解析")
            .setView(input)
            .setPositiveButton("确认") { _, _ ->
                val inputText = input.text.toString()
                val number = inputText.toIntOrNull()
                if (number != null) {
                    analyzeFaultCode(number)
                } else {
                    showToast("请输入有效的数字")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun analyzeFaultCode(num: Int) {
        val faultCodes = listOf(
            0b1 to "HCU is not ready!",
            0b10 to "EPS is not ready!",
            0b100 to "IBC is not ready!",
            0b1000 to "No D Gear!",
            0b10000 to "StandStill!车辆静止",
            0b100000 to "Not Belt",
            0b1000000 to "Not Driver",
            0b10000000 to "Driver Intervention！驾驶员介入",
            0b100000000 to "Control Error！Control状态异常",
            0b1000000000 to "Control TimeStamp Error！Control时间戳异常",
            0b10000000000 to "AI Button is not pressed！ai按键未按下"
        )

        var faultCount = 0
        writeAnalysisResult("==========故障码解析结果==========")
        faultCodes.forEach { (mask, message) ->
            if (num and mask != 0) {
                writeAnalysisResult(message)
                faultCount++
            }
        }

        if (faultCount == 0) {
            writeAnalysisResult("No Error")
        } else {
            writeAnalysisResult("总共检测到 $faultCount 个故障")
        }
        writeAnalysisResult("==========故障码解析结果==========")
    }

    private fun writeAnalysisResult(message: String) {
        try {
            val logEntry = "$message\n"
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
            updateLogDisplay()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("保存解析结果失败: ${e.message}")
        }
    }

}
