package com.example.rezervasyonapp1.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.databinding.FragmentKoltukSecimBinding
import com.example.rezervasyonapp1.ui.viewmodel.BiletViewModel
import SeferlerViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class KoltukSecimFragment : Fragment() {

    private var _binding: FragmentKoltukSecimBinding? = null
    private val binding get() = _binding!!

    private val args: KoltukSecimFragmentArgs by navArgs()

    private val seferViewModel: SeferlerViewModel by viewModels()
    private val biletViewModel: BiletViewModel by viewModels()

    private val secilenKoltuklar = mutableSetOf<Int>()
    private val iptalEdilecekKoltuklar = mutableSetOf<Int>()
    private var benimBiletlerim = listOf<Int>()
    private var guncelSeferNesnesi: Seferler? = null // Real-time veriyi saklamak için

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKoltukSecimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gelenSefer = args.secilenSefer
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.textViewBaslik.text = "${gelenSefer.kalkis_yeri} -> ${gelenSefer.varis_yeri}"
        binding.textViewAltBilgi.text = "${gelenSefer.tarih} | ${gelenSefer.saat} | ${gelenSefer.fiyat} TL"

        // 1. Veritabanındaki biletlerimi (sahiplik) gözlemle
        biletViewModel.biletlerListesi.observe(viewLifecycleOwner) { biletler ->
            benimBiletlerim = biletler.filter { it.sefer_id == gelenSefer.sefer_id }.map { it.koltuk_no }
            // Biletlerim değiştiğinde (iptal/alım) arayüzü yenile
            guncelSeferNesnesi?.let { generateKoltuklar(it) }
        }

        // 2. KRİTİK: Seferdeki doluluk oranını GERÇEK ZAMANLI gözlemle
        seferViewModel.anlikSefer.observe(viewLifecycleOwner) { guncelSefer ->
            if (guncelSefer != null) {
                guncelSeferNesnesi = guncelSefer
                // Başka biri koltuk aldığı an burası tetiklenir ve koltuklar yeniden çizilir
                generateKoltuklar(guncelSefer)
            }
        }

        // 3. Başlangıç verilerini yükle
        biletViewModel.yukle()
        seferViewModel.seciliSeferiTakipEt(gelenSefer.sefer_id) // Real-time dinleyiciyi başlat

        binding.buttonDevamEt.setOnClickListener {
            guncelSeferNesnesi?.let { safeSefer ->
                if (secilenKoltuklar.isNotEmpty() || iptalEdilecekKoltuklar.isNotEmpty()) {
                    islemiTamamla(safeSefer, uid)
                } else {
                    Toast.makeText(requireContext(), "Lütfen bir seçim yapınız!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateKoltuklar(sefer: Seferler) {
        binding.gridLayoutKoltuklar.removeAllViews()
        val doluListe = sefer.dolu_koltuklar.split(",").filter { it.isNotEmpty() }

        val (sutunSayisi, koridorIndeksi) = if (sefer.arac_tipi == "UCAK") Pair(7, 4) else Pair(5, 3)
        binding.gridLayoutKoltuklar.columnCount = sutunSayisi

        val toplamKapasite = if (sefer.arac_tipi == "UCAK") 180 else 44
        var koltukSayaci = 1

        while (koltukSayaci <= toplamKapasite) {
            for (kolon in 1..sutunSayisi) {
                if (kolon == koridorIndeksi) {
                    val koridor = View(requireContext()).apply { layoutParams = ViewGroup.LayoutParams(40, 40) }
                    binding.gridLayoutKoltuklar.addView(koridor)
                } else if (koltukSayaci <= toplamKapasite) {
                    val koltukNo = koltukSayaci++
                    val button = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle)

                    button.text = koltukNo.toString()
                    button.layoutParams = ViewGroup.LayoutParams(120, 120)
                    button.setPadding(0, 0, 0, 0)
                    button.textSize = 10f

                    when {
                        benimBiletlerim.contains(koltukNo) -> {
                            // MAVİ (İptal edilebilir)
                            val renk = if (iptalEdilecekKoltuklar.contains(koltukNo)) Color.GRAY else Color.BLUE
                            button.backgroundTintList = ColorStateList.valueOf(renk)
                            button.setTextColor(Color.WHITE)
                            button.setOnClickListener { toggleIptal(button, koltukNo) }
                        }
                        doluListe.contains(koltukNo.toString()) -> {
                            // KIRMIZI (Dolu)
                            button.backgroundTintList = ColorStateList.valueOf(Color.RED)
                            button.setTextColor(Color.WHITE)
                            button.isEnabled = false
                        }
                        else -> {
                            // BOŞ VEYA SEÇİLMİŞ
                            val renk = if (secilenKoltuklar.contains(koltukNo)) Color.parseColor("#4CAF50") else Color.TRANSPARENT
                            val yaziRengi = if (secilenKoltuklar.contains(koltukNo)) Color.WHITE else Color.BLACK

                            button.backgroundTintList = ColorStateList.valueOf(renk)
                            button.setTextColor(yaziRengi)
                            button.strokeColor = ColorStateList.valueOf(Color.LTGRAY)
                            button.setOnClickListener { toggleSecim(button, koltukNo) }
                        }
                    }
                    binding.gridLayoutKoltuklar.addView(button)
                }
            }
        }
    }

    private fun toggleSecim(button: MaterialButton, no: Int) {
        if (secilenKoltuklar.contains(no)) {
            secilenKoltuklar.remove(no)
        } else {
            secilenKoltuklar.add(no)
        }
        guncelSeferNesnesi?.let { generateKoltuklar(it) } // Görünümü tazele
    }

    private fun toggleIptal(button: MaterialButton, no: Int) {
        if (iptalEdilecekKoltuklar.contains(no)) {
            iptalEdilecekKoltuklar.remove(no)
        } else {
            iptalEdilecekKoltuklar.add(no)
        }
        guncelSeferNesnesi?.let { generateKoltuklar(it) } // Görünümü tazele
    }

    private fun islemiTamamla(sefer: Seferler, uid: String) {
        // 1. Yeni Biletleri Oluştur
        secilenKoltuklar.forEach { no -> biletViewModel.biletAl(sefer.sefer_id, no) }

        // 2. İptal Edilecek Biletleri Sil
        iptalEdilecekKoltuklar.forEach { no -> biletViewModel.biletIptal(sefer.sefer_id, no) }

        // 3. Seferdeki 'dolu_koltuklar' stringini güncelle
        val guncelDoluListe = sefer.dolu_koltuklar.split(",").filter { it.isNotEmpty() }.toMutableList()
        secilenKoltuklar.forEach { guncelDoluListe.add(it.toString()) }
        iptalEdilecekKoltuklar.forEach { guncelDoluListe.remove(it.toString()) }

        sefer.dolu_koltuklar = guncelDoluListe.joinToString(",")
        seferViewModel.guncelle(sefer)

        Toast.makeText(requireContext(), "İşlem Başarıyla Gerçekleşti", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}