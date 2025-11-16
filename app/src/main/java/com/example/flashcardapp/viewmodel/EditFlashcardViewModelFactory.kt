package com.example.flashcardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.data.FlashcardRepository

class EditFlashcardViewModelFactory(
    private val repository: FlashcardRepository,
    private val flashcardId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditFlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditFlashcardViewModel(repository, flashcardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
