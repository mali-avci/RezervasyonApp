package com.example.rezervasyonapp1.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.R
import com.example.rezervasyonapp1.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // 1. Firebase nesnelerini burada tanımlıyoruz
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // 2. Nesneleri başlatıyoruz
        auth = Firebase.auth
        db = Firebase.firestore

        binding.buttonGiris.setOnClickListener {
            // 3. Değişkenleri binding üzerinden alıyoruz
            val email = binding.etLoginEmail.text.toString()
            val sifre = binding.etLoginSifre.text.toString()

            if (email.isNotEmpty() && sifre.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, sifre)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid

                            uid?.let {
                                // Firestore yetki kontrolü
                                db.collection("kullanicilar").document(it).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val isAdmin = document.getBoolean("isAdmin") ?: false

                                            if (isAdmin) {
                                                findNavController().navigate(R.id.action_loginFragment_to_adminPanelFragment)
                                            } else {
                                                findNavController().navigate(R.id.action_loginFragment_to_anasayfaFragment)
                                            }
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Giriş Başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonKayit.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_kayitOlFragment)
        }

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        // Sayfa her görünür olduğunda şifre alanını temizle
        binding.etLoginSifre.setText("")

        // Eğer E-posta'nın da silinmesini istersen bunu da açabilirsin:
        // binding .etLoginEmail.setText("")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}