package com.example.evaluation3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: android.widget.TextView = itemView.findViewById(R.id.transactionDate)
        val categoryOrReasonText: android.widget.TextView = itemView.findViewById(R.id.transactionCategoryOrReason)
        val amountText: android.widget.TextView = itemView.findViewById(R.id.transactionAmount)
        val descriptionText: android.widget.TextView = itemView.findViewById(R.id.transactionDescription)

        init {
            itemView.setOnClickListener {
                onItemClick(transactions[adapterPosition])
            }
        }
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
            categoryOrReasonText.text = transaction.categoryOrReason
            amountText.text = transaction.amount
            descriptionText.text = transaction.description ?: ""
            descriptionText.visibility = if (transaction.description.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Set color based on transaction type
            val color = when (transaction.type) {
                TransactionType.INCOME -> android.graphics.Color.parseColor("#00FF00")
                TransactionType.EXPENSE -> android.graphics.Color.parseColor("#FF0000")
            }
            amountText.setTextColor(color)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}