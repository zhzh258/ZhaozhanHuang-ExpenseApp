package com.example.expenseapp.database

import androidx.room.TypeConverter
import com.example.expenseapp.Category
import java.util.Date
import java.util.UUID

class ExpenseTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromUUID(uuid: String?): UUID? = UUID.fromString(uuid)

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun fromCategory(value: String) = enumValueOf<Category>(value)

    @TypeConverter
    fun categoryToString(category: Category) = category.name
}
