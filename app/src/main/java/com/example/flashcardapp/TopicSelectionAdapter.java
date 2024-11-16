// File: TopicSelectionAdapter.java
package com.example.flashcardapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TopicSelectionAdapter extends RecyclerView.Adapter<TopicSelectionAdapter.TopicViewHolder> {

    private final List<Topic> topics;

    public TopicSelectionAdapter(List<Topic> topics) {
        this.topics = topics;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_selection, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.cbTopic.setText(topic.getName());
        holder.cbTopic.setChecked(topic.isSelected());

        // Update selection state on checkbox change
        holder.cbTopic.setOnCheckedChangeListener((buttonView, isChecked) -> topic.setSelected(isChecked));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public List<Topic> getSelectedTopics() {
        return topics;
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTopic;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTopic = itemView.findViewById(R.id.cb_topic);
        }
    }
}
