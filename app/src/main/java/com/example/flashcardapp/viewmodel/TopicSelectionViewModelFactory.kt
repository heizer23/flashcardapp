package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.data.FlashcardRepository

class TopicSelectionViewModelFactory(
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicSelectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
