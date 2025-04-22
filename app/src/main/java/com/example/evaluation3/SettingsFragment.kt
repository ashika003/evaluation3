package com.example.evaluation3

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.evaluation3.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        setupListeners()
        updateThemeButtonText()
    }

    private fun setupListeners() {
        binding.themeToggleButton.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(newMode)
            prefs.edit().putInt("theme_mode", newMode).apply()
            updateThemeButtonText()
        }

        binding.currencyButton.setOnClickListener {
            showCurrencyDialog()
        }

        binding.clearTransactionsButton.setOnClickListener {
            showClearTransactionsDialog()
        }

        binding.passwordResetButton.setOnClickListener {
            showPasswordResetDialog()
        }
    }

    private fun updateThemeButtonText() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        binding.themeToggleButton.text = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            getString(R.string.switch_to_light_theme)
        } else {
            getString(R.string.switch_to_dark_theme)
        }
    }

    private fun showCurrencyDialog() {
        val currencies = resources.getStringArray(R.array.currency_options)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_currency)
            .setItems(currencies) { _, which ->
                val selectedCurrency = currencies[which]
                prefs.edit().putString("currency", selectedCurrency).apply()
                requireActivity().recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showClearTransactionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.clear_transactions)
            .setMessage(R.string.clear_transactions_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                clearAllTransactions()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun clearAllTransactions() {
        val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        incomePrefs.edit().clear().apply()
        val expensePrefs = requireContext().getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        expensePrefs.edit().clear().apply()
        android.widget.Toast.makeText(
            requireContext(),
            R.string.transactions_cleared,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPasswordResetDialog() {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_password_reset, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.reset_password)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val password = passwordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (password.isEmpty()) {
                    passwordInput.error = getString(R.string.password_empty)
                    return@setOnClickListener
                }
                if (password != confirmPassword) {
                    confirmPasswordInput.error = getString(R.string.password_mismatch)
                    return@setOnClickListener
                }

                prefs.edit().putString("user_password", password).apply()
                android.widget.Toast.makeText(
                    context,
                    R.string.password_updated,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}