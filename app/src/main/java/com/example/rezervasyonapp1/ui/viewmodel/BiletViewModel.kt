package com.example.rezervasyonapp1.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rezervasyonapp1.data.entity.Biletler
import com.example.rezervasyonapp1.data.repo.BiletRepository

class BiletViewModel : ViewModel() {
    private val brepo = BiletRepository()
    val biletlerListesi = MutableLiveData<List<Biletler>>()

    fun yukle() {
        brepo.biletlerimiGetir { biletlerListesi.value = it }
    }

    fun biletAl(seferId: String, koltukNo: Int) = brepo.biletOlustur(seferId, koltukNo)

    fun biletIptal(seferId: String, koltukNo: Int) = brepo.biletSil(seferId, koltukNo)
}