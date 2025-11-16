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

    // For each flashcard the user is reviewing
    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> get() = _currentFlashcard

    // Counters to track how many flashcards have been seen
    private val seenFlashcards = HashSet<Int>()

    // Track how many questions moved beyond a 24-hour window
    private val _questionsMovedCount = MutableLiveData<Int>(0)
    val questionsMovedCount: LiveData<Int> get() = _questionsMovedCount

    // Score accumulation
    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> get() = _score

    // Observables for counts of past/future, used to display in UI
    private val _pastCount = MutableLiveData<Int>(0)
    val pastCount: LiveData<Int> get() = _pastCount

    private val _futureCount = MutableLiveData<Int>(0)
    val futureCount: LiveData<Int> get() = _futureCount

    // We'll track how long user had the question hidden
    private var answerStartTime: Long = 0

    // Called by the Activity so we can show the first flashcard
    fun fetchNextFlashcard() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextCard = repository.getNextDueFlashcard(System.currentTimeMillis())
            withContext(Dispatchers.Main) {
                if (nextCard != null) {
                    _currentFlashcard.value = nextCard
                    seenFlashcards.add(nextCard.id)
                } else {
                    // If there's no card due, set null to indicate we're done
                    _currentFlashcard.value = null
                }
                updateCounts()
            }
        }
    }

    // Called when the user taps \"Show Answer\"
    fun onShowAnswer() {
        answerStartTime = System.nanoTime()
    }

    // Called when user selects one of the 6 confidence levels
    fun handleConfidence(quality: Int) {
        val currentFc = _currentFlashcard.value ?: return

        val answerDurationMillis = (System.nanoTime() - answerStartTime) / 1_000_000L
        // If we wanted, we could store 'answerDurationMillis' somewhere or log it.

        val currentTime = System.currentTimeMillis()
        val lastReviewTime = currentFc.nextReview - (currentFc.interval * 1000L)
        var interval = currentFc.interval
        var repetition = currentFc.repetition

        when (quality) {
            0 -> {
                interval = 1
                repetition = 0
            }
            1 -> {
                interval = 10
                repetition = 0
            }
            2 -> {
                interval = 20
                repetition = 0
            }
            3 -> {
                interval = 30
                repetition++
            }
            4 -> {
                interval = 1600
                repetition++
            }
            5 -> {
                val timeLapsed = (currentTime - lastReviewTime) / 1000
                interval = (timeLapsed * 2 + 3600).toInt()
                repetition++
            }
            else -> {
                interval = 30
                repetition = 0
            }
        }

        val nextReview = currentTime + interval * 1000L
        currentFc.interval = interval
        currentFc.nextReview = nextReview
        currentFc.repetition = repetition

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFlashcard(currentFc)
            val timePushed = currentFc.nextReview - System.currentTimeMillis()
            if (timePushed > 24 * 60 * 60 * 1000L) {
                // increment questionsMovedCount if it's beyond 24 hours
                val oldValue = _questionsMovedCount.value ?: 0
                _questionsMovedCount.postValue(oldValue + 1)
            }
            // add to score
            val oldScore = _score.value ?: 0
            _score.postValue(oldScore + quality)

            // after updating current card, fetch the next one
            fetchNextFlashcard()
        }
    }

    // Update the counts for how many are in the past/future
    private fun updateCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val counts = repository.getPastAndFutureQuestionsCount(System.currentTimeMillis())
            withContext(Dispatchers.Main) {
                _pastCount.value = counts[0]
                _futureCount.value = counts[1]
            }
        }
    }

    // For UI display: how many we have \"seen\" so far
    fun getSeenCount(): Int {
        return seenFlashcards.size
    }
}