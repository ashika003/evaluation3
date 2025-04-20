package com.example.evaluation3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evaluation3.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    // ViewBinding
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Adapter
    private lateinit var transactionAdapter: TransactionAdapter

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
        setupRecyclerView()
        setupButtonListeners()
    }

    private fun setupRecyclerView() {
        // Sample data
        val transactions = listOf(
            Transaction("2025-04-18", "Grocery Shopping", "-$45.30", TransactionType.EXPENSE),
            Transaction("2025-04-18", "Salary", "+$1,500.00", TransactionType.INCOME),
            Transaction("2025-04-19", "Coffee", "-$5.20", TransactionType.EXPENSE),
            Transaction("2025-04-20", "Freelance Payment", "+$300.00", TransactionType.INCOME)
        )

        transactionAdapter = TransactionAdapter(transactions)
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupButtonListeners() {
        binding.apply {
            expensesButton.setOnClickListener {
                showToast("Expenses clicked")
                startActivity(Intent(requireContext(), ExpenseActivity::class.java))
            }
            incomeButton.setOnClickListener {
                showToast("Income clicked")
            }
            savingsButton.setOnClickListener {
                showToast("Savings clicked")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Add these classes if they don't exist in your project:

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val date: String,
    val description: String,
    val amount: String,
    val type: TransactionType
)

class TransactionAdapter(
    private val transactions: List<Transaction>
) : androidx.recyclerview.widget.RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        // Make sure these IDs match your item_transaction.xml
        val dateText: android.widget.TextView = itemView.findViewById(R.id.date_text)
        val descriptionText: android.widget.TextView = itemView.findViewById(R.id.description_text)
        val amountText: android.widget.TextView = itemView.findViewById(R.id.amount_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.apply {
            dateText.text = transaction.date
            descriptionText.text = transaction.description
            amountText.text = transaction.amount

            // Set color based on transaction type
            val color = when (transaction.type) {
                TransactionType.INCOME -> android.graphics.Color.GREEN
                TransactionType.EXPENSE -> android.graphics.Color.RED
            }
            amountText.setTextColor(color)
        }
    }

    override fun getItemCount() = transactions.size
}