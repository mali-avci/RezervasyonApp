package com.example.rezervasyonapp1.data.repo

import com.example.rezervasyonapp1.data.entity.Otobus
import com.example.rezervasyonapp1.data.entity.Ucak
import com.google.firebase.firestore.FirebaseFirestore

class AracRepository {
    private val db = FirebaseFirestore.getInstance()

    // Araç Ekleme
    fun aracKaydet(plaka: String, veri: Map<String, Any>) {
        db.collection("araclar").document(plaka).set(veri)
    }

    // Araçları Listeleme (Spinner için)
    fun araclariGetir(tip: String, callback: (List<Any>) -> Unit) {
        db.collection("araclar")
            .whereEqualTo("tip", tip)
            .get()
            .addOnSuccessListener { snapshot ->
                if (tip == "OTOBUS") {
                    val liste = snapshot.toObjects(Otobus::class.java)
                    callback(liste)
                } else {
                    val liste = snapshot.toObjects(Ucak::class.java)
                    callback(liste)
                }
            }
    }
}