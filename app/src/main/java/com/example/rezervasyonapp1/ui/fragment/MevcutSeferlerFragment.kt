package com.example.rezervasyonapp1.ui.fragment

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rezervasyonapp1.R
import com.example.rezervasyonapp1.databinding.FragmentMevcutSeferlerBinding
import com.example.rezervasyonapp1.ui.adapter.SeferlerAdapter
import com.example.rezervasyonapp1.ui.viewmodel.SeferlerViewModel
import com.google.android.material.snackbar.Snackbar

class MevcutSeferlerFragment : Fragment() {
    private var _binding: FragmentMevcutSeferlerBinding? = null
    private val binding get() = _binding!!

    private val seferViewModel: SeferlerViewModel by viewModels()
    private lateinit var adapter: SeferlerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMevcutSeferlerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeData()

        // Geri butonu
        binding.buttonGeri.setOnClickListener {
            findNavController().popBackStack()
        }

        // Verileri yükle
        seferViewModel.seferleriYukle()
    }

    private fun setupRecyclerView() {
        adapter = SeferlerAdapter { secilenSefer ->
            // Sefere tıklandığında AdminFragment'a git (düzenleme için)
            // Bundle ile manuel navigasyon (Safe Args yerine daha güvenli)
            val bundle = Bundle().apply {
                putString("seferId", secilenSefer.sefer_id)
            }
            findNavController().navigate(R.id.action_mevcutSeferlerFragment_to_adminFragment, bundle)
        }

        binding.rvSeferler.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSeferler.adapter = adapter
        binding.rvSeferler.setHasFixedSize(true)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION || position >= adapter.currentList.size) {
                    return
                }

                val silinecekSefer = adapter.currentList[position]

                // Onay dialog'u göster
                AlertDialog.Builder(requireContext())
                    .setTitle("Seferi Sil")
                    .setMessage("${silinecekSefer.kalkis_yeri} -> ${silinecekSefer.varis_yeri} seferini silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        seferViewModel.sil(silinecekSefer.sefer_id)
                        Snackbar.make(binding.root, "Sefer silindi", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hayır") { _, _ ->
                        // Silme iptal edilirse listeyi eski haline getir
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvSeferler)
    }

    private fun observeData() {
        seferViewModel.seferlerListesi.observe(viewLifecycleOwner) { liste ->
            Log.d("MevcutSeferlerFragment", "Veri geldi, liste boyutu: ${liste?.size ?: 0}")
            
            // Progress'i gizle
            binding.progressBar.visibility = View.GONE

            if (liste != null && liste.isNotEmpty()) {
                adapter.submitList(liste.toList())
                binding.rvSeferler.visibility = View.VISIBLE
                binding.textViewBosMesaj.visibility = View.GONE
            } else {
                adapter.submitList(emptyList())
                binding.rvSeferler.visibility = View.GONE
                binding.textViewBosMesaj.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
