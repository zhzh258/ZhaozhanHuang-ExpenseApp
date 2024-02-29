package com.example.expenseapp

import android.os.Parcel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date

class ExpenseListViewModel() : ViewModel() {
    private val _expenses: MutableStateFlow<List<Expense>> = MutableStateFlow(emptyList())
    val expenses: StateFlow<List<Expense>>
        get() = _expenses.asStateFlow()

    private val repo = ExpenseRepository.getInstance()

    var selection1: Category? = null
    var selection2: TimeRange? = null
    init {
        // coroutine: get the Flow<List<Expense>> from DB
        viewModelScope.launch {
//            Log.d("MyDebug", "launching expenseListViewModel...")
            repo.fetchExpenseByCategory(null).collect {
                _expenses.value = it
            }
        }

        // I can add other coroutines here if needed
    }

    fun getExpensesData(category: Category? = null, startDate: Date? = null) {
        viewModelScope.launch {
            repo.fetchExpenseByCategory(category).collect() { li: List<Expense> ->
                if (startDate == null) { // any date range
                    _expenses.value = li
                } else { // Today, In three days, In this week
                    val filteredExpenses = li.filter { e: Expense ->
                        e.date.after(startDate)
                    }
                    Log.d("SpinnerDebug", "${filteredExpenses.map { e -> e.title }}")
                    _expenses.value = filteredExpenses
                }

            }
        }
    }


}