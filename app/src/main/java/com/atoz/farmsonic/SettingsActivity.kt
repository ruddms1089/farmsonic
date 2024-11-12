package com.atoz.farmsonic

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.atoz.farmsonic.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var ssidEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var initipEditText: EditText
    private lateinit var resetipEditText: EditText
    private lateinit var serveripEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ssidEditText = findViewById(R.id.ssidEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        initipEditText = findViewById(R.id.initipEditText)
        resetipEditText = findViewById(R.id.resetipEditText)
        serveripEditText = findViewById(R.id.serveripEditText)

        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("WifiSettings", Context.MODE_PRIVATE)

        // 기존 저장된 값을 EditText에 설정
        ssidEditText.setText(sharedPreferences.getString("ssid", ""))
        passwordEditText.setText(sharedPreferences.getString("password", ""))
        initipEditText.setText(sharedPreferences.getString("initip", ""))
        resetipEditText.setText(sharedPreferences.getString("resetip", ""))
        serveripEditText.setText(sharedPreferences.getString("serverip",""))

        // 저장 버튼 클릭 시 SharedPreferences에 저장
        saveButton.setOnClickListener {
            val ssid = ssidEditText.text.toString()
            val password = passwordEditText.text.toString()
            val initip = initipEditText.text.toString()
            val resetip = resetipEditText.text.toString()
            val serverip = serveripEditText.text.toString()

            if (ssid.isNotEmpty() && password.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("ssid", ssid)
                editor.putString("password", password)
                editor.putString("initip",initip)
                editor.putString("resetip",resetip)
                editor.putString("serverip",serverip)
                editor.apply()

                Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish() // 설정 후 액티비티 종료
            } else {
                Toast.makeText(this, "SSID와 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}
