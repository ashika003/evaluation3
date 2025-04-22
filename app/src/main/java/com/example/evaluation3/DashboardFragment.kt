package com.example.evaluation3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evaluation3.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var expensePrefs: SharedPreferences
    private lateinit var incomePrefs: SharedPreferences
    private lateinit var settingsPrefs: SharedPreferences
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "currency") {
            Log.d("DashboardFragment", "Currency changed, refreshing data")
            refreshData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensePrefs = requireContext().getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        setupRecyclerView()
        setupButtonListeners()
        refreshData()
    }

    override fun onStart() {
        super.onStart()
        expensePrefs.registerOnSharedPreferenceChangeListener(prefsListener)
        incomePrefs.registerOnSharedPreferenceChangeListener(prefsListener)
        settingsPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        expensePrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        incomePrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        settingsPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun setupRecyclerView() {
        val transactions = loadRecentTransactions()
        transactionAdapter = TransactionAdapter(transactions) { /* No-op: Dashboard doesn't handle clicks */ }
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadRecentTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val threeDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -3)
        }.time

        // Load expenses
        val savedExpenses = expensePrefs.getStringSet("expenses", mutableSetOf())?.toList() ?: listOf()
        Log.d("DashboardFragment", "Recent expenses loaded: $savedExpenses")
        for (expenseString in savedExpenses) {
            val parts = expenseString.split("|")
            if (parts.size == 3) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("DashboardFragment", "Invalid date format in expense: ${parts[0]}")
                    null
                }
                if (date != null && date.after(threeDaysAgo)) {
                    transactions.add(
                        Transaction(
                            type = TransactionType.EXPENSE,
                            date = parts[0],
                            categoryOrReason = parts[1],
                            amount = parts[2],
                            description = null
                        )
                    )
                }
            } else {
                Log.e("DashboardFragment", "Invalid expense format: $expenseString")
            }
        }

        // Load income
        val savedIncomes = incomePrefs.getStringSet("incomes", mutableSetOf())?.toList() ?: listOf()
        Log.d("DashboardFragment", "Recent incomes loaded: $savedIncomes")
        for (incomeString in savedIncomes) {
            val parts = incomeString.split("|")
            if (parts.size == 4) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("DashboardFragment", "Invalid date format in income: ${parts[0]}")
                    null
                }
                if (date != null && date.after(threeDaysAgo)) {
                    transactions.add(
                        Transaction(
                            type = TransactionType.INCOME,
                            date = parts[0],
                            categoryOrReason = parts[1],
                            amount = parts[2],
                            description = parts[3]
                        )
                    )
                }
            } else {
                Log.e("DashboardFragment", "Invalid income format: $incomeString")
            }
        }

        // Sort by date (descending)
        return transactions.sortedByDescending { dateFormat.parse(it.date) }
    }

    private fun loadAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        // Load expenses
        val savedExpenses = expensePrefs.getStringSet("expenses", mutableSetOf())?.toList() ?: listOf()
        Log.d("DashboardFragment", "All expenses loaded: $savedExpenses")
        for (expenseString in savedExpenses) {
            val parts = expenseString.split("|")
            if (parts.size == 3) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("DashboardFragment", "Invalid date format in expense: ${parts[0]}")
                    null
                }
                if (date != null) {
                    transactions.add(
                        Transaction(
                            type = TransactionType.EXPENSE,
                            date = parts[0],
                            categoryOrReason = parts[1],
                            amount = parts[2],
                            description = null
                        )
                    )
                }
            } else {
                Log.e("DashboardFragment", "Invalid expense format: $expenseString")
            }
        }

        // Load income
        val savedIncomes = incomePrefs.getStringSet("incomes", mutableSetOf())?.toList() ?: listOf()
        Log.d("DashboardFragment", "All incomes loaded: $savedIncomes")
        for (incomeString in savedIncomes) {
            val parts = incomeString.split("|")
            if (parts.size == 4) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("DashboardFragment", "Invalid date format in income: ${parts[0]}")
                    null
                }
                if (date != null) {
                    transactions.add(
                        Transaction(
                            type = TransactionType.INCOME,
                            date = parts[0],
                            categoryOrReason = parts[1],
                            amount = parts[2],
                            description = parts[3]
                        )
                    )
                }
            } else {
                Log.e("DashboardFragment", "Invalid income format: $incomeString")
            }
        }

        // Sort by date (descending)
        return transactions.sortedByDescending { dateFormat.parse(it.date) }
    }

    private fun calculateBalance(): Double {
        val transactions = loadAllTransactions()
        var balance = 0.0
        for (transaction in transactions) {
            try {
                val amountStr = transaction.amount.replace("[^0-9.]".toRegex(), "")
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                balance += if (transaction.type == TransactionType.INCOME) amount else -amount
            } catch (e: Exception) {
                Log.e("DashboardFragment", "Error parsing amount: ${transaction.amount}", e)
            }
        }
        Log.d("DashboardFragment", "Calculated balance (LKR): $balance")
        return balance
    }

    private fun updateBalance() {
        val balance = calculateBalance()
        val currency = settingsPrefs.getString("currency", "LKR") ?: "LKR"
        // Convert balance based on currency
        val convertedBalance = when (currency) {
            "USD" -> balance / 300.0 // Example: 1 LKR = 0.0033 USD
            "EUR" -> balance / 360.0 // Example: 1 LKR = 0.0028 EUR
            else -> balance // LKR
        }
        // Format balance with currency symbol
        val formattedBalance = when (currency) {
            "USD" -> String.format("$%.2f", convertedBalance)
            "EUR" -> String.format("€%.2f", convertedBalance)
            else -> String.format("%.2f LKR", convertedBalance)
        }
        binding.expenseTrackerAmount.text = formattedBalance
        binding.expenseTrackerAmount.setTextColor(
            if (balance >= 0) android.graphics.Color.parseColor("#00FF00")
            else android.graphics.Color.parseColor("#FF0000")
        )
        Log.d("DashboardFragment", "Balance: $balance (LKR) -> $convertedBalance ($currency) -> $formattedBalance")
    }

    private fun refreshData() {
        transactionAdapter.updateTransactions(loadRecentTransactions())
        updateBalance()
    }

    private fun setupButtonListeners() {
        binding.apply {
            expensesButton.setOnClickListener {
                android.widget.Toast.makeText(requireContext(), "Expenses clicked", android.widget.Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), ExpenseActivity::class.java))
            }
            incomeButton.setOnClickListener {
                android.widget.Toast.makeText(requireContext(), "Income clicked", android.widget.Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), IncomeActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}