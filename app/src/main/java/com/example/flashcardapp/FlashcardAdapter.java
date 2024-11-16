// File: FlashcardAdapter.java
package com.example.flashcardapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.flashcardapp.TimeUtils.formatTimeDifference;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {

    private List<Flashcard> flashcards;
    private Context context;

    public FlashcardAdapter(Context context, List<Flashcard> flashcards) {
        this.context = context;
        // Sort flashcards by nextReview, with overdue ones first
        Collections.sort(flashcards, new Comparator<Flashcard>() {
            @Override
            public int compare(Flashcard f1, Flashcard f2) {
                return Long.compare(f1.getNextReview(), f2.getNextReview());
            }
        });
        this.flashcards = flashcards;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvQuestion, tvAnswer, tvTimeDifference, tvInterval;

        public ViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            tvTimeDifference = itemView.findViewById(R.id.tv_time_difference);
            tvInterval = itemView.findViewById(R.id.tv_interval);

            // Handle row click to open the edit/delete view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Flashcard flashcard = flashcards.get(position);
                Intent intent = new Intent(context, EditFlashcardActivity.class);
                intent.putExtra("FLASHCARD_ID", flashcard.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public FlashcardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashcardAdapter.ViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.tvQuestion.setText(flashcard.getQuestion());
        holder.tvAnswer.setText(flashcard.getAnswer());

        // Calculate time difference in seconds, minutes, and days
        long currentTime = System.currentTimeMillis();
        long nextReviewTime = flashcard.getNextReview();
        long timeDifferenceMillis = nextReviewTime - currentTime;

        long absTimeDifferenceMillis = Math.abs(timeDifferenceMillis);
        String timeDifferenceText = formatTimeDifference(absTimeDifferenceMillis);

        // Add a negative sign if the review is overdue
        if (timeDifferenceMillis < 0) {
            timeDifferenceText = "-" + timeDifferenceText;
        }

        long intervalValue = Math.abs(flashcard.getInterval()) * 1000L;  // Convert seconds to milliseconds

        // Set the time difference and interval values
        holder.tvTimeDifference.setText(timeDifferenceText);
        holder.tvInterval.setText(formatTimeDifference(intervalValue));
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }
}
