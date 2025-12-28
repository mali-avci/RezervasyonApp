package com.example.rezervasyonapp1.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rezervasyonapp1.data.entity.Biletler
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.databinding.BiletCardTasarimBinding

class BiletlerimAdapter(
    private val onBiletClick: (Pair<Biletler, Seferler?>) -> Unit
) : ListAdapter<Pair<Biletler, Seferler?>, BiletlerimAdapter.BiletViewHolder>(BiletDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BiletViewHolder {
        val binding = BiletCardTasarimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BiletViewHolder(binding, onBiletClick)
    }

    override fun onBindViewHolder(holder: BiletViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BiletViewHolder(
        private val binding: BiletCardTasarimBinding,
        private val onBiletClick: (Pair<Biletler, Seferler?>) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(biletSefer: Pair<Biletler, Seferler?>) {
            val bilet = biletSefer.first
            val sefer = biletSefer.second

            if (sefer != null) {
                binding.textViewGuzergah.text = "${sefer.kalkis_yeri} -> ${sefer.varis_yeri}"
                binding.textViewDetay.text = "${sefer.tarih} | ${sefer.saat}"
                binding.textViewKoltukNo.text = "Koltuk: ${bilet.koltuk_no}"
                binding.textViewFiyat.text = "${sefer.fiyat} TL"

                // Araç Tipi Ayrımı (Görselleştirme)
                if (sefer.arac_tipi == "UCAK") {
                    binding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_send)
                    binding.tvAracTipiLabel.text = "UÇAK SEFERİ"
                    binding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_blue_dark)
                } else {
                    binding.ivAracIkonu.setImageResource(android.R.drawable.ic_menu_directions)
                    binding.tvAracTipiLabel.text = "OTOBÜS SEFERİ"
                    binding.tvAracTipiLabel.setBackgroundResource(android.R.color.holo_green_dark)
                }
            } else {
                // Sefer bilgisi yüklenemedi
                binding.textViewGuzergah.text = "Sefer bilgisi yüklenemedi"
                binding.textViewDetay.text = "Sefer ID: ${bilet.sefer_id}"
                binding.textViewKoltukNo.text = "Koltuk: ${bilet.koltuk_no}"
                binding.textViewFiyat.text = "-"
            }

            // Tıklama olayı
            binding.root.setOnClickListener {
                onBiletClick(biletSefer)
            }
        }
    }

    class BiletDiffCallback : DiffUtil.ItemCallback<Pair<Biletler, Seferler?>>() {
        override fun areItemsTheSame(oldItem: Pair<Biletler, Seferler?>, newItem: Pair<Biletler, Seferler?>): Boolean {
            return oldItem.first.bilet_id == newItem.first.bilet_id
        }

        override fun areContentsTheSame(oldItem: Pair<Biletler, Seferler?>, newItem: Pair<Biletler, Seferler?>): Boolean {
            return oldItem == newItem
        }
    }
}

