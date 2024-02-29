package com.example.expenseapp

import android.app.Application
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class ExpenseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // db is created in ApplicationContext because I pass this to ExpenseRepository
        ExpenseRepository.initInstance(this)
        Log.d("MyDebug", "Successfully init the instance of ExpenseRepository...")
        loadDummyDataToDatabase()
    }

    private fun loadDummyDataToDatabase() {
        GlobalScope.launch {
            Category.values().map { it: Category ->
                val date = Date() // Current date and time

                ExpenseRepository.getInstance().insertExpense(
                    Expense(
                        id = UUID.randomUUID(),
                        category = it,
                        title = "Example ${it.name} (Just now)",
                        amount = Random.nextFloat() * (100 - 10) + 10,
                        date = date,
                        description = "This is the example description of a ${it.name} expense"
                    )
                )
            }
            Category.values().map { it: Category ->
                val date = Date() // Current date and time
                val oneDayInMillis = 24 * 60 * 60 * 1000 // Hours * Minutes * Seconds * Milliseconds
                date.time = date.time - oneDayInMillis * 2

                ExpenseRepository.getInstance().insertExpense(
                Expense(
                        id = UUID.randomUUID(),
                        category = it,
                        title = "Example ${it.name} (Two days ago)",
                        amount = Random.nextFloat() * (100 - 10) + 10,
                        date = date,
                        description = "This is the example description of a food expense"
                    )
                )
            }
            Category.values().map { it: Category ->
                val date = Date() // Current date and time
                val oneDayInMillis = 24 * 60 * 60 * 1000 // Hours * Minutes * Seconds * Milliseconds
                date.time = date.time - oneDayInMillis * 6

                ExpenseRepository.getInstance().insertExpense(
                    Expense(
                        id = UUID.randomUUID(),
                        category = it,
                        title = "Example ${it.name} (Six days ago)",
                        amount = Random.nextFloat() * (100 - 10) + 10,
                        date = date,
                        description = "This is the example description of a food expense"
                    )
                )
            }
        }
    }
}