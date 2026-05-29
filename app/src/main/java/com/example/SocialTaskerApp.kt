package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.AppRepository

class SocialTaskerApp : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.taskDao(), database.socialMessageDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
