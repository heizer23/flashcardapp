package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.data.FlashcardRepository

class ImportFlashcardsViewModelFactory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportFlashcardsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportFlashcardsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
