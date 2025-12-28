package com.example.rezervasyonapp1.data.repo

import com.example.rezervasyonapp1.data.entity.Biletler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BiletRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Kullanıcının sadece kendi biletlerini getirmesi için
    fun biletlerimiGetir(callback: (List<Biletler>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("biletler")
            .whereEqualTo("kullanici_id", uid)
            .addSnapshotListener { value, _ ->
                val liste = value?.toObjects(Biletler::class.java) ?: listOf()
                callback(liste)
            }
    }

    // Yeni bilet ekleme
    fun biletOlustur(seferId: String, koltukNo: Int) {
        val uid = auth.currentUser?.uid ?: return
        val yeniBilet = Biletler(
            kullanici_id = uid,
            sefer_id = seferId,
            koltuk_no = koltukNo
        )
        db.collection("biletler").add(yeniBilet)
    }

    // Bilet İptali
    fun biletSil(seferId: String, koltukNo: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("biletler")
            .whereEqualTo("sefer_id", seferId)
            .whereEqualTo("koltuk_no", koltukNo)
            .whereEqualTo("kullanici_id", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot) { doc.reference.delete() }
            }
    }
}