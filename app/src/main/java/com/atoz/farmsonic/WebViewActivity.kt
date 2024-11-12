package com.atoz.farmsonic

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.atoz.farmsonic.R

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnNavigate: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        sharedPreferences = getSharedPreferences("WifiSettings", Context.MODE_PRIVATE)
        val initip = sharedPreferences.getString("initip", "") ?: ""

        // Toolbar를 앱바로 설정
        val toolbar: Toolbar = findViewById(R.id.webView_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // WebView 초기화
        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(initip) // 초기 페이지 로드

        // 버튼 설정
        btnNavigate = findViewById(R.id.btnNavigate)
        btnNavigate.setOnClickListener {
            showConfirmationDialog() // 확인창 표시
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // 확인창을 표시하는 함수
    private fun showConfirmationDialog() {
        val resetip = sharedPreferences.getString("resetip", "") ?: ""
        val builder = AlertDialog.Builder(this)
        builder.setMessage("정말로 초기화 하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                // "예"를 선택하면 버튼을 숨기고 다음 페이지로 이동
                btnNavigate.visibility = View.GONE
                webView.loadUrl(resetip)
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss() // "아니오"를 선택하면 다이얼로그 닫기
            }

        // 다이얼로그 표시
        val alertDialog = builder.create()
        alertDialog.show()
    }
}
