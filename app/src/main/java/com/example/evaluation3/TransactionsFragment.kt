package com.example.evaluation3

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evaluation3.databinding.FragmentTransactionsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var expensePrefs: SharedPreferences
    private lateinit var incomePrefs: SharedPreferences
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        loadTransactions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensePrefs = requireContext().getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        setupRecyclerView()
        setupFilterSpinner()
        loadTransactions()
    }

    override fun onStart() {
        super.onStart()
        expensePrefs.registerOnSharedPreferenceChangeListener(prefsListener)
        incomePrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        expensePrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        incomePrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList()) { transaction ->
            showEditDeleteDialog(transaction)
        }
        binding.transactionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterSpinner() {
        binding.filterSpinner.adapter = android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFilter = parent.getItemAtPosition(position).toString()
                val allTransactions = loadAllTransactions()
                val filteredTransactions = when (selectedFilter) {
                    "All Transactions" -> allTransactions
                    "Income" -> allTransactions.filter { it.type == TransactionType.INCOME }
                    "Expenses" -> allTransactions.filter { it.type == TransactionType.EXPENSE }
                    else -> allTransactions
                }
                transactionAdapter.updateTransactions(filteredTransactions)
                updateBalance(filteredTransactions)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                val allTransactions = loadAllTransactions()
                transactionAdapter.updateTransactions(allTransactions)
                updateBalance(allTransactions)
            }
        }
    }

    private fun loadAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        // Load expenses
        val savedExpenses = expensePrefs.getStringSet("expenses", mutableSetOf())?.toList() ?: listOf()
        Log.d("TransactionsFragment", "All expenses loaded: $savedExpenses")
        for (expenseString in savedExpenses) {
            val parts = expenseString.split("|")
            if (parts.size == 3) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("TransactionsFragment", "Invalid date format in expense: ${parts[0]}")
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
                Log.e("TransactionsFragment", "Invalid expense format: $expenseString")
            }
        }

        // Load income
        val savedIncomes = incomePrefs.getStringSet("incomes", mutableSetOf())?.toList() ?: listOf()
        Log.d("TransactionsFragment", "All incomes loaded: $savedIncomes")
        for (incomeString in savedIncomes) {
            val parts = incomeString.split("|")
            if (parts.size == 4) {
                val date = try {
                    dateFormat.parse(parts[0])
                } catch (e: Exception) {
                    Log.e("TransactionsFragment", "Invalid date format in income: ${parts[0]}")
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
                Log.e("TransactionsFragment", "Invalid income format: $incomeString")
            }
        }

        // Sort by date (descending)
        return transactions.sortedByDescending { dateFormat.parse(it.date) }
    }

    private fun loadTransactions() {
        val transactions = loadAllTransactions()
        transactionAdapter.updateTransactions(transactions)
        updateBalance(transactions)
    }

    private fun calculateBalance(transactions: List<Transaction>): Double {
        var balance = 0.0
        for (transaction in transactions) {
            try {
                val amountStr = transaction.amount.replace("[^0-9.]".toRegex(), "")
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                balance += if (transaction.type == TransactionType.INCOME) amount else -amount
            } catch (e: Exception) {
                Log.e("TransactionsFragment", "Error parsing amount: ${transaction.amount}", e)
            }
        }
        Log.d("TransactionsFragment", "Calculated balance: $balance")
        return balance
    }

    private fun updateBalance(transactions: List<Transaction>) {
        val balance = calculateBalance(transactions)
        binding.balanceText.text = getString(R.string.balance_label, String.format("%.2f LKR", balance))
        binding.balanceText.setTextColor(
            if (balance >= 0) android.graphics.Color.parseColor("#00FF00")
            else android.graphics.Color.parseColor("#FF0000")
        )
        Log.d("TransactionsFragment", "Balance updated: ${binding.balanceText.text}")
    }

    private fun saveTransaction(transaction: Transaction) {
        val sharedPreferences = requireContext().getSharedPreferences(
            if (transaction.type == TransactionType.INCOME) "IncomePrefs" else "ExpensesPrefs",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val key = if (transaction.type == TransactionType.INCOME) "incomes" else "expenses"
        val transactionSet = sharedPreferences.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // Remove old transaction if editing
        transactionSet.removeIf {
            val parts = it.split("|")
            if (transaction.type == TransactionType.INCOME && parts.size == 4) {
                parts[0] == transaction.date && parts[1] == transaction.categoryOrReason && parts[2] == transaction.amount && parts[3] == transaction.description
            } else if (transaction.type == TransactionType.EXPENSE && parts.size == 3) {
                parts[0] == transaction.date && parts[1] == transaction.categoryOrReason && parts[2] == transaction.amount
            } else false
        }

        // Add new transaction
        val transactionString = if (transaction.type == TransactionType.INCOME) {
            "${transaction.date}|${transaction.categoryOrReason}|${transaction.amount}|${transaction.description}"
        } else {
            "${transaction.date}|${transaction.categoryOrReason}|${transaction.amount}"
        }
        transactionSet.add(transactionString)
        editor.putStringSet(key, transactionSet)
        editor.apply()
        loadTransactions()
    }

    private fun deleteTransaction(transaction: Transaction) {
        val sharedPreferences = requireContext().getSharedPreferences(
            if (transaction.type == TransactionType.INCOME) "IncomePrefs" else "ExpensesPrefs",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val key = if (transaction.type == TransactionType.INCOME) "incomes" else "expenses"
        val transactionSet = sharedPreferences.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        transactionSet.removeIf {
            val parts = it.split("|")
            if (transaction.type == TransactionType.INCOME && parts.size == 4) {
                parts[0] == transaction.date && parts[1] == transaction.categoryOrReason && parts[2] == transaction.amount && parts[3] == transaction.description
            } else if (transaction.type == TransactionType.EXPENSE && parts.size == 3) {
                parts[0] == transaction.date && parts[1] == transaction.categoryOrReason && parts[2] == transaction.amount
            } else false
        }

        editor.putStringSet(key, transactionSet)
        editor.apply()
        loadTransactions()
    }

    private fun showEditDeleteDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_transaction, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.dialogAmountInput)
        val categoryOrReasonInput = dialogView.findViewById<EditText>(R.id.dialogCategoryOrReasonInput)
        val dateInput = dialogView.findViewById<EditText>(R.id.dialogDateInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.dialogDescriptionInput)
        val saveButton = dialogView.findViewById<Button>(R.id.dialogSaveButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.dialogDeleteButton)

        // Populate fields
        amountInput.setText(transaction.amount.replace("[+-]".toRegex(), "").replace(" LKR", ""))
        categoryOrReasonInput.setText(transaction.categoryOrReason)
        dateInput.setText(transaction.date)
        if (transaction.type == TransactionType.INCOME) {
            descriptionInput.setText(transaction.description)
        } else {
            descriptionInput.visibility = View.GONE
        }

        // Date picker
        val calendar = Calendar.getInstance()
        dateInput.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    dateInput.setText(dateFormat.format(selectedDate.time))
                },
                year, month, day
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val updatedTransaction = Transaction(
                transaction.type,
                dateInput.text.toString(),
                categoryOrReasonInput.text.toString(),
                if (transaction.type == TransactionType.INCOME) "+${amountInput.text} LKR" else "-${amountInput.text} LKR",
                if (transaction.type == TransactionType.INCOME) descriptionInput.text.toString() else null
            )
            saveTransaction(updatedTransaction)
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteTransaction(transaction)
            dialog.dismiss()
        }

        dialog.show()
    }
}