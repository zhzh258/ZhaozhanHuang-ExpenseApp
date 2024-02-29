package com.example.expenseapp

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.expenseapp.database.ExpenseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

const val DATABASE_NAME ="expense-database"
// singleton pattern. Remember to initialize it in ExpenseApp.kt
// initInstance(): initialize the singleton object
// getInstance(): get the singleton object
class ExpenseRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope,
){

    private var database: ExpenseDatabase = createDatabase(context)

    private fun createDatabase(context: Context): ExpenseDatabase {
        context.deleteDatabase(DATABASE_NAME)
        val db = Room.databaseBuilder(
            context.applicationContext,
            ExpenseDatabase::class.java,
            DATABASE_NAME
        ).build()
        Log.d("MyDebug", "Successfully created the db...")

        return db
    }


    companion object {
        private var INSTANCE: ExpenseRepository? = null

        fun initInstance(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ExpenseRepository(context)
            }
        }

        fun getInstance(): ExpenseRepository {
            return INSTANCE
                ?: throw IllegalStateException("ExpenseRepository must be initialized")
        }
    }

    suspend fun fetchExpenseByCategory(category: Category? = null): Flow<List<Expense>> {
        Log.d("MyDebug", "repo.fetchExpenseByCategory()")
        return if (category == null) {
            database.expenseDao().getFlowAll()
        } else {
            database.expenseDao().getFlowByCategory(category)
        }
    }

    suspend fun fetchExpenseById(id: UUID): Flow<Expense> {
        Log.d("MyDebug", "repo.fetchExpenseById()")
        return database.expenseDao().getFlowById(id)
    }

    suspend fun insertExpense(expense: Expense) {
        Log.d("MyDebug", "repo.addExpense()")
        database.expenseDao().insert(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        Log.d("MyDebug", "repo.updateExpense()")
        database.expenseDao().update(expense)
    }

    suspend fun upsertExpense(expense: Expense) {
        val existingExpense = database.expenseDao().getById(expense.id)
        if (existingExpense == null) {
            // Expense does not exist, so insert
            Log.d("MyDebug", "Inserting new expense")
            database.expenseDao().insert(expense)
        } else {
            // Expense exists, so update
            Log.d("MyDebug", "Updating existing expense")
            database.expenseDao().update(expense)
        }
    }

    fun fetchEmptyExpense(): Flow<Expense> = flow {
        val expense = Expense(
            id = java.util.UUID.randomUUID(),
            category = Category.Food,
            title = "",
            date = Date(),
            amount = 0.0f,
            description = ""
        )
        emit(expense)
    }
}

