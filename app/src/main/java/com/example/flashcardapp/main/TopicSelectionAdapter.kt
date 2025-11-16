package com.example.flashcardapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.TopicWithCount

// changes: now we store TopicWithCount
class TopicSelectionAdapter(private val topics: List<TopicWithCount>) :
    RecyclerView.Adapter<TopicSelectionAdapter.TopicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_selection, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topicWC = topics[position]
        // show count next to the name
        val label = "${topicWC.name} (${topicWC.cardCount})"

        holder.cbTopic.text = label
        holder.cbTopic.isChecked = topicWC.selected
        holder.cbTopic.setOnCheckedChangeListener { _, isChecked ->
            topicWC.selected = isChecked
        }
    }

    override fun getItemCount(): Int {
        return topics.size
    }

    // Return the list (for saving) after user toggles or individually checks/unchecks.
    fun getSelectedTopics(): List<TopicWithCount> {
        return topics
    }

    // NEW: Check if everything is selected
    fun areAllSelected(): Boolean {
        return topics.all { it.selected }
    }

    // NEW: Select all
    fun selectAll() {
        for (t in topics) {
            t.selected = true
        }
        notifyDataSetChanged()
    }

    // NEW: Deselect all
    fun deselectAll() {
        for (t in topics) {
            t.selected = false
        }
        notifyDataSetChanged()
    }

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbTopic: CheckBox = itemView.findViewById(R.id.cb_topic)
    }
}
