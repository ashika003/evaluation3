package com.example.evaluation3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evaluation3.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtonListeners()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        val transactions = listOf(
            Transaction("2025-04-18", "Grocery Shopping", "-$45.30"),
            Transaction("2025-04-18", "Salary", "+$1,500.00"),
            Transaction("2025-04-19", "Coffee", "-$5.20"),
            Transaction("2025-04-20", "Freelance Payment", "+$300.00")
        )

        transactionAdapter = TransactionAdapter(transactions)
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupButtonListeners() {
        binding.expensesButton.setOnClickListener {
            Toast.makeText(this, "Expenses clicked", Toast.LENGTH_SHORT).show()
        }

        binding.incomeButton.setOnClickListener {
            Toast.makeText(this, "Income clicked", Toast.LENGTH_SHORT).show()
        }

        binding.savingsButton.setOnClickListener {
            Toast.makeText(this, "Savings clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_transactions -> {
                    Toast.makeText(this, "Transactions clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_statistics -> {
                    Toast.makeText(this, "Statistics clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}

// Transaction data class remains the same
data class Transaction(val date: String, val description: String, val amount: String)

// Improved TransactionAdapter
class TransactionAdapter(private val transactions: List<Transaction>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: android.view.View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val dateText: android.widget.TextView = itemView.findViewById(R.id.date_text)
        val descriptionText: android.widget.TextView = itemView.findViewById(R.id.description_text)
        val amountText: android.widget.TextView = itemView.findViewById(R.id.amount_text)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TransactionViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.dateText.text = transaction.date
        holder.descriptionText.text = transaction.description
        holder.amountText.text = transaction.amount

        // Set different colors for income/expense
        val color = if (transaction.amount.startsWith("+")) {
            android.graphics.Color.GREEN
        } else {
            android.graphics.Color.RED
        }
        holder.amountText.setTextColor(color)
    }

    override fun getItemCount(): Int = transactions.size
}