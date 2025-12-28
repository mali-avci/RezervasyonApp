package com.example.rezervasyonapp1.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.R
import com.example.rezervasyonapp1.databinding.FragmentKayitOlBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class KayitOlFragment : Fragment(R.layout.fragment_kayit_ol) {
    private lateinit var binding: FragmentKayitOlBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentKayitOlBinding.bind(view)
        auth = Firebase.auth
        
        // Geri dön butonu
        binding.buttonGeriDon.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnKayitOl.setOnClickListener {
            val email = binding.etKayitEmail.text.toString()
            val sifre = binding.etKayitSifre.text.toString()
            val ad = binding.etKayitAd.text.toString()

            if (email.isNotEmpty() && sifre.isNotEmpty() && ad.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, sifre)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            // Kullanıcı detaylarını Firestore'a kaydet (isAdmin kontrolü için)
                            val kullaniciVerisi = hashMapOf(
                                "kullanici_ad" to ad,
                                "email" to email,
                                "isAdmin" to false // Varsayılan olarak normal kullanıcı
                            )

                            uid?.let {
                                db.collection("kullanicilar").document(it).set(kullaniciVerisi)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}