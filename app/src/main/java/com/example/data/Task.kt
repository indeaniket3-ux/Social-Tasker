package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String, // "Work", "Personal", "Social", "Other"
    val priority: String, // "High", "Medium", "Low"
    val createdAt: Long = System.currentTimeMillis()
)
