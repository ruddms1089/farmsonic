package com.atoz.farmsonic

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class ApListAdapter(private val context: Context, list: ArrayList<DataModel.Wifi>) :
    RecyclerView.Adapter<ApListAdapter.ViewHolder>() {

    private val mList = list

    private val sharedPreferences = context.getSharedPreferences("WifiSettings", Context.MODE_PRIVATE)
    private val savedPassword = sharedPreferences.getString("password", "") ?: ""


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ssid: TextView = itemView.findViewById(R.id.listItemSSID)
        private val level: ImageView = itemView.findViewById(R.id.listItemLevel)
        private val levelNumber: TextView = itemView.findViewById(R.id.listItemLevelNumber)
        private val cap: ImageView = itemView.findViewById(R.id.listItemWifiLock)
        private val freq: TextView = itemView.findViewById(R.id.listItemFreq)

        fun bind(dao: DataModel.Wifi) {
            ssid.text = dao.ssid

            dao.level?.let { levelValue ->
                level.setImageDrawable(parseLevel(levelValue))
                levelNumber.text = "$levelValue dBm" // 신호 세기를 dBm 단위로 텍스트 뷰에 설정
            } ?: run {
                level.visibility = View.GONE
                levelNumber.visibility = View.GONE // 신호 세기가 없을 경우 텍스트 뷰 숨김
            }

            dao.capabilities?.let {
                cap.visibility = if (isCapability(it)) View.VISIBLE else View.GONE
            } ?: run { cap.visibility = View.GONE }
        }

        // 비밀번호 유무
        fun isCapability(capabilities: String): Boolean {
            return capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains("WEP")
        }

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mList[position].ssid?.let { it1 -> connectToWifi(it1) }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int = mList.size

    private fun connectToWifi(ssid: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(savedPassword)
                .build()

            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val loadingDialog = showLoadingDialog()  // 로딩 다이얼로그 표시

            connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("WiFi", "Network connected successfully")
                    connectivityManager.bindProcessToNetwork(network)

                    // 다이얼로그 닫기
                    loadingDialog.dismiss()

                    // WebViewActivity로 이동
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.e("WiFi", "Network unavailable")
                    // 네트워크 연결 실패 시 다이얼로그 닫기
                    loadingDialog.dismiss()
                }
            })
        } else {
            connectToWifiForOlderDevices(ssid)
        }

    }

    private fun connectToWifiForOlderDevices(ssid: String) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfig = WifiConfiguration().apply {
            this.SSID = "\"$ssid\"" // 큰따옴표로 감싸야 함
            this.preSharedKey = "\"${savedPassword}\"" // 큰따옴표로 감싸야 함
            this.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        }

        val netId = wifiManager.addNetwork(wifiConfig)
        if (netId != -1) {
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
        } else {
            Log.e("WiFi", "Failed to add network configuration")
        }
    }


    // 로딩 다이얼로그를 위한 함수
    private fun showLoadingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = (context as Activity).layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    private fun parseLevel(level: Int): Drawable? {
        return ResourcesCompat.getDrawable(
            context.resources,
            when {
                level >= -30 -> { R.drawable.level_wifi_4 }
                level in -30 .. -60 -> { R.drawable.level_wifi_3 }
                level in -60 .. -70 -> { R.drawable.level_wifi_2 }
                else -> { R.drawable.level_wifi_1 }
            }, null
        )
    }

}
