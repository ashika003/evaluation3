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

class IncomeActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var submitButton: Button
    private lateinit var recentIncomeList: ListView
    private val incomeList = mutableListOf<Income>()
    private lateinit var adapter: IncomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        // Initialize UI components
        amountInput = findViewById(R.id.amountInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        dateInput = findViewById(R.id.dateInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        submitButton = findViewById(R.id.submitButton)
        recentIncomeList = findViewById(R.id.recentIncomeList)

        // Set up the ListView adapter
        adapter = IncomeAdapter(this, incomeList)
        recentIncomeList.adapter = adapter

        // Load saved income
        loadIncome()

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
            val category = categorySpinner.selectedItem.toString()
            val date = dateInput.text.toString()
            val description = descriptionInput.text.toString()

            if (amount.isNotEmpty() && date.isNotEmpty()) {
                val income = Income(date, category, "+$amount LKR", description)
                incomeList.add(income)
                adapter.notifyDataSetChanged()

                // Save to SharedPreferences
                saveIncome(income)

                // Clear inputs
                amountInput.text.clear()
                dateInput.text.clear()
                descriptionInput.text.clear()
            }
        }
    }

    private fun saveIncome(income: Income) {
        val sharedPreferences = getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val incomeSet = sharedPreferences.getStringSet("incomes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        incomeSet.add("${income.date}|${income.category}|${income.amount}|${income.description}")
        editor.putStringSet("incomes", incomeSet)
        editor.apply()
    }

    private fun loadIncome() {
        val sharedPreferences = getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val savedIncomes = sharedPreferences.getStringSet("incomes", mutableSetOf())?.toList() ?: listOf()
        incomeList.clear()
        for (incomeString in savedIncomes) {
            val parts = incomeString.split("|")
            if (parts.size == 4) {
                incomeList.add(Income(parts[0], parts[1], parts[2], parts[3]))
            }
        }
        adapter.notifyDataSetChanged()
    }
}

data class Income(
    val date: String,
    val category: String,
    val amount: String,
    val description: String
)

class IncomeAdapter(context: Context, private val incomes: List<Income>) :
    ArrayAdapter<Income>(context, 0, incomes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_income, parent, false)

        val income = getItem(position)

        val dateText = view.findViewById<TextView>(R.id.incomeDate)
        val categoryText = view.findViewById<TextView>(R.id.incomeCategory)
        val amountText = view.findViewById<TextView>(R.id.incomeAmount)
        val descriptionText = view.findViewById<TextView>(R.id.incomeDescription)

        dateText.text = income?.date
        categoryText.text = income?.category
        amountText.text = income?.amount
        descriptionText.text = income?.description

        return view
    }
}