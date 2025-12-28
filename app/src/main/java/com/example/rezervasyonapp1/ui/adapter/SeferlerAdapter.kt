package com.example.rezervasyonapp1.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.databinding.SeferCardTasarimBinding

class SeferlerAdapter(private val onSeferClick: (Seferler) -> Unit) :
    ListAdapter<Seferler, SeferlerAdapter.SeferViewHolder>(SeferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeferViewHolder {
        val binding = SeferCardTasarimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeferViewHolder, position: Int) {
        holder.bind(getItem(position), onSeferClick)
    }

    class SeferViewHolder(private val binding: SeferCardTasarimBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sefer: Seferler, onSeferClick: (Seferler) -> Unit) {
            binding.textViewGuzergah.text = "${sefer.kalkis_yeri} -> ${sefer.varis_yeri}"
            binding.textViewFiyat.text = "${sefer.fiyat} TL"

            // Araç Tipi Ayrımı (Görselleştirme)
            if (sefer.arac_tipi == "UCAK") {
                // Uçak Ayarları: Mavi Tema
                binding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_send) // Uçak benzeri bir ikon
                binding.tvAracTipiLabel.text = "UÇAK SEFERİ"
                binding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_blue_dark)

                // Uçağa özel detay (Havayolu)
                binding.textViewDetay.text = "${sefer.tarih} | ${sefer.saat} | ${sefer.ucak_detay?.havayolu_sirketi ?: "THY"}"
            } else {
                // Otobüs Ayarları: Yeşil Tema
                binding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_directions) // Otobüs benzeri bir ikon
                binding.tvAracTipiLabel.text = "OTOBÜS SEFERİ"
                binding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_green_dark)

                // Otobüse özel detay (Plaka)
                binding.textViewDetay.text = "${sefer.tarih} | ${sefer.saat} | ${sefer.otobus_detay?.arac_plaka ?: "35 EGE 35"}"
            }

            binding.root.setOnClickListener {
                onSeferClick(sefer)
            }
        }
    }

    class SeferDiffCallback : DiffUtil.ItemCallback<Seferler>() {
        override fun areItemsTheSame(oldItem: Seferler, newItem: Seferler): Boolean = oldItem.sefer_id == newItem.sefer_id
        override fun areContentsTheSame(oldItem: Seferler, newItem: Seferler): Boolean = oldItem == newItem
    }
}