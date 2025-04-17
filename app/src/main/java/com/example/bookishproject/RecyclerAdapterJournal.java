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

/*
This class represents a RecyclerAdapterJournal object.
A RecyclerAdapterJournal handles the recycler view layouts specific to the Journal fragment.
It has a list of entries, a context, a listener, and an expanded position variable.
 */
public class RecyclerAdapterJournal extends RecyclerView.Adapter<RecyclerAdapterJournal.ViewHolder> {

    /*
    This interface defines methods for onNoteClick and onNoteLongClick.
    This class will assign the implementations of these methods to the cards it generate.
    */
    public interface OnNoteListener {
        void onNoteClick(Entry entry);
        void onNoteLongClick(Entry entry);
    }

    final List<Entry> entries;
    private OnNoteListener mListener;
    private JournalCardviewBinding binding;
    private int expandedPosition = -1;
    private Context context;

    /*
    Required no-argument constructor
     */
    public RecyclerAdapterJournal() {
        super();
        entries = new ArrayList<Entry>();
    }

    /*
    Constuctor
     */
    public RecyclerAdapterJournal(Context context, List<Entry> entries) {
        this.entries = entries;
        this.context = context;
    }

    /*
    Static class ViewHolder
    Keeps track of the UI elements for the RecyclerAdapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

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

//            itemView.setOnClickListener(this);

            // sets an onLongClickListener for the recycler view
            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    // get the position of the item cicked
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // tell the listener which entry was clicked on and have it do whatever it does
                        onNoteListener.onNoteLongClick(entries.get(position));
                        return true; // Consume the long click
                    }
                }
                return false;
            });

            // sets an onClickListener for the recycler view
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    // get the position of the item clicked
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // tell the listener which entry was clicked on and have it do whatever it does
                        listener.onNoteClick(entries.get(position));
                    }
                }
            });
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

        // safety check
        if (position == RecyclerView.NO_POSITION || position >= entries.size()) {
            return;
        }

        Entry entry = entries.get(position);

        // set all cards to collapsed reather than expanded
        holder.expContentLayout.setVisibility(View.GONE);
        holder.collapsedLayout.setVisibility(View.VISIBLE);

        // set data in cards
        holder.cardImage.setImageResource((entries.get(position)).getBook().getCover());
        holder.title.setText((entries.get(position)).getBook().getTitle());
        holder.date.setText(entries.get(position).getDate().displayDate());
        holder.activity.setText(entries.get(position).getDescription());
        // set data in expanded card views
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

        // handle expanded state separately
        boolean isExpanded = position == expandedPosition;
        holder.expContentLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.collapsedLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

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

    /*
    Method to switch back and forth between expanded and collapsed card views
     */
    public void toggleExpansion(int position) {
        // If this position is already expanded, collapse it
        // or: if the position clicked on was already expanded
        if (expandedPosition == position) {
            // set the expanded position to -1
            // or no position. no expanded
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
