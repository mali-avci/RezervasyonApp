package com.example.rezervasyonapp1.data.entity

data class Ucak(
    override var kapasite: Int = 180,
    override var arac_plaka: String = "",
    var havayolu_sirketi: String = "",
    var ucak_modeli: String = ""
) : Vehicle(kapasite, arac_plaka)