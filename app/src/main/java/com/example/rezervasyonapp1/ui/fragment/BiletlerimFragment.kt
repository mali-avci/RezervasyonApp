package com.example.rezervasyonapp1.ui.fragment

import com.example.rezervasyonapp1.ui.viewmodel.SeferlerViewModel
import android.app.AlertDialog
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rezervasyonapp1.databinding.DialogBiletDetayBinding
import com.example.rezervasyonapp1.databinding.FragmentBiletlerimBinding
import com.example.rezervasyonapp1.ui.adapter.BiletlerimAdapter
import com.example.rezervasyonapp1.ui.viewmodel.BiletViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BiletlerimFragment : Fragment() {
    private var _binding: FragmentBiletlerimBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BiletViewModel by viewModels()
    private val seferViewModel: SeferlerViewModel by viewModels()
    private lateinit var adapter: BiletlerimAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiletlerimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        // Biletleri sefer bilgileriyle birlikte yükle
        viewModel.yukleSeferBilgileriyle()
    }

    private fun setupRecyclerView() {
        adapter = BiletlerimAdapter { biletSefer ->
            biletDetayDialogGoster(biletSefer)
        }
        binding.rvBiletlerim.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBiletlerim.adapter = adapter
    }

    private fun biletDetayDialogGoster(biletSefer: Pair<com.example.rezervasyonapp1.data.entity.Biletler, com.example.rezervasyonapp1.data.entity.Seferler?>) {
        val bilet = biletSefer.first
        val sefer = biletSefer.second

        val dialogBinding = DialogBiletDetayBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        if (sefer != null) {
            dialogBinding.textViewGuzergah.text = "${sefer.kalkis_yeri} -> ${sefer.varis_yeri}"
            dialogBinding.textViewTarih.text = "Tarih: ${sefer.tarih}"
            dialogBinding.textViewSaat.text = "Saat: ${sefer.saat}"
            dialogBinding.textViewKoltukNo.text = "Koltuk No: ${bilet.koltuk_no}"
            dialogBinding.textViewFiyat.text = "Fiyat: ${sefer.fiyat.toInt()} TL"

            // Araç Tipi Ayrımı
            if (sefer.arac_tipi == "UCAK") {
                dialogBinding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_send)
                dialogBinding.tvAracTipiLabel.text = "UÇAK SEFERİ"
                dialogBinding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_blue_dark)
            } else {
                dialogBinding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_directions)
                dialogBinding.tvAracTipiLabel.text = "OTOBÜS SEFERİ"
                dialogBinding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_green_dark)
            }

            // Paylaş butonu - Örtülü Intent ile bilet bilgilerini paylaş
            dialogBinding.buttonPaylas.setOnClickListener {
                biletBilgileriniPaylas(bilet, sefer)
            }

            // Silme butonu
            dialogBinding.buttonSil.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Bilet İptali")
                    .setMessage("Bu bileti iptal etmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        // Bileti iptal et
                        viewModel.biletIptal(sefer.sefer_id, bilet.koltuk_no)
                        
                        // Seferdeki dolu koltuklar listesinden de kaldır
                        val guncelDoluListe = sefer.dolu_koltuklar.split(",").filter { it.isNotEmpty() }.toMutableList()
                        guncelDoluListe.remove(bilet.koltuk_no.toString())
                        sefer.dolu_koltuklar = guncelDoluListe.joinToString(",")
                        
                        // Seferi güncelle
                        seferViewModel.guncelle(sefer)
                        
                        Toast.makeText(requireContext(), "Bilet iptal edildi", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        // Listeyi yenile
                        viewModel.yukleSeferBilgileriyle()
                    }
                    .setNegativeButton("Hayır", null)
                    .show()
            }
        } else {
            // Sefer silinmiş durumda
            dialogBinding.textViewGuzergah.text = "Sefer silinmiş veya bulunamadı"
            dialogBinding.textViewTarih.text = "Sefer ID: ${bilet.sefer_id}"
            dialogBinding.textViewSaat.text = "Koltuk No: ${bilet.koltuk_no}"
            dialogBinding.textViewKoltukNo.visibility = View.GONE
            dialogBinding.textViewFiyat.visibility = View.GONE
            dialogBinding.ivAracIkonu.visibility = View.GONE
            dialogBinding.tvAracTipiLabel.visibility = View.GONE
            
            // Paylaş butonu devre dışı (sefer bilgisi yok)
            dialogBinding.buttonPaylas.isEnabled = false
            dialogBinding.buttonPaylas.alpha = 0.5f
            
            // Silme butonu hala çalışmalı!
            dialogBinding.buttonSil.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Bilet Silme")
                    .setMessage("Bu bileti silmek istediğinize emin misiniz?\n\n(Not: Sefer sistemden silindiği için geri iade yapılamaz)")
                    .setPositiveButton("Evet, Sil") { _, _ ->
                        // Sadece bileti sil (sefer zaten yok)
                        viewModel.biletSil(bilet.bilet_id)
                        
                        Toast.makeText(requireContext(), "Bilet silindi", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        // Listeyi yenile
                        viewModel.yukleSeferBilgileriyle()
                    }
                    .setNegativeButton("Hayır", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun biletBilgileriniPaylas(bilet: com.example.rezervasyonapp1.data.entity.Biletler, sefer: com.example.rezervasyonapp1.data.entity.Seferler) {
        try {
            // PDF oluştur
            val pdfFile = biletPdfOlustur(bilet, sefer)
            
            // PDF dosyasını paylaş
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                pdfFile
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Bilet Bilgileri - ${sefer.kalkis_yeri} → ${sefer.varis_yeri}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Bilet PDF'ini paylaş"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "PDF oluşturulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun biletPdfOlustur(bilet: com.example.rezervasyonapp1.data.entity.Biletler, sefer: com.example.rezervasyonapp1.data.entity.Seferler): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 boyutu (point cinsinden)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = android.graphics.Paint()
        var y = 100f
        
        // Başlık
        paint.textSize = 28f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#2196F3")
        canvas.drawText("BİLET BİLGİLERİ", 50f, y, paint)
        y += 60f
        
        // Çizgi
        paint.color = android.graphics.Color.parseColor("#757575")
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)
        y += 40f
        
        // Bilet bilgileri
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        
        canvas.drawText("Güzergah:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("${sefer.kalkis_yeri} → ${sefer.varis_yeri}", 200f, y, paint)
        y += 40f
        
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Tarih:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(sefer.tarih, 200f, y, paint)
        y += 40f
        
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Saat:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(sefer.saat, 200f, y, paint)
        y += 40f
        
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Koltuk No:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(bilet.koltuk_no.toString(), 200f, y, paint)
        y += 40f
        
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Fiyat:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#4CAF50")
        canvas.drawText("${sefer.fiyat.toInt()} TL", 200f, y, paint)
        y += 40f
        
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        canvas.drawText("Araç Tipi:", 50f, y, paint)
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(if (sefer.arac_tipi == "UCAK") "Uçak" else "Otobüs", 200f, y, paint)
        y += 60f
        
        // Alt çizgi
        paint.color = android.graphics.Color.parseColor("#757575")
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)
        y += 40f
        
        // Alt bilgi
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.color = android.graphics.Color.parseColor("#757575")
        canvas.drawText("RezervasyonApp ile oluşturuldu", 50f, y, paint)
        
        pdfDocument.finishPage(page)
        
        // Dosyayı kaydet
        val fileName = "Bilet_${sefer.kalkis_yeri}_${sefer.varis_yeri}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        file.outputStream().use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        
        return file
    }

    private fun observeData() {
        viewModel.biletlerSeferBilgileriyle.observe(viewLifecycleOwner) { liste ->
            if (liste.isNullOrEmpty()) {
                binding.rvBiletlerim.visibility = View.GONE
                binding.textViewBosMesaj.visibility = View.VISIBLE
            } else {
                binding.rvBiletlerim.visibility = View.VISIBLE
                binding.textViewBosMesaj.visibility = View.GONE
                adapter.submitList(liste)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

