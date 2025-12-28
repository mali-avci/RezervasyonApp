package com.example.rezervasyonapp1.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.rezervasyonapp1.data.entity.Otobus
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.data.entity.Ucak
import com.example.rezervasyonapp1.databinding.FragmentAdminBinding
import com.example.rezervasyonapp1.ui.viewmodel.AracViewModel
import com.example.rezervasyonapp1.ui.viewmodel.SeferlerViewModel
import com.example.rezervasyonapp1.util.SehirlerConstants
import java.util.*

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    // İki ViewModel kullanıyoruz: Biri Seferleri yönetir, diğeri Araç listesini getirir
    private val seferViewModel: SeferlerViewModel by viewModels()
    private val aracViewModel: AracViewModel by viewModels()

    private var secilenSeferId: String? = null

    // Dropdown'dan seçilen araç nesnesini burada tutacağız
    private var secilenAracNesnesi: Any? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSehirDropdowns()
        setupAracDropdownObserver() // Araç listesini dinle

        // Bundle'dan seferId'yi al (düzenleme modu için)
        val gelenSeferId = arguments?.getString("seferId") ?: ""
        
        if (gelenSeferId.isNotEmpty()) {
            // Düzenleme modunda: Sefer bilgilerini yükle
            seferDuzenlemeModu(gelenSeferId)
        }

        // 1. Araç Tipi Değiştiğinde Listeyi Yenile
        binding.rgTip.setOnCheckedChangeListener { _, checkedId ->
            val tip = if (checkedId == binding.rbOtobus.id) "OTOBUS" else "UCAK"
            aracViewModel.araclariYukle(tip)
            secilenAracNesnesi = null // Tip değişince seçimi sıfırla
        }

        // İlk açılışta otobüsleri yükle
        aracViewModel.araclariYukle("OTOBUS")

        // 2. Tarih Seçici
        binding.etTarih.setOnClickListener { tarihSeciciyiGoster() }

        // 3. Kaydet Butonu
        binding.btnEkle.setOnClickListener { veriyiIsle() }

        // 4. Geri Butonu
        binding.btnGeri.setOnClickListener { 
            findNavController().popBackStack()
        }
    }
    
    private fun seferDuzenlemeModu(seferId: String) {
        // Önce sefer bilgilerini Firestore'dan çek
        seferViewModel.srepo.tekSeferGetir(seferId) { sefer ->
            sefer?.let { secilenSefer ->
                secilenSeferId = secilenSefer.sefer_id
                binding.etTarih.setText(secilenSefer.tarih)
                binding.etSaat.setText(secilenSefer.saat)
                binding.etFiyat.setText(secilenSefer.fiyat.toString())
                binding.btnEkle.text = "GÜNCELLE"

                // Araç tipini ayarla
                if (secilenSefer.arac_tipi == "OTOBUS") {
                    binding.rbOtobus.isChecked = true
                    aracViewModel.araclariYukle("OTOBUS")
                } else {
                    binding.rbUcak.isChecked = true
                    aracViewModel.araclariYukle("UCAK")
                }
                
                // AutoCompleteTextView seçimlerini ayarla
                if (SehirlerConstants.SEHIRLER.contains(secilenSefer.kalkis_yeri)) {
                    binding.autoCompleteKalkis.setText(secilenSefer.kalkis_yeri, false)
                }
                if (SehirlerConstants.SEHIRLER.contains(secilenSefer.varis_yeri)) {
                    binding.autoCompleteVaris.setText(secilenSefer.varis_yeri, false)
                }
                
                // Seçilen aracı bul ve ayarla
                aracViewModel.aracListesi.observe(viewLifecycleOwner) { aracListesi ->
                    val gosterilecekListe = aracListesi.map { arac ->
                        if (arac is Otobus) "${arac.arac_plaka} - ${arac.firma_adi} (${arac.kapasite})"
                        else (arac as Ucak).let { "${it.arac_plaka} - ${it.havayolu_sirketi} (${it.kapasite})" }
                    }
                    val secilenArac = aracListesi.find { arac ->
                        if (arac is Otobus && secilenSefer.otobus_detay != null) {
                            arac.arac_plaka == secilenSefer.otobus_detay?.arac_plaka
                        } else if (arac is Ucak && secilenSefer.ucak_detay != null) {
                            arac.arac_plaka == secilenSefer.ucak_detay?.arac_plaka
                        } else {
                            false
                        }
                    }
                    secilenArac?.let {
                        secilenAracNesnesi = it
                        val aracIndex = aracListesi.indexOf(it)
                        if (aracIndex >= 0 && aracIndex < gosterilecekListe.size) {
                            binding.autoCompleteAracSec.setText(gosterilecekListe[aracIndex], false)
                        }
                    }
                }
            }
        }
    }

    private fun setupAracDropdownObserver() {
        // ViewModel'den gelen araç listesini dinle ve AutoComplete'e doldur
        aracViewModel.aracListesi.observe(viewLifecycleOwner) { aracListesi ->
            val gosterilecekListe = aracListesi.map { arac ->
                if (arac is Otobus) "${arac.arac_plaka} - ${arac.firma_adi} (${arac.kapasite})"
                else (arac as Ucak).let { "${it.arac_plaka} - ${it.havayolu_sirketi} (${it.kapasite})" }
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                gosterilecekListe
            )
            binding.autoCompleteAracSec.setAdapter(adapter)

            // Seçim yapıldığında araç nesnesini kaydet
            binding.autoCompleteAracSec.setOnItemClickListener { _, _, position, _ ->
                secilenAracNesnesi = aracListesi[position]
            }
        }
    }

    private fun veriyiIsle() {
        val kalkis = binding.autoCompleteKalkis.text.toString().trim()
        val varis = binding.autoCompleteVaris.text.toString().trim()
        val fiyatText = binding.etFiyat.text.toString().trim()
        val tarih = binding.etTarih.text.toString().trim()
        val saat = binding.etSaat.text.toString().trim()

        // Validasyon kontrolleri
        if (kalkis.isEmpty() || varis.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen kalkış ve varış şehirlerini seçiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (kalkis == varis) {
            Toast.makeText(requireContext(), "Kalkış ve varış noktaları aynı olamaz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (tarih.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tarih seçiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (saat.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen saat giriniz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (fiyatText.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen fiyat giriniz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (secilenAracNesnesi == null) {
            Toast.makeText(requireContext(), "Lütfen araç seçiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val tip = if (binding.rbOtobus.isChecked) "OTOBUS" else "UCAK"
            val fiyat = fiyatText.toDouble()

            val sefer = Seferler(
                sefer_id = secilenSeferId ?: "",
                kalkis_yeri = kalkis,
                varis_yeri = varis,
                tarih = tarih,
                saat = saat,
                fiyat = fiyat,
                arac_tipi = tip,
                dolu_koltuklar = ""
            )

            // Spinner'dan seçilen aracı seferin içine gömüyoruz
            if (secilenAracNesnesi is Otobus) {
                sefer.otobus_detay = secilenAracNesnesi as Otobus
                sefer.ucak_detay = null
            } else if (secilenAracNesnesi is Ucak) {
                sefer.ucak_detay = secilenAracNesnesi as Ucak
                sefer.otobus_detay = null
            }

            if (secilenSeferId == null) {
                seferViewModel.srepo.seferKaydet(sefer)
                Toast.makeText(requireContext(), "Sefer Başarıyla Kaydedildi", Toast.LENGTH_SHORT).show()
            } else {
                seferViewModel.srepo.seferGuncelle(sefer)
                Toast.makeText(requireContext(), "Sefer Başarıyla Güncellendi", Toast.LENGTH_SHORT).show()
            }
            
            alanlariTemizle()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Fiyat geçerli bir sayı olmalıdır!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSehirDropdowns() {
        // Kalkış dropdown için adapter
        val adapterKalkis = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SehirlerConstants.SEHIRLER
        )
        binding.autoCompleteKalkis.setAdapter(adapterKalkis)
        
        // Varış dropdown için adapter
        val adapterVaris = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SehirlerConstants.SEHIRLER
        )
        binding.autoCompleteVaris.setAdapter(adapterVaris)

        // Şehir seçildiğinde diğer dropdown'u güncelle (aynı şehir seçilemez)
        binding.autoCompleteKalkis.setOnItemClickListener { _, _, _, _ ->
            updateVarisDropdown()
        }
        
        binding.autoCompleteVaris.setOnItemClickListener { _, _, _, _ ->
            updateKalkisDropdown()
        }
    }

    private fun updateVarisDropdown() {
        val secilenKalkis = binding.autoCompleteKalkis.text.toString()
        if (secilenKalkis.isEmpty()) return
        
        val filtrelenmisListe = SehirlerConstants.SEHIRLER.filter { it != secilenKalkis }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            filtrelenmisListe
        )
        binding.autoCompleteVaris.setAdapter(adapter)
        
        // Eğer seçili varış, kalkış ile aynıysa temizle
        if (binding.autoCompleteVaris.text.toString() == secilenKalkis) {
            binding.autoCompleteVaris.setText("", false)
        }
    }

    private fun updateKalkisDropdown() {
        val secilenVaris = binding.autoCompleteVaris.text.toString()
        if (secilenVaris.isEmpty()) return
        
        val filtrelenmisListe = SehirlerConstants.SEHIRLER.filter { it != secilenVaris }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            filtrelenmisListe
        )
        binding.autoCompleteKalkis.setAdapter(adapter)
        
        // Eğer seçili kalkış, varış ile aynıysa temizle
        if (binding.autoCompleteKalkis.text.toString() == secilenVaris) {
            binding.autoCompleteKalkis.setText("", false)
        }
    }

    private fun tarihSeciciyiGoster() {
        val takvim = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(), { _, y, a, g ->
            val formatliGun = if (g < 10) "0$g" else "$g"
            val formatliAy = if (a + 1 < 10) "0${a + 1}" else "${a + 1}"
            binding.etTarih.setText("$formatliGun.$formatliAy.$y")
        }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun alanlariTemizle() {
        binding.etTarih.setText("")
        binding.etSaat.setText("")
        binding.etFiyat.setText("")
        binding.autoCompleteKalkis.setText("", false)
        binding.autoCompleteVaris.setText("", false)
        binding.btnEkle.text = "SEFERİ KAYDET"
        secilenSeferId = null
        secilenAracNesnesi = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}