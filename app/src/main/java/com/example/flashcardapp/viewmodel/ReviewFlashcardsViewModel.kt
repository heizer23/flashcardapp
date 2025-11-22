package com.example.flashcardapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.ReviewHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class ReviewFlashcardsViewModel(private val repository: FlashcardRepository) : ViewModel() {

    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> get() = _currentFlashcard

    private val _lastInterval = MutableLiveData<Long>()
    val lastInterval: LiveData<Long> get() = _lastInterval

    private val _lastReviewedTimestamp = MutableLiveData<Long?>()
    val lastReviewedTimestamp: LiveData<Long?> get() = _lastReviewedTimestamp

    private val _reviewedCount = MutableLiveData<Int>()
    val reviewedCount: LiveData<Int> get() = _reviewedCount

    private val _todaysReviewedCount = MutableLiveData<Int>(0)
    val todaysReviewedCount: LiveData<Int> get() = _todaysReviewedCount

    private val _dueFlashcardsForLevel = MutableLiveData<Int>(0)
    val dueFlashcardsForLevel: LiveData<Int> get() = _dueFlashcardsForLevel

    private val _currentLevel = MutableLiveData<Int>(0)
    val currentLevel: LiveData<Int> get() = _currentLevel

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
            val todaysCount = repository.getTodaysReviewedFlashcardCount()
            val lastReviewed = nextCard?.let { repository.getLastReviewedTimestamp(it.id) }
            val reviewedCount = nextCard?.let { repository.getReviewedCount(it.id) }
            val dueForLevel = nextCard?.let { repository.getDueFlashcardsCountForLevel(System.currentTimeMillis(), it.level) } ?: 0
            withContext(Dispatchers.Main) {
                _totalFlashcardsForSelectedTopics.value = totalCount
                _todaysReviewedCount.value = todaysCount
                _lastReviewedTimestamp.value = lastReviewed
                _reviewedCount.value = reviewedCount ?: 0
                _dueFlashcardsForLevel.value = dueForLevel
                if (nextCard != null) {
                    _currentFlashcard.value = nextCard
                    _currentLevel.value = nextCard.level
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
        val timeSinceLastReview = currentTime - lastReviewTime
        var interval: Int
        var repetition = currentFc.repetition

        when (quality) {
            0 -> { // Forgot
                interval = 1
                repetition = 0
                currentFc.lastAnswer = false
            }
            1 -> { // Okay
                interval = max(5, ((timeSinceLastReview / 1000) * 2).toInt())
                repetition++
                currentFc.lastAnswer = true
            }
            2 -> { // Good
                interval = max(20 * 60, ((timeSinceLastReview / 1000) * 2).toInt())
                repetition++
                currentFc.lastAnswer = true
            }
            3 -> { // Perfect
                interval = max(24 * 60 * 60, ((timeSinceLastReview / 1000) * 3).toInt())
                repetition++
                currentFc.lastAnswer = true
            }
            else -> { // Should not happen
                interval = currentFc.interval
            }
        }

        _lastInterval.postValue(interval * 1000L)

        val nextReview = currentTime + interval * 1000L
        currentFc.interval = interval
        currentFc.nextReview = nextReview
        currentFc.repetition = repetition

        val reviewHistory = ReviewHistory(
            question_id = currentFc.id,
            confidence_level = quality,
            timestamp = currentTime,
            time_since_last_seen = timeSinceLastReview,
            interval = interval,
            review_type = "review",
            answer_duration = answerDurationMillis.toInt()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFlashcard(currentFc)
            repository.insertReviewHistory(reviewHistory)
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
}