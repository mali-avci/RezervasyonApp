package com.example.rezervasyonapp1.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rezervasyonapp1.data.entity.Otobus
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.data.entity.Ucak
import com.example.rezervasyonapp1.databinding.FragmentAdminBinding
import com.example.rezervasyonapp1.ui.adapter.SeferlerAdapter
import com.example.rezervasyonapp1.ui.viewmodel.AracViewModel
import SeferlerViewModel
import android.app.AlertDialog
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    // İki ViewModel kullanıyoruz: Biri Seferleri yönetir, diğeri Araç listesini getirir
    private val seferViewModel: SeferlerViewModel by viewModels()
    private val aracViewModel: AracViewModel by viewModels()

    private val sehirler = listOf("İzmir", "İstanbul", "Ankara", "Antalya", "Bursa", "Adana", "Trabzon", "Erzurum")
    private var secilenSeferId: String? = null

    // Spinner'dan seçilen araç nesnesini burada tutacağız
    private var secilenAracNesnesi: Any? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSehirSpinners()
        setupAracSpinnerObserver() // Araç listesini dinle
        setupRecyclerView()

        seferViewModel.seferleriYukle()

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
    }

    private fun setupAracSpinnerObserver() {
        // ViewModel'den gelen araç listesini dinle ve Spinner'a doldur
        aracViewModel.aracListesi.observe(viewLifecycleOwner) { aracListesi ->
            val gosterilecekListe = aracListesi.map { arac ->
                if (arac is Otobus) "${arac.arac_plaka} - ${arac.firma_adi} (${arac.kapasite})"
                else (arac as Ucak).let { "${it.arac_plaka} - ${it.havayolu_sirketi} (${it.kapasite})" }
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gosterilecekListe)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAracSec.adapter = adapter // XML'de spinnerAracSec ID'li spinner olmalı

            binding.spinnerAracSec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    secilenAracNesnesi = aracListesi[position]
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun veriyiIsle() {
        val kalkis = binding.spinnerKalkis.selectedItem.toString()
        val varis = binding.spinnerVaris.selectedItem.toString()
        val fiyatText = binding.etFiyat.text.toString()
        val tarih = binding.etTarih.text.toString()

        if (fiyatText.isNotEmpty() && tarih.isNotEmpty() && secilenAracNesnesi != null) {
            val tip = if (binding.rbOtobus.isChecked) "OTOBUS" else "UCAK"

            val sefer = Seferler(
                sefer_id = secilenSeferId ?: "",
                kalkis_yeri = kalkis,
                varis_yeri = varis,
                tarih = tarih,
                saat = binding.etSaat.text.toString(),
                fiyat = fiyatText.toDouble(),
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
            } else {
                seferViewModel.srepo.seferGuncelle(sefer)
            }

            Toast.makeText(requireContext(), "Sefer Başarıyla Kaydedildi", Toast.LENGTH_SHORT).show()
            alanlariTemizle()
        } else {
            Toast.makeText(requireContext(), "Lütfen tarih, fiyat ve araç seçimi yapınız!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        val adapter = SeferlerAdapter { secilenSefer ->
            // DÜZENLEME MODU (Tıklanınca çalışır)
            secilenSeferId = secilenSefer.sefer_id
            binding.etTarih.setText(secilenSefer.tarih)
            binding.etSaat.setText(secilenSefer.saat)
            binding.etFiyat.setText(secilenSefer.fiyat.toString())
            binding.btnEkle.text = "GÜNCELLE"

            if (secilenSefer.arac_tipi == "OTOBUS") binding.rbOtobus.isChecked = true else binding.rbUcak.isChecked = true
            binding.spinnerKalkis.setSelection(sehirler.indexOf(secilenSefer.kalkis_yeri))
            binding.spinnerVaris.setSelection(sehirler.indexOf(secilenSefer.varis_yeri))
        }

        binding.rvAdminSeferler.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminSeferler.adapter = adapter

        // --- KAYDIRARAK SİLME İŞLEMİ (SWIPE TO DELETE) ---
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // Taşıma işlemi yapmıyoruz
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val silinecekSefer = adapter.currentList[position]

                // Kullanıcıdan son bir onay alalım (Yanlışlıkla silmeyi önlemek için)
                AlertDialog.Builder(requireContext())
                    .setTitle("Seferi Sil")
                    .setMessage("${silinecekSefer.kalkis_yeri} -> ${silinecekSefer.varis_yeri} seferini silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        // ViewModel üzerinden silme işlemini başlat
                        seferViewModel.sil(silinecekSefer.sefer_id)
                        Snackbar.make(binding.root, "Sefer silindi", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hayır") { _, _ ->
                        // Silme iptal edilirse listeyi eski haline getir (animasyonla geri gelir)
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            // (Opsiyonel) Kaydırırken arkada kırmızı renk ve çöp kutusu ikonu göstermek istersen:
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                // Eğer basit bir kütüphane kullanmak istersen 'RecyclerViewSwipeDecorator' öneririm.
                // Yoksa burayı boş bırakabilirsin, varsayılan efekt çalışır.
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Helper'ı RecyclerView'a bağla
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvAdminSeferler)

        // Verileri gözlemle
        seferViewModel.seferlerListesi.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun setupSehirSpinners() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sehirler)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKalkis.adapter = adapter
        binding.spinnerVaris.adapter = adapter
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
        binding.etTarih.setText(""); binding.etSaat.setText(""); binding.etFiyat.setText("")
        binding.btnEkle.text = "SEFERİ KAYDET"
        secilenSeferId = null
        secilenAracNesnesi = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}