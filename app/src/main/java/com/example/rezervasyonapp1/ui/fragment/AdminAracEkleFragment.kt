package com.example.rezervasyonapp1.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.data.entity.Otobus
import com.example.rezervasyonapp1.data.entity.Ucak
import com.example.rezervasyonapp1.databinding.FragmentAdminAracEkleBinding
import com.example.rezervasyonapp1.ui.viewmodel.AracViewModel

class AdminAracEkleFragment : Fragment() {
    private var _binding: FragmentAdminAracEkleBinding? = null
    private val binding get() = _binding!!

    // Araç işlemleri için ViewModel (Clean Architecture)
    private val aracViewModel: AracViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminAracEkleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnKaydet.setOnClickListener {
            val plaka = binding.etPlaka.text.toString()
            val firma = binding.etFirma.text.toString()
            val kapasite = binding.etKapasite.text.toString().toIntOrNull()

            if (plaka.isNotEmpty() && firma.isNotEmpty() && kapasite != null) {
                if (binding.rbOtobus.isChecked) {
                    // Otobüs Nesnesi Oluştur
                    val otobus = Otobus(kapasite, plaka, firma, "2+2")
                    // Firestore için Map hazırla ve Tip ekle
                    val veriMap = hashMapOf(
                        "kapasite" to otobus.kapasite,
                        "arac_plaka" to otobus.arac_plaka,
                        "firma_adi" to otobus.firma_adi,
                        "otobus_tipi" to otobus.otobus_tipi,
                        "tip" to "OTOBUS" // Sorgulama için gerekli
                    )
                    aracViewModel.aracEkle(plaka, veriMap)
                } else {
                    // Uçak Nesnesi Oluştur
                    val ucak = Ucak(kapasite, plaka, firma)
                    val veriMap = hashMapOf(
                        "kapasite" to ucak.kapasite,
                        "arac_plaka" to ucak.arac_plaka,
                        "havayolu_sirketi" to ucak.havayolu_sirketi,
                        "tip" to "UCAK" // Sorgulama için gerekli
                    )
                    aracViewModel.aracEkle(plaka, veriMap)
                }
                Toast.makeText(requireContext(), "Araç Havuza Eklendi!", Toast.LENGTH_SHORT).show()
                temizle()
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        // Geri Butonu
        binding.btnGeri.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun temizle() {
        binding.etPlaka.setText("")
        binding.etFirma.setText("")
        binding.etKapasite.setText("")
        binding.rbOtobus.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}