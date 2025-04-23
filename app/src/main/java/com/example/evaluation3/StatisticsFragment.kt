package com.example.evaluation3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.evaluation3.databinding.FragmentStatisticsBinding
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var expensePrefs: SharedPreferences
    private lateinit var incomePrefs: SharedPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensePrefs = requireContext().getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        setupCharts()
    }

    private fun setupCharts() {
        val transactions = loadAllTransactions()
        setupIncomeExpensePieChart(transactions)
        setupCategoryPieChart(transactions)
        setupMonthlyBalanceBarChart(transactions)
    }

    private fun loadAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        // Load expenses
        val savedExpenses = expensePrefs.getStringSet("expenses", mutableSetOf())?.toList() ?: listOf()
        for (expenseString in savedExpenses) {
            val parts = expenseString.split("|")
            if (parts.size == 3) {
                try {
                    dateFormat.parse(parts[0])?.let {
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
                } catch (e: Exception) {}
            }
        }

        // Load income
        val savedIncomes = incomePrefs.getStringSet("incomes", mutableSetOf())?.toList() ?: listOf()
        for (incomeString in savedIncomes) {
            val parts = incomeString.split("|")
            if (parts.size == 4) {
                try {
                    dateFormat.parse(parts[0])?.let {
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
                } catch (e: Exception) {}
            }
        }

        return transactions.sortedByDescending { dateFormat.parse(it.date) }
    }

    private fun setupIncomeExpensePieChart(transactions: List<Transaction>) {
        var incomeTotal = 0.0
        var expenseTotal = 0.0

        transactions.forEach { transaction ->
            val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            if (transaction.type == TransactionType.INCOME) {
                incomeTotal += amount
            } else {
                expenseTotal += amount
            }
        }

        val entries = listOf(
            ChartEntry("Income", incomeTotal.toFloat(), android.graphics.Color.parseColor("#4CAF50")),
            ChartEntry("Expenses", expenseTotal.toFloat(), android.graphics.Color.parseColor("#F44336"))
        )

        binding.incomeExpensePieChart.setData(entries)
    }

    private fun setupCategoryPieChart(transactions: List<Transaction>) {
        val categoryMap = mutableMapOf<String, Double>()
        val colors = listOf(
            android.graphics.Color.parseColor("#2196F3"), // Blue
            android.graphics.Color.parseColor("#FF9800"), // Orange
            android.graphics.Color.parseColor("#9C27B0"), // Purple
            android.graphics.Color.parseColor("#009688"), // Teal
            android.graphics.Color.parseColor("#FFC107")  // Amber
        )

        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
            val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val category = transaction.categoryOrReason
            categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + amount
        }

        val entries = categoryMap.entries.mapIndexed { index, entry ->
            ChartEntry(entry.key, entry.value.toFloat(), colors[index % colors.size])
        }

        binding.categoryPieChart.setData(entries)
    }

    private fun setupMonthlyBalanceBarChart(transactions: List<Transaction>) {
        val monthlyBalances = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            try {
                val date = dateFormat.parse(transaction.date)
                val monthKey = monthFormat.format(date)
                val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                val currentBalance = monthlyBalances.getOrDefault(monthKey, 0f)
                monthlyBalances[monthKey] = currentBalance +
                        (if (transaction.type == TransactionType.INCOME) amount else -amount).toFloat()
            } catch (e: Exception) {}
        }

        val entries = monthlyBalances.entries.sortedBy { monthFormat.parse(it.key) }.map { entry ->
            BarChartEntry(entry.key, entry.value, android.graphics.Color.parseColor("#2196F3"))
        }

        binding.monthlyBalanceBarChart.setData(entries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}