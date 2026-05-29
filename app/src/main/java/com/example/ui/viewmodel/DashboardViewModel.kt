package com.example.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.SocialTaskerApp
import com.example.data.AppRepository
import com.example.data.SocialMessage
import com.example.data.Task
import com.example.services.NotificationMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: AppRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val socialMessages: StateFlow<List<SocialMessage>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isListenerPermissionGranted = MutableStateFlow(false)
    val isListenerPermissionGranted: StateFlow<Boolean> = _isListenerPermissionGranted.asStateFlow()

    fun checkListenerPermission(context: Context) {
        val cn = ComponentName(context, NotificationMonitorService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val enabled = flat != null && flat.contains(cn.flattenToString())
        _isListenerPermissionGranted.value = enabled
    }

    fun addTask(title: String, category: String, priority: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                category = category,
                priority = priority,
                isCompleted = false
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompleteness(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun simulateIncomingMessage(sender: String, text: String, platform: String) {
        viewModelScope.launch {
            val message = SocialMessage(
                sender = sender,
                text = text,
                platform = platform,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            repository.insertMessage(message)
        }
    }

    fun markMessageAsRead(id: Int) {
        viewModelScope.launch {
            repository.markMessageRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllMessagesRead()
        }
    }

    fun clearAllSocialMessages() {
        viewModelScope.launch {
            repository.clearSocialMessages()
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as SocialTaskerApp
                return DashboardViewModel(app.repository) as T
            }
        }
    }
}
