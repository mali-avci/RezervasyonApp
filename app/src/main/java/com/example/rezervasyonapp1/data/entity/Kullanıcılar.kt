package com.example.rezervasyonapp1.data.entity

import java.io.Serializable

data class Kullanicilar(
    var kullanici_id: String = "",
    var kullanici_ad: String = "",
    var sifre: String = "",
    var email: String = "", // Login ve Auth işlemleri için email alanı eklendi
    var isAdmin: Boolean = false // Yetki kontrolü için
) : Serializable