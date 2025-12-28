package com.example.rezervasyonapp1.data.entity
import java.io.Serializable

data class Biletler(
    var bilet_id: String = "",
    var kullanici_id: String = "",
    var sefer_id: String = "",
    var koltuk_no: Int = 0,
    var fiyat: Double = 0.0
) : Serializable