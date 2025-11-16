package com.example.flashcardapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.TopicWithCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicSelectionViewModel(private val repository: FlashcardRepository) : ViewModel() {

    // changes: Instead of a list of Topic, we track a list of TopicWithCount.
    private val _topicsWithCount = MutableLiveData<List<TopicWithCount>>(emptyList())
    val topicsWithCount: LiveData<List<TopicWithCount>> get() = _topicsWithCount

    // We'll load them in one query.
    fun loadTopics() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllTopicsWithCount()
            withContext(Dispatchers.Main) {
                _topicsWithCount.value = result
            }
        }
    }

    // We'll keep the same update logic, but we have to pass topic IDs.
    fun updateSelectedTopics(list: List<TopicWithCount>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (twc in list) {
                repository.updateTopicSelection(twc.id, twc.selected)
            }
        }
    }
}
