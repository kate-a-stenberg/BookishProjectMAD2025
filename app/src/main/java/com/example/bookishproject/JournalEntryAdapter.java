package com.example.bookishproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class JournalEntryAdapter {

    private int image;
    private Entry activity;

    public JournalEntryAdapter(int image, Entry activity) {
        super();
        this.image = image;
        this.activity = activity;
    }

    static class ViewHolder {

        TextView date;
        TextView title;
        TextView author;
        TextView activityType;
        TextView description;
        ImageView cover;

        public ViewHolder(@NonNull View itemView) {
            super();
//            date = itemView.findViewById(R.id.textJournalEntryDate);
//            title = itemView.findViewById(R.id.textJournalEntryTitle);
//            author = itemView.findViewById(R.id.textJournalEntryAuthor);
//            activityType = itemView.findViewById(R.id.textJournalEntryActivity);
//            description = itemView.findViewById(R.id.textJournalEntryDescription);
//            cover = itemView.findViewById(R.id.imageJournalEntryCover);
        }
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_journal_entry, parent, false);
        return new ViewHolder(v);

    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //holder.cardImage.setImageResource(images.get(position));
//        holder.date.setText(activity.getDate().displayDate());
//        holder.title.setText(activity.getBook().getTitle());
//        holder.author.setText(activity.getBook().getAuthor());
//        holder.activityType.setText(activity.displayActivityType());
//        holder.description.setText(activity.getDescription());
//        holder.cover.setImageResource(activity.getBook().getCover());

    }

}
