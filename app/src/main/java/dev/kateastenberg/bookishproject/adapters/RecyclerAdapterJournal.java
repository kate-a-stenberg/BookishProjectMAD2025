package dev.kateastenberg.bookishproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.EntryLineBinding;

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
        void onNoteClick(int position);
        void onNoteLongClick(int position);
    }

    final List<Entry> entries;
    private OnNoteListener mListener;
    private int expandedPosition = -1;

    /*
    Constructor
     */
    public RecyclerAdapterJournal(List<Entry> entries) {
        this.entries = entries;
    }

    /*
    Static class ViewHolder
    Keeps track of the UI elements for the RecyclerAdapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView coverImage;
        TextView title, date, activity, details;
        OnNoteListener onNoteListener;
        LinearLayout expContent;
        View divider;

        public ViewHolder(@NonNull EntryLineBinding binding,
                          final OnNoteListener listener) {
            super(binding.getRoot());
            this.onNoteListener = listener;

            coverImage = binding.coverImage;

            title = binding.textTitle;
            date = binding.textDate;
            activity = binding.textActivity;
            details = binding.textDetails;

            expContent = binding.expContent;
            divider = binding.divider;


            // sets an onLongClickListener for the recycler view
            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    // get the position of the item clicked
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // tell the listener which entry was clicked on and have it do whatever it does
                        onNoteListener.onNoteLongClick(position);
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
                        listener.onNoteClick(position);
                    }
                }
            });
        }

        public ImageView getCover() {
            return this.coverImage;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EntryLineBinding binding = EntryLineBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(binding, this.mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {

        int position = holder.getAdapterPosition();

        // safety check
        if (position == RecyclerView.NO_POSITION || position >= entries.size()) {
            return;
        }

        Entry entry = entries.get(position);

        // set all cards to collapsed rather than expanded
        holder.divider.setVisibility(View.GONE);
        holder.expContent.setVisibility(View.GONE);

        // SET DATA

        holder.coverImage
                .setImageResource((entries.get(position)).getUserBook().getBook().getCover());
        holder.title.setText((entries.get(position)).getUserBook().getBook().getTitle());
        holder.date.setText(entries.get(position).getDate().displayDate());
        holder.activity.setText(entries.get(position).getDescription());

        // SET DATA IN EXPANDED VIEWS

        if (entry.getComments() != null && !entry.getComments().isEmpty()) {
            holder.details.setText((entries.get(position)).getComments());
        }
        else {
            holder.details.setVisibility(View.GONE);
        }

        if (entry.getUserBook().getBook().getCoverUrl() != null
                && !entry.getUserBook().getBook().getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(entry.getUserBook().getBook().getCoverUrl())
                    .placeholder(R.drawable.book_cover_background)
                    .error(R.drawable.book_cover_background)
                    .into(holder.coverImage);
        }
        else {
            holder.coverImage.setImageResource(entry.getUserBook().getBook().getCover());
        }

        if (entry.getComments() != null && !entry.getComments().isEmpty()) {
            // handle expanded state separately
            boolean isExpanded = position == expandedPosition;
            holder.expContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

    }

    public void setOnNoteListener(OnNoteListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    /*
    Method to switch back and forth between expanded and collapsed card views
     */
    public void toggleExpansion(int position) {

        int oldExpandedPosition = expandedPosition;

        // If this position is already expanded, collapse it
        // or: if the position clicked on was already expanded
        if (expandedPosition == position) {
            // set the expanded position to -1
            // or no position. not expanded
            expandedPosition = -1;
        } else {
            // Otherwise, collapse any expanded position and expand this one
            expandedPosition = position;
        }

        // Notify the adapter to update the previously expanded item (if any)
        if (oldExpandedPosition != -1 && oldExpandedPosition != position) {
            notifyItemChanged(oldExpandedPosition);
        }

        // Notify adapter to update this position
        notifyItemChanged(position);
    }

}
