package com.example.evaluation3

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var paymentTypeSpinner: Spinner
    private lateinit var dateInput: EditText
    private lateinit var reasonInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var submitButton: Button
    private lateinit var recentExpensesList: ListView
    private val expensesList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_acrivity)

        // Initialize UI components
        amountInput = findViewById(R.id.amountInput)
        paymentTypeSpinner = findViewById(R.id.paymentTypeSpinner)
        dateInput = findViewById(R.id.dateInput)
        reasonInput = findViewById(R.id.reasonInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        submitButton = findViewById(R.id.submitButton)
        recentExpensesList = findViewById(R.id.recentExpensesList)

        // Set up the ListView adapter
        adapter = ExpenseAdapter(this, expensesList)
        recentExpensesList.adapter = adapter

        // Load saved expenses
        loadExpenses()

        // Set up Date Picker
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    dateInput.setText(dateFormat.format(selectedDate.time))
                },
                year,
                month,
                day
            )
            // Restrict future dates
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Set up Submit button
        submitButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val paymentType = paymentTypeSpinner.selectedItem.toString()
            val date = dateInput.text.toString()
            val reason = reasonInput.text.toString()
            val description = descriptionInput.text.toString()

            if (amount.isNotEmpty() && date.isNotEmpty()) {
                val expense = Expense(date, reason, "-$amount LKR")
                expensesList.add(expense)
                adapter.notifyDataSetChanged()

                // Save to SharedPreferences
                saveExpense(expense)

                // Clear inputs
                amountInput.text.clear()
                dateInput.text.clear()
                reasonInput.text.clear()
                descriptionInput.text.clear()
            }
        }
    }

    private fun saveExpense(expense: Expense) {
        val sharedPreferences = getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val expensesSet = sharedPreferences.getStringSet("expenses", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        expensesSet.add("${expense.date}|${expense.reason}|${expense.amount}")
        editor.putStringSet("expenses", expensesSet)
        editor.apply()
    }

    private fun loadExpenses() {
        val sharedPreferences = getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)
        val savedExpenses = sharedPreferences.getStringSet("expenses", mutableSetOf())?.toList() ?: listOf()
        expensesList.clear()
        for (expenseString in savedExpenses) {
            val parts = expenseString.split("|")
            if (parts.size == 3) {
                expensesList.add(Expense(parts[0], parts[1], parts[2]))
            }
        }
        adapter.notifyDataSetChanged()
    }
}

data class Expense(
    val date: String,
    val reason: String,
    val amount: String
)

class ExpenseAdapter(context: Context, private val expenses: List<Expense>) :
    ArrayAdapter<Expense>(context, 0, expenses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_expense, parent, false)

        val expense = getItem(position)

        val dateText = view.findViewById<TextView>(R.id.expenseDate)
        val reasonText = view.findViewById<TextView>(R.id.expenseReason)
        val amountText = view.findViewById<TextView>(R.id.expenseAmount)

        dateText.text = expense?.date
        reasonText.text = expense?.reason
        amountText.text = expense?.amount

        return view
    }
}