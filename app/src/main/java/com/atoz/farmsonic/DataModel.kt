package com.atoz.farmsonic

class DataModel {
    data class Wifi(
        val ssid: String,
        val level: Int, // 신호 세기
        val capabilities: String // 보안 방식 등
    )
}
