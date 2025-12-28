package com.example.rezervasyonapp1.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.R // R sınıfının import edildiğinden emin ol
import com.example.rezervasyonapp1.databinding.FragmentAdminPanelBinding
import com.google.firebase.auth.FirebaseAuth

class AdminPanelFragment : Fragment() {
    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Sefer Yönetimine Git (Eski AdminFragment)
        binding.cardSeferYonetimi.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_adminFragment)
        }

        // 2. Araç Yönetimine Git (Yeni AdminAracEkleFragment)
        binding.cardAracYonetimi.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_adminAracEkleFragment)
        }

        // 3. Mevcut Seferleri Görüntüle (YENİ)
        binding.cardMevcutSeferler.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_mevcutSeferlerFragment)
        }
        
        binding.btnCikis.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}