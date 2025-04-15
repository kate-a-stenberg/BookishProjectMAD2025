package com.example.bookishproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.JournalCardviewBinding;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapterJournal extends RecyclerView.Adapter<RecyclerAdapterJournal.ViewHolder> {

    public interface OnNoteListener {
        void onNoteClick(Entry entry);
        void onNoteLongClick(Entry entry);
    }

    final List<Entry> entries;
    private OnNoteListener mListener;
    private JournalCardviewBinding binding;
    private int expandedPosition = -1;
    private Context context;

    public RecyclerAdapterJournal() {
        super();
        entries = new ArrayList<Entry>();
    }

    public RecyclerAdapterJournal(ArrayList<Entry> entries, OnNoteListener onNoteListener) {
        this.entries = entries;
        this.mListener = onNoteListener;
    }

    public RecyclerAdapterJournal(Context context, List<Entry> entries) {
        this.entries = entries;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView cardImage;
        TextView title;
        TextView date;
        TextView activity;
        TextView title2;
        TextView date2;
        TextView activity2;
        TextView author;
        TextView details;
        private final List<Entry> entries;
        OnNoteListener onNoteListener;
        LinearLayout expContentLayout;
        LinearLayout collapsedLayout;

        public ViewHolder(@NonNull JournalCardviewBinding binding, final OnNoteListener listener, List<Entry> entries) {
            super(binding.getRoot());
            this.entries = entries;
            this.onNoteListener = listener;

            cardImage = binding.coverImage;

            title = binding.textJournalTitle;
            date = binding.textJournalDate;
            activity = binding.textJournalActivity;

            title2 = binding.textJournalTitle2;
            date2 = binding.textJournalDate2;
            activity2 = binding.textJournalActivity2;
            author = binding.textJournalAuthor;
            details = binding.textJournalDetails;

            expContentLayout = binding.expandedContentLayout;
            collapsedLayout = binding.collapsedContentLayout;

            itemView.setOnClickListener(this);
            this.onNoteListener = onNoteListener;

            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onNoteListener.onNoteLongClick(entries.get(position));
                        return true; // Consume the long click
                    }
                }
                return false;
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onNoteClick(entries.get(position));
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            this.onNoteListener.onNoteClick(entries.get(getAdapterPosition()));
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = JournalCardviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, this.mListener, entries);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {

        int position = holder.getAdapterPosition();

        if (position == RecyclerView.NO_POSITION || position >= entries.size()) {
            // Safety check to avoid crashes
            return;
        }

        Entry entry = entries.get(position);

        holder.expContentLayout.setVisibility(View.GONE);
        holder.collapsedLayout.setVisibility(View.VISIBLE);

        // Set Data
        //holder.cardImage.setImageResource(images.get(position));
        holder.cardImage.setImageResource((entries.get(position)).getBook().getCover());

        holder.title.setText((entries.get(position)).getBook().getTitle());
        holder.date.setText(entries.get(position).getDate().displayDate());
        holder.activity.setText(entries.get(position).getDescription());

        holder.title2.setText((entries.get(position)).getBook().getTitle());
        holder.date2.setText((entries.get(position)).getDate().displayDate());
        holder.activity2.setText((entries.get(position)).getDescription());
        holder.author.setText((entries.get(position)).getBook().getAuthor());
        holder.details.setText((entries.get(position)).getComments());

        if (entry.getBook().getCoverUrl() != null && !entry.getBook().getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(entry.getBook().getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(holder.cardImage);
        }
        else {
            holder.cardImage.setImageResource(entry.getBook().getCover());
        }

        boolean isExpanded = position == expandedPosition;
        holder.expContentLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.collapsedLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);


//        holder.itemView.setOnClickListener(view -> {
//            if (isExpanded) {
//                expandedPosition = -1;
//            }
//            else {
//                int oldExpandedPosition = expandedPosition;
//                expandedPosition = position;
//
//                if (oldExpandedPosition >= 0) {
//                    notifyItemChanged(oldExpandedPosition);
//                }
//            }
//            notifyItemChanged(position);
//            onNoteListener.onNoteClick(entry);
//        });

    }

    public void setOnNoteListener(OnNoteListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public void resetExpandedState() {
        expandedPosition = -1;
        notifyDataSetChanged();
    }

    public void toggleExpansion(int position) {
        // If this position is already expanded, collapse it
        if (expandedPosition == position) {
            expandedPosition = -1;
        } else {
            // Otherwise, collapse any expanded position and expand this one
            int oldExpandedPosition = expandedPosition;
            expandedPosition = position;

            // Notify the adapter to update the previously expanded item (if any)
            if (oldExpandedPosition >= 0) {
                notifyItemChanged(oldExpandedPosition);
            }
        }

        // Notify adapter to update this position
        notifyItemChanged(position);
    }

}
