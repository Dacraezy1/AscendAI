package com.ascendai.app.model

data class DailyHabit(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconName: String,
    val isCompleted: Boolean = false,
    val streakDays: Int = 0
)
