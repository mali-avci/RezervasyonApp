package com.example.rezervasyonapp1.data.entity

import java.io.Serializable

data class Seferler(
    var sefer_id: String = "",
    var kalkis_yeri: String = "",
    var varis_yeri: String = "",
    var tarih: String = "",
    var saat: String = "",
    var fiyat: Double = 0.0,
    var arac_tipi: String = "OTOBUS", // "OTOBUS" veya "UCAK"
    var dolu_koltuklar: String = "",
    // Araç detaylarını Firestore'da iç içe (nested) saklıyoruz
    var otobus_detay: Otobus? = null,
    var ucak_detay: Ucak? = null
) : Serializable