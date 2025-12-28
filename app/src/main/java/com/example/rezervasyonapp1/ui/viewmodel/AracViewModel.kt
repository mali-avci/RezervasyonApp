package com.example.rezervasyonapp1.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rezervasyonapp1.data.repo.AracRepository

class AracViewModel : ViewModel() {
    private val arepo = AracRepository()

    // Araç listesini Fragment'a taşımak için LiveData
    val aracListesi = MutableLiveData<List<Any>>()

    fun aracEkle(plaka: String, veri: Map<String, Any>) {
        arepo.aracKaydet(plaka, veri)
    }

    fun araclariYukle(tip: String) {
        arepo.araclariGetir(tip) { liste ->
            aracListesi.value = liste
        }
    }
}