package com.example.rezervasyonapp1.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.R
import com.example.rezervasyonapp1.databinding.FragmentAnasayfaBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AnasayfaFragment : Fragment() {
    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)

        // Şehir Listesi (Spinnerlar için)
        val sehirler = listOf("İstanbul", "Ankara", "İzmir", "Antalya", "Bursa")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sehirler)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerNereden.adapter = adapter
        binding.spinnerNereye.adapter = adapter

        // Tarih Seçici (DatePicker)
        binding.editTextTarih.setOnClickListener {
            val takvim = Calendar.getInstance()
            val yil = takvim.get(Calendar.YEAR)
            val ay = takvim.get(Calendar.MONTH)
            val gun = takvim.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, y, a, g ->
                binding.editTextTarih.setText("$g/${a + 1}/$y")
            }, yil, ay, gun)
            dpd.show()
        }

        binding.buttonSeferAra.setOnClickListener {
            val kalkis = binding.spinnerNereden.selectedItem.toString()
            val varis = binding.spinnerNereye.selectedItem.toString()
            val tarih = binding.editTextTarih.text.toString()

            // Burada SeferlerFragment'a geçiş yapıyoruz.
            // Seçilen şehirleri argüman olarak gönderiyoruz.
            val gecis = AnasayfaFragmentDirections.actionAnasayfaFragmentToSeferlerFragment(kalkis, varis)
            findNavController().navigate(gecis)
        }

        // --- ÇIKIŞ İŞLEMİ (Admin Panelindeki Mantığın Aynısı) ---
        binding.CikisYap.setOnClickListener {
            // 1. Firebase oturumunu kapat
            FirebaseAuth.getInstance().signOut()

            // 2. Login ekranına YÖNLENDİR (Geri dönme değil, gitme işlemi)
            // Ayrıca giderken "Anasayfa"yı geçmişten sil ki geri tuşuna basınca tekrar girmesin.
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.anasayfaFragment, true) // Anasayfayı yığından sil
                .build()

                findNavController().navigate(R.id.loginFragment, null, navOptions)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}