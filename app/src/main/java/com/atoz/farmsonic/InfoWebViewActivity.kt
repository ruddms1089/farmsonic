package com.atoz.farmsonic

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class InfoWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_web_view)

        sharedPreferences = getSharedPreferences("WifiSettings", Context.MODE_PRIVATE)
        val serverip = sharedPreferences.getString("serverip", "") ?: ""

        // SwipeRefreshLayout 설정
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()  // 웹뷰 새로고침
        }

        //val url = "http://106.245.78.214:8080/farmdata/"
        webView = findViewById(R.id.infoWebView)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 새로고침 완료 후 SwipeRefreshLayout의 로딩 종료
                swipeRefreshLayout.isRefreshing = false
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(serverip)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.bindProcessToNetwork(null)  // 이전 네트워크 연결 해제


//        val networkReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                val networkInfo = connectivityManager.activeNetworkInfo
//                if (networkInfo != null && networkInfo.isConnected) {
//                    webView.clearCache(true)
//                    webView.reload()  // 네트워크가 연결되었을 때 웹뷰 새로고침
//                }
//            }
//        }
    }
}
