package com.atoz.farmsonic;

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private val list = ArrayList<DataModel.Wifi>()
    private val adapter by lazy { ApListAdapter(this, list) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var wifiManager: WifiManager
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SharedPreferences 초기화
        var sharedPreferences = getSharedPreferences("WifiSettings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 키가 존재하지 않을 경우 기본값 설정
        if (!sharedPreferences.contains("ssid")) { editor.putString("ssid", "FARM") }
        if (!sharedPreferences.contains("password")) { editor.putString("password", "admin1234") }
        if (!sharedPreferences.contains("initip")) { editor.putString("initip", "192.168.1.1") }
        if (!sharedPreferences.contains("resetip")) { editor.putString("resetip", "192.168.1.1/reset") }
        if (!sharedPreferences.contains("serverip")) { editor.putString("serverip","106.245.78.214:8080/farmdata")}
        editor.apply()

        // ActionBar의 기본 제목 숨기기
        supportActionBar?.setDisplayShowTitleEnabled(false)

        progressBar = findViewById(R.id.progressBar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // BroadcastReceiver 등록
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, filter)

        // Wi-Fi 스캔 시작
        startWifiScan()

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        // 메뉴 버튼 설정
        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END)
            } else {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_info -> {
                    val intent = Intent(this, InfoWebViewActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_refresh -> {
                    //Toast.makeText(this, "리스트가 새로 고침되었습니다.", Toast.LENGTH_SHORT).show()
                    startWifiScan() // 데이터 새로고침 함수 호출
                    true
                }
                R.id.action_settings -> {
//                    Toast.makeText(this, "설정 클릭됨", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivityForResult(intent, 100)
                    true
                }
                R.id.action_exit -> {
                    AlertDialog.Builder(this)
                        .setTitle("종료")
                        .setMessage("앱을 종료하시겠습니까?")
                        .setPositiveButton("예") { _, _ ->
                            finishAffinity() // 앱 종료
                        }
                        .setNegativeButton("아니오", null) // 취소
                        .show()
                    true
                }

            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

    }

    //Wi-Fi 스캔 기능
    private fun startWifiScan() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            progressBar.visibility = View.VISIBLE
            wifiManager.startScan() // 새로운 스캔 시작
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION )
            Toast.makeText(this, "Location permission required for Wi-Fi scanning", Toast.LENGTH_SHORT).show()
        }
    }

    // Wi-Fi 스캔 완료 시 호출되는 BroadcastReceiver
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                progressBar.visibility = View.GONE
                // 스캔 결과를 바탕으로 UI 업데이트
                val dataModelList = getAvailableAPs(context)
                val arrayList = ArrayList(dataModelList)
                recyclerView.adapter = ApListAdapter(context, arrayList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // 설정이 완료되었음을 확인하고 처리할 작업 수행
            startWifiScan()
            Log.d("MainActivity", "Settings saved")
        }
    }

    private fun getAvailableAPs(context: Context): List<DataModel.Wifi> {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dataModelList = mutableListOf<DataModel.Wifi>()

        val sharedPreferences = context.getSharedPreferences("WifiSettings", Context.MODE_PRIVATE)
        val savedSsid = sharedPreferences.getString("ssid", "") ?: ""

        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val results = wifiManager.scanResults
            for (result in results) {
                val ssid = result.SSID ?: "Unknown SSID"
                if (ssid.startsWith(savedSsid)) {
                    dataModelList.add(DataModel.Wifi(ssid, result.level, result.capabilities ?: ""))
                    Log.d("KIMKM", "SSID: ${ssid}, BSSID: ${result.BSSID}, Strength: ${result.level}")
                }
            }
        } else {
            Toast.makeText(context, "Location permission required for Wi-Fi scanning", Toast.LENGTH_SHORT).show()
        }
        return dataModelList
    }

    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용 시 와이파이 스캔 수행
                startWifiScan()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver) // BroadcastReceiver 해제
    }

//    private fun requestLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
//            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION )
//        }
//    }

}