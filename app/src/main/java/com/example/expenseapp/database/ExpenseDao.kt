package com.example.expenseapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import com.example.expenseapp.Category
import kotlinx.coroutines.flow.Flow
import com.example.expenseapp.Expense
import java.util.UUID

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense")
    fun getFlowAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id=(:id)")
    fun getFlowById(id: UUID): Flow<Expense>

    @Query("SELECT * FROM expense WHERE category=(:category)")
    fun getFlowByCategory(category: Category): Flow<List<Expense>>

    @Query("SELECT * FROM expense")
    fun getAll(): List<Expense>?

    @Query("SELECT * FROM expense WHERE id=(:id)")
    fun getById(id: UUID): Expense?

    @Query("SELECT * FROM expense WHERE category=(:category)")
    fun getByCategory(category: Category): List<Expense>?


    @Update
    suspend fun update(expense: Expense)

    @Insert
    suspend fun insert(expense: Expense)
}