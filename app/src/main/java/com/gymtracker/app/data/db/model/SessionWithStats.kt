package com.gymtracker.app.data.db.model

data class SessionWithStats(
    val id: Long,
    val planId: Long?,
    val planName: String?,
    val startedAt: Long,
    val finishedAt: Long?,
    val exerciseCount: Int,
    val volume: Double
)
