package com.example.rezervasyonapp1.data.repo

import androidx.lifecycle.MutableLiveData
import com.example.rezervasyonapp1.data.entity.Seferler
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rezervasyonapp1.data.entity.Otobus
import com.example.rezervasyonapp1.data.entity.Ucak

class SeferlerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val seferlerCollection = db.collection("seferler")
    val seferlerListesi = MutableLiveData<List<Seferler>>()



    fun tumAraclariGetir(tip: String, callback: (List<Any>) -> Unit) {
        // Firestore'da 'araclar' adında yeni bir koleksiyon olduğunu varsayıyoruz
        db.collection("araclar").whereEqualTo("tip", tip).get()
            .addOnSuccessListener { snapshot ->
                if (tip == "OTOBUS") {
                    callback(snapshot.toObjects(Otobus::class.java))
                } else {
                    callback(snapshot.toObjects(Ucak::class.java))
                }
            }
    }

    // 'callback' parametresini 'Seferler?' (nullable) yaparak hatayı çözüyoruz
    fun tekSeferiCanliDinle(seferId: String, callback: (Seferler?) -> Unit) {
        seferlerCollection.document(seferId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                callback(null)
                return@addSnapshotListener
            }

            // Döküman varsa Seferler nesnesine dönüştür, yoksa null dön
            val sefer = snapshot?.toObject(Seferler::class.java)
            callback(sefer)
        }
    }

    fun tumSeferleriAl() {
        // SnapshotListener: Veritabanında bir değişiklik (ekleme, silme, güncelleme)
        // olduğunda uygulama ekranı anında güncellenir.
        seferlerCollection.addSnapshotListener { value, error ->
            if (value != null) {
                val liste = value.toObjects(Seferler::class.java)
                seferlerListesi.value = liste
            }
        }
    }

    fun seferAra(kalkis: String, varis: String) {
        seferlerCollection
            .whereEqualTo("kalkis_yeri", kalkis)
            .whereEqualTo("varis_yeri", varis)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                // Veritabanında herhangi bir koltuk değiştiği anda burası tetiklenir
                val liste = value?.toObjects(Seferler::class.java) ?: listOf()
                seferlerListesi.value = liste
            }
    }

    fun seferKaydet(sefer: Seferler) {
        val yeniDoc = seferlerCollection.document() // Firestore'da benzersiz döküman ID üretir
        sefer.sefer_id = yeniDoc.id // Üretilen ID, Seferler nesnesinin içine yazılır
        yeniDoc.set(sefer)
    }

    fun seferSil(sefer_id: String) {
        // ID üzerinden ilgili dökümanı bulur ve siler
        seferlerCollection.document(sefer_id).delete()
    }

    // YENİ: Sefer Güncelleme (Özellikle koltuk seçiminde 'dolu_koltuklar' alanı için)
    fun seferGuncelle(sefer: Seferler) {
        // Mevcut dökümanı yeni nesne verileriyle (ID değişmeden) günceller
        seferlerCollection.document(sefer.sefer_id).set(sefer)
    }

    // YENİ: Tek Bir Sefer Getir (Opsiyonel: Bilet detayı veya derin linkleme için)
    fun tekSeferGetir(sefer_id: String, callback: (Seferler?) -> Unit) {
        seferlerCollection.document(sefer_id).get().addOnSuccessListener { document ->
            val sefer = document.toObject(Seferler::class.java)
            callback(sefer)
        }
    }
}