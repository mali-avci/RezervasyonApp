package com.example.rezervasyonapp1.data.entity
import java.io.Serializable

abstract class Vehicle(
    open var kapasite: Int = 0,
    open var arac_plaka: String = ""
) : Serializable