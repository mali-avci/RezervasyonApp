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

    // Bilet İptali (sefer_id ve koltuk_no ile)
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

    // Bilet Silme (bilet_id ile - silinmiş seferler için)
    fun biletSilById(biletId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("biletler")
            .whereEqualTo("bilet_id", biletId)
            .whereEqualTo("kullanici_id", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot) { doc.reference.delete() }
            }
    }

    // Biletleri sefer bilgileriyle birlikte getir
    fun biletlerimiSeferBilgileriyleGetir(callback: (List<Pair<Biletler, com.example.rezervasyonapp1.data.entity.Seferler?>>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("biletler")
            .whereEqualTo("kullanici_id", uid)
            .addSnapshotListener { biletSnapshot, _ ->
                val biletler = biletSnapshot?.toObjects(Biletler::class.java) ?: listOf()
                
                if (biletler.isEmpty()) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                // Her bilet için sefer bilgisini getir
                val sonucListesi = mutableListOf<Pair<Biletler, com.example.rezervasyonapp1.data.entity.Seferler?>>()
                var tamamlananSayisi = 0

                biletler.forEach { bilet ->
                    db.collection("seferler").document(bilet.sefer_id).get()
                        .addOnSuccessListener { seferDoc ->
                            val sefer = seferDoc.toObject(com.example.rezervasyonapp1.data.entity.Seferler::class.java)
                            sonucListesi.add(Pair(bilet, sefer))
                            tamamlananSayisi++
                            
                            // Tüm biletler için sefer bilgileri alındığında callback çağır
                            if (tamamlananSayisi == biletler.size) {
                                callback(sonucListesi)
                            }
                        }
                        .addOnFailureListener {
                            sonucListesi.add(Pair(bilet, null))
                            tamamlananSayisi++
                            if (tamamlananSayisi == biletler.size) {
                                callback(sonucListesi)
                            }
                        }
                }
            }
    }
}