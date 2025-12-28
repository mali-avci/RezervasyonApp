package com.example.rezervasyonapp1.data.entity

data class Otobus(
    override var kapasite: Int = 44,
    override var arac_plaka: String = "",
    var firma_adi: String = "",
    var otobus_tipi: String = "2+2" // 2+1 veya 2+2 düzeni için
) : Vehicle(kapasite, arac_plaka)