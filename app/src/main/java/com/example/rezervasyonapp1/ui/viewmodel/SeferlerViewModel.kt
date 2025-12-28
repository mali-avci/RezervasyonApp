package com.example.rezervasyonapp1.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.data.repo.SeferlerRepository


class SeferlerViewModel : ViewModel() {
    val srepo = SeferlerRepository()
    val seferlerListesi: MutableLiveData<List<Seferler>> = srepo.seferlerListesi
    val anlikSefer = MutableLiveData<Seferler>()

    init {
        Log.d("SeferlerViewModel", "ViewModel oluşturuldu, LiveData referansı: ${seferlerListesi.hashCode()}")
    }

    fun seciliSeferiTakipEt(seferId: String) {
        // Repository'deki yeni canlı dinleme fonksiyonunu çağırıyoruz
        srepo.tekSeferiCanliDinle(seferId) { sefer ->
            anlikSefer.value = sefer // Artık 'sefer' null gelse bile hata vermez
        }
    }
    fun seferleriYukle() {
        Log.d("SeferlerViewModel", "seferleriYukle çağrıldı")
        srepo.tumSeferleriAl()
    }

    fun ara(kalkis: String, varis: String, tarih: String) {
        srepo.seferAra(kalkis, varis, tarih)
    }

    fun kaydet(sefer: Seferler) {
        srepo.seferKaydet(sefer)
    }
    // YENİ: Sefer bilgilerini (Örn: dolu_koltuklar) güncellemek için
    fun guncelle(sefer: Seferler) {
        srepo.seferGuncelle(sefer)
    }

    // YENİ: Sefer silmek için (Admin Paneli için kritik)
    fun sil(seferId: String) {
        srepo.seferSil(seferId)
    }
}