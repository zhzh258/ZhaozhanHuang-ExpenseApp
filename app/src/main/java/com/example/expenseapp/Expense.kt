package com.example.expenseapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

//a. Use Room to store expenses.
//b. Each expense should include:
//i. Date
//ii. Amount
//iii. Category (Food, Entertainment, Housing, Utilities, Fuel, Automotive, Misc)
//c. Show a categorized list of expenses in a RecyclerView within a fragment.
//d. Allow filtering expenses by date or category.
//e. Navigate to a separate fragment for adding or editing an expense.

@Entity
data class Expense(
    @PrimaryKey val id: UUID,
    val category: Category,
    val title: String,
    val date: Date,
    val amount: Float,
    val description: String?,
)

enum class Category {
    Food,
    Entertainment,
    Housing,
    Utilities,
    Fuel,
    Automotive,
    Misc
}

enum class TimeRange {
    OneDay,
    ThreeDay,
    OneWeek
}