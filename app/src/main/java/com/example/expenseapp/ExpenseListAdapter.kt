package com.example.expenseapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.expenseapp.databinding.FragmentExpenseListBinding
import com.example.expenseapp.databinding.ListItemExpenseBinding
import java.util.UUID
import kotlin.math.exp

class ExpenseHolder(private val binding: ListItemExpenseBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(expense: Expense, onItemClicked: (expenseId: UUID) -> Unit) {
        binding.expenseTitle.text = expense.title
        binding.expenseAmount.text = binding.root.context.getString(R.string.expense_amount, expense.amount.toString())
        binding.expenseDate.text = binding.root.context.getString(R.string.expense_date, expense.date.toString())

        binding.root.setOnClickListener {
            onItemClicked(expense.id)
        }
    }
}

class ExpenseListAdapter(
    private val expenses: List<Expense>,
    private val onItemClicked: (expenseId: UUID) -> Unit
): RecyclerView.Adapter<ExpenseHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemExpenseBinding.inflate(inflater, parent, false)
        return ExpenseHolder(binding)
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onBindViewHolder(holder: ExpenseHolder, position: Int) {
        holder.bind(expenses[position], onItemClicked)
    }
}

