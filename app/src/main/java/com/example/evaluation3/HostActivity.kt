package com.example.evaluation3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evaluation3.databinding.ActivityHostBinding

class HostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load DashboardFragment by default
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DashboardFragment())
            .commit()

        // Setup bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment())
                        .commit()
                    true
                }
                R.id.nav_transactions -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TransactionsFragment())
                        .commit()
                    true
                }
                R.id.nav_statistics -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, StatisticsFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}