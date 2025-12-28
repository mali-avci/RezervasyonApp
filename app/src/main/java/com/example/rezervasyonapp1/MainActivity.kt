package com.example.rezervasyonapp1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.rezervasyonapp1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // ViewBinding ve NavController tanımlamaları
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ViewBinding Kurulumu (activity_main.xml ile bağlama)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Navigation Controller Kurulumu
        // XML'deki 'navHostFragment' id'li parçayı buluyoruz
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Not: Senin örneğinde BottomNavigationView vardı.
        // Eğer projemize alt menü eklemek istersen kodları buraya ekleyebiliriz.
        // Ama şu an Login ve Admin ekranları olduğu için menüsüz başlamak daha güvenli.
    }
}