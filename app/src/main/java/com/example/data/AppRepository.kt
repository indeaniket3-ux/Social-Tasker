package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val taskDao: TaskDao,
    private val socialMessageDao: SocialMessageDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allMessages: Flow<List<SocialMessage>> = socialMessageDao.getAllMessages()

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) {
        taskDao.updateTaskStatus(id, isCompleted)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun insertMessage(message: SocialMessage) {
        socialMessageDao.insertMessage(message)
    }

    suspend fun markMessageRead(id: Int) {
        socialMessageDao.markAsRead(id)
    }

    suspend fun markAllMessagesRead() {
        socialMessageDao.markAllAsRead()
    }

    suspend fun clearSocialMessages() {
        socialMessageDao.clearAllMessages()
    }
}
