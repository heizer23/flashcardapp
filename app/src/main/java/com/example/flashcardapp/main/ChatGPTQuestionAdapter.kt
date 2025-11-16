package com.example.flashcardapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard

class ChatGPTQuestionAdapter(
    private val flashcards: MutableList<Flashcard>?,
    private val recreateClickListener: OnRecreateClickListener?
) : RecyclerView.Adapter<ChatGPTQuestionAdapter.QuestionViewHolder>() {

    private val selectionStates: MutableList<Boolean> = mutableListOf()

    init {
        if (flashcards != null) {
            repeat(flashcards.size) {
                selectionStates.add(false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_generated_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val flashcard = flashcards?.get(position) ?: return
        holder.etQuestion.setText(flashcard.question)
        holder.etAnswer.setText(flashcard.answer)

        // Avoid triggering the listener during binding
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = selectionStates[position]
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            selectionStates[position] = isChecked
        }

        holder.itemView.setOnClickListener {
            recreateClickListener?.onRecreateClick(flashcard, position)
        }
    }

    override fun getItemCount(): Int {
        return flashcards?.size ?: 0
    }

    fun getSelectedQuestions(): List<Flashcard> {
        val selected = mutableListOf<Flashcard>()
        flashcards?.forEachIndexed { index, flashcard ->
            if (selectionStates[index]) {
                selected.add(flashcard)
            }
        }
        return selected
    }

    fun updateData(newFlashcards: List<Flashcard>) {
        flashcards?.clear()
        selectionStates.clear()
        flashcards?.addAll(newFlashcards)
        repeat(newFlashcards.size) {
            selectionStates.add(false)
        }
        notifyDataSetChanged()
    }

    interface OnRecreateClickListener {
        fun onRecreateClick(flashcard: Flashcard, position: Int)
    }

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etQuestion: EditText = itemView.findViewById(R.id.et_generated_question)
        val etAnswer: EditText = itemView.findViewById(R.id.et_generated_answer)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cb_select_question)
    }
}
