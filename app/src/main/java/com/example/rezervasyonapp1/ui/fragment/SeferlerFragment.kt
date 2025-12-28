package com.example.rezervasyonapp1.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rezervasyonapp1.databinding.FragmentSeferlerBinding
import com.example.rezervasyonapp1.ui.adapter.SeferlerAdapter
import com.example.rezervasyonapp1.ui.viewmodel.SeferlerViewModel

class SeferlerFragment : Fragment() {

    private var _binding: FragmentSeferlerBinding? = null
    private val binding get() = _binding!!

    // Firebase tabanlı yeni ViewModel
    private val viewModel: SeferlerViewModel by viewModels()
    private lateinit var adapter: SeferlerAdapter

    // SafeArgs: Anasayfadan gelen kalkış ve varış şehirleri
    private val args: SeferlerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeferlerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // 1. Veri Gözlemleme (Livedata)
        observeData()

        // 2. Firestore üzerinden filtreli aramayı başlat
        // Bu metod Repository içindeki 'whereEqualTo' sorgusunu tetikler.
        // Tarih filtresi de eklendi
        viewModel.ara(args.kalkis, args.varis, args.tarih)
    }

    private fun setupRecyclerView() {
        adapter = SeferlerAdapter { secilenSefer ->
            // Seçilen sefer nesnesini Koltuk Seçimi ekranına gönderir
            // Seferler nesnesi artık String tipinde sefer_id içerir
            val gecis = SeferlerFragmentDirections.actionSeferlerFragmentToKoltukSecimFragment(secilenSefer)
            findNavController().navigate(gecis)
        }

        binding.rvSeferler.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSeferler.adapter = adapter
    }

    private fun observeData() {
        // ViewModel'deki Livedata Firebase SnapshotListener'dan beslenir
        viewModel.seferlerListesi.observe(viewLifecycleOwner) { liste ->
            if (liste.isNullOrEmpty()) {
                // Filtreye göre özel mesaj göster
                binding.textViewBosMesaj.text = "${args.kalkis} - ${args.varis} arasında\n${args.tarih} tarihinde sefer bulunamadı."
                binding.textViewBosMesaj.visibility = View.VISIBLE
                binding.rvSeferler.visibility = View.GONE
            } else {
                binding.textViewBosMesaj.visibility = View.GONE
                binding.rvSeferler.visibility = View.VISIBLE
                adapter.submitList(liste)
            }
        }

        // Geri butonu
        binding.buttonGeri.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}