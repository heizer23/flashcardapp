package com.example.flashcardapp.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import java.util.Collections

class FlashcardAdapter(
    private val context: Context,
    private val flashcards: MutableList<Flashcard>
) : RecyclerView.Adapter<FlashcardAdapter.ViewHolder>() {

    init {
        // Sort flashcards by nextReview ascending, putting overdue first.
        Collections.sort(flashcards) { f1, f2 ->
            f1.nextReview.compareTo(f2.nextReview)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestion: TextView = itemView.findViewById(R.id.tv_question)
        val tvAnswer: TextView = itemView.findViewById(R.id.tv_answer)
        val tvTimeDifference: TextView = itemView.findViewById(R.id.tv_time_difference)
        val tvInterval: TextView = itemView.findViewById(R.id.tv_interval)

        init {
            // Handle row click to open edit
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val flashcard = flashcards[position]
                    val intent = Intent(context, EditFlashcardActivity::class.java)
                    intent.putExtra("FLASHCARD_ID", flashcard.id)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flashcard = flashcards[position]
        holder.tvQuestion.text = flashcard.question
        holder.tvAnswer.text = flashcard.answer

        val currentTime = System.currentTimeMillis()
        val nextReviewTime = flashcard.nextReview
        val timeDifferenceMillis = nextReviewTime - currentTime
        val absTimeDifferenceMillis = kotlin.math.abs(timeDifferenceMillis)
        var timeDifferenceText = TimeUtils.formatTimeDifference(absTimeDifferenceMillis)

        if (timeDifferenceMillis < 0) {
            timeDifferenceText = "-$timeDifferenceText"
        }

        val intervalValue = kotlin.math.abs(flashcard.interval) * 1000L
        holder.tvTimeDifference.text = timeDifferenceText
        holder.tvInterval.text = TimeUtils.formatTimeDifference(intervalValue)
    }

    override fun getItemCount(): Int {
        return flashcards.size
    }
}
