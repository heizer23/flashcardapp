package com.example.flashcardapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashSet

class ReviewFlashcardsViewModel(private val repository: FlashcardRepository) : ViewModel() {

    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> get() = _currentFlashcard

    private val _lastInterval = MutableLiveData<Long>()
    val lastInterval: LiveData<Long> get() = _lastInterval

    private val seenFlashcards = HashSet<Int>()

    private val _questionsMovedCount = MutableLiveData<Int>(0)
    val questionsMovedCount: LiveData<Int> get() = _questionsMovedCount

    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> get() = _score

    private val _pastCount = MutableLiveData<Int>(0)
    val pastCount: LiveData<Int> get() = _pastCount

    private val _futureCount = MutableLiveData<Int>(0)
    val futureCount: LiveData<Int> get() = _futureCount

    private val _totalFlashcardsForSelectedTopics = MutableLiveData<Int>(0)
    val totalFlashcardsForSelectedTopics: LiveData<Int> get() = _totalFlashcardsForSelectedTopics

    private var answerStartTime: Long = 0

    fun fetchNextFlashcard() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextCard = repository.getNextDueFlashcardForSelectedTopics(System.currentTimeMillis())
            val totalCount = repository.getTotalFlashcardsForSelectedTopics()
            withContext(Dispatchers.Main) {
                _totalFlashcardsForSelectedTopics.value = totalCount
                if (nextCard != null) {
                    _currentFlashcard.value = nextCard
                    seenFlashcards.add(nextCard.id)
                } else {
                    _currentFlashcard.value = null
                }
                updateCounts()
            }
        }
    }

    fun onShowAnswer() {
        answerStartTime = System.nanoTime()
    }

    fun handleConfidence(quality: Int) {
        val currentFc = _currentFlashcard.value ?: return

        val answerDurationMillis = (System.nanoTime() - answerStartTime) / 1_000_000L

        val currentTime = System.currentTimeMillis()
        val lastReviewTime = currentFc.nextReview - (currentFc.interval * 1000L)
        var interval = currentFc.interval
        var repetition = currentFc.repetition

        when (quality) {
            0 -> { interval = 1; repetition = 0 }
            1 -> { interval = 10; repetition = 0 }
            2 -> { interval = 20; repetition = 0 }
            3 -> { interval = 30; repetition++ }
            4 -> { interval = 1600; repetition++ }
            5 -> {
                val timeLapsed = (currentTime - lastReviewTime) / 1000
                interval = (timeLapsed * 2 + 3600).toInt()
                repetition++
            }
            else -> { interval = 30; repetition = 0 }
        }

        _lastInterval.postValue(interval * 1000L)

        val nextReview = currentTime + interval * 1000L
        currentFc.interval = interval
        currentFc.nextReview = nextReview
        currentFc.repetition = repetition

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFlashcard(currentFc)
            val timePushed = currentFc.nextReview - System.currentTimeMillis()
            if (timePushed > 24 * 60 * 60 * 1000L) {
                val oldValue = _questionsMovedCount.value ?: 0
                _questionsMovedCount.postValue(oldValue + 1)
            }
            val oldScore = _score.value ?: 0
            _score.postValue(oldScore + quality)
            fetchNextFlashcard()
        }
    }

    private fun updateCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val counts = repository.getPastAndFutureQuestionsCountForSelectedTopics(System.currentTimeMillis())
            withContext(Dispatchers.Main) {
                _pastCount.value = counts[0]
                _futureCount.value = counts[1]
            }
        }
    }

    fun getSeenCount(): Int {
        return seenFlashcards.size
    }
}