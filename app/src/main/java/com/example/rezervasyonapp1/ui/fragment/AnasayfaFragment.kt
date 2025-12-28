package com.example.rezervasyonapp1.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.R
import com.example.rezervasyonapp1.databinding.FragmentAnasayfaBinding
import com.example.rezervasyonapp1.util.SehirlerConstants
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

        // Dropdown'ları kur
        setupSehirDropdowns()

        // Tarih Seçici (DatePicker)
        binding.editTextTarih.setOnClickListener {
            val takvim = Calendar.getInstance()
            val yil = takvim.get(Calendar.YEAR)
            val ay = takvim.get(Calendar.MONTH)
            val gun = takvim.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, y, a, g ->
                val formatliGun = if (g < 10) "0$g" else "$g"
                val formatliAy = if (a + 1 < 10) "0${a + 1}" else "${a + 1}"
                binding.editTextTarih.setText("$formatliGun.$formatliAy.$y")
            }, yil, ay, gun)
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        }

        binding.buttonSeferAra.setOnClickListener {
            val kalkis = binding.autoCompleteNereden.text.toString().trim()
            val varis = binding.autoCompleteNereye.text.toString().trim()
            val tarih = binding.editTextTarih.text.toString()

            if (kalkis.isEmpty() || varis.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen kalkış ve varış noktalarını seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (kalkis == varis) {
                Toast.makeText(requireContext(), "Kalkış ve varış noktaları aynı olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarih.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen tarih seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Burada SeferlerFragment'a geçiş yapıyoruz.
            // Seçilen şehirleri ve tarihi argüman olarak gönderiyoruz.
            val gecis = AnasayfaFragmentDirections.actionAnasayfaFragmentToSeferlerFragment(kalkis, varis, tarih)
            findNavController().navigate(gecis)
        }

        // Biletlerim butonu
        binding.buttonBiletlerim.setOnClickListener {
            findNavController().navigate(R.id.action_anasayfaFragment_to_biletlerimFragment)
        }

        // --- ÇIKIŞ İŞLEMİ ---
        binding.CikisYap.setOnClickListener {
            // 1. Firebase oturumunu kapat
            FirebaseAuth.getInstance().signOut()

            // 2. Login ekranına YÖNLENDİR
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.anasayfaFragment, true)
                .build()

            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }

        return binding.root
    }

    private fun setupSehirDropdowns() {
        // Kalkış dropdown için adapter
        val adapterNereden = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SehirlerConstants.SEHIRLER
        )
        binding.autoCompleteNereden.setAdapter(adapterNereden)

        // Varış dropdown için adapter
        val adapterNereye = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SehirlerConstants.SEHIRLER
        )
        binding.autoCompleteNereye.setAdapter(adapterNereye)

        // Şehir seçildiğinde diğer dropdown'u güncelle (aynı şehir seçilemez)
        binding.autoCompleteNereden.setOnItemClickListener { _, _, _, _ ->
            updateNereyeDropdown()
        }

        binding.autoCompleteNereye.setOnItemClickListener { _, _, _, _ ->
            updateNeredenDropdown()
        }
    }

    private fun updateNereyeDropdown() {
        val secilenKalkis = binding.autoCompleteNereden.text.toString()
        if (secilenKalkis.isEmpty()) return

        val filtrelenmisListe = SehirlerConstants.SEHIRLER.filter { it != secilenKalkis }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            filtrelenmisListe
        )
        binding.autoCompleteNereye.setAdapter(adapter)

        // Eğer seçili varış, kalkış ile aynıysa temizle
        if (binding.autoCompleteNereye.text.toString() == secilenKalkis) {
            binding.autoCompleteNereye.setText("", false)
        }
    }

    private fun updateNeredenDropdown() {
        val secilenVaris = binding.autoCompleteNereye.text.toString()
        if (secilenVaris.isEmpty()) return

        val filtrelenmisListe = SehirlerConstants.SEHIRLER.filter { it != secilenVaris }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            filtrelenmisListe
        )
        binding.autoCompleteNereden.setAdapter(adapter)

        // Eğer seçili kalkış, varış ile aynıysa temizle
        if (binding.autoCompleteNereden.text.toString() == secilenVaris) {
            binding.autoCompleteNereden.setText("", false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}