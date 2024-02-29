package com.example.expenseapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenseapp.Expense
import com.example.expenseapp.databinding.FragmentExpenseDetailBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

// This ViewModel accepts a string as parameter. This is unusual and can be done by ViewModelProvider.Factory
// The @argument expenseId can either be UUID or null.
class ExpenseDetailViewModel(expenseId: UUID?): ViewModel() {
    private val _expense: MutableStateFlow<Expense?> = MutableStateFlow(null)
    val expense: StateFlow<Expense?> = _expense.asStateFlow()

    val repo = ExpenseRepository.getInstance()

    init {
        // This coroutine initialize this._expense based on the argument passed to ExpenseDetailFragment
        viewModelScope.launch {
            Log.d("ExpenseDetailViewModelDebug", "Now initialize viewmodel._expense. expenseId == $expenseId")
            if (expenseId == null) { // navigated by menu_add_button
                repo.fetchEmptyExpense().collect() {
                    _expense.value = it
                    Log.d("MyDebug", "expenseDetailViewModel is init... expense.value after fetching is ${expense.value}")
                }
            } else { // navigated by item
                repo.fetchExpenseById(expenseId).collect {
                    _expense.value = it
                    Log.d("MyDebug", "expenseDetailViewModel is init... expense.value after fetching is ${expense.value}")
                }
            }
        }
        // I can add other coroutines here if needed
    }

    override fun onCleared() {
        Log.d("MyDebug", "expenseDetailViewModel is cleared... expense.value is ${expense.value}")
        super.onCleared()
        // Sync the data to DB when the view model is cleared
        GlobalScope.launch {
            expense.value?.let { repo.upsertExpense(it) }
        }
    }

    /**
     * This function will be called in ExpenseDetailFragment whenever _expense need to be changed
     * this._expense will only be changed when this._expense != null
     * @param onUpdate An callback function. How to change current _expense
     */
    fun updateExpenseData(onUpdate: (Expense) -> Expense) {
        _expense.update { oldExpense ->
            Log.d("MyDebug", "Now updating the flow in viewmodel...")
            oldExpense?.let { onUpdate(it) }
        }
    }
}

class ExpenseDetailViewModelFactory(private val expenseId: UUID?): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseDetailViewModel(expenseId) as T
    }
}