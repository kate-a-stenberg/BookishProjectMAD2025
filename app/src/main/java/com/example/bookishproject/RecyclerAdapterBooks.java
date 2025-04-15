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
import com.example.bookishproject.databinding.BookCardviewBinding;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapterBooks extends RecyclerView.Adapter<RecyclerAdapterBooks.ViewHolder> {

    public interface OnNoteListener {
        void onNoteClick(Book book);
        void onNoteLongClick(Book book);
    }

    private List<Book> books;
    private BookCardviewBinding binding;
    private int expandedPosition = -1;
    private OnNoteListener mListener;
    private Context context;

    public RecyclerAdapterBooks() {
        super();
        books = new ArrayList<Book>();
    }

    public RecyclerAdapterBooks(ArrayList<Book> books, OnNoteListener onNoteListener) {
        this.books = books;
        this.mListener = onNoteListener;
    }

    public RecyclerAdapterBooks(Context context, List<Book> bookList) {
        this.books = bookList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView cover;
        TextView title;
        TextView author;
        TextView title2;
        TextView author2;
        TextView pubYear;
        TextView genre;
        TextView age;
        TextView synopsis;
        TextView categories;
        private final List<Book> books;
        OnNoteListener onNoteListener;
        LinearLayout expContentLayout;
        LinearLayout collapsedLayout;

        public ViewHolder(@NonNull BookCardviewBinding binding, final OnNoteListener listener, List<Book> books) {
            super(binding.getRoot());
            this.books = books;
            this.onNoteListener = listener;

            cover = binding.coverImage;

            title = binding.textTitle;
            author = binding.textAuthor;

            title2 = binding.textBookTitle2;
            author2 = binding.textBookAuthor2;
            pubYear = binding.textBookPubYear;
            genre = binding.textBookGenre;
            age = binding.textBookAge;
            synopsis = binding.textBookSynopsis;
            categories = binding.textBookThemes;

            expContentLayout = binding.expandedLayout;
            collapsedLayout = binding.collapsedLayout;

            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onNoteListener.onNoteLongClick(books.get(position));
                        return true; // Consume the long click
                    }
                }
                return false;
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onNoteClick(books.get(position));
                    }
                }
            });

        }

        @Override
        public void onClick(View v) {
            this.onNoteListener.onNoteClick(books.get(getAdapterPosition()));
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = BookCardviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, mListener, books);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {
        int position = holder.getAdapterPosition();

        if (position == RecyclerView.NO_POSITION || position >= books.size()) {
            // Safety check to avoid crashes
            return;
        }

        Book book = books.get(position);

        // Reset visibility states - this is crucial
        holder.expContentLayout.setVisibility(View.GONE);
        holder.collapsedLayout.setVisibility(View.VISIBLE);

        // Set your data
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.title2.setText(book.getTitle());
        holder.author2.setText(book.getAuthor());
        holder.pubYear.setText("Published: " + book.getPubYear());
        holder.genre.setText("Genre: " + (book.getGenre() != null ? book.getGenre() : "Unknown"));
        holder.age.setText("Age: " + (book.getAgeRange() != null ? book.getAgeRange() : "Not specified"));
        holder.synopsis.setText(book.getSynopsis() != null ? book.getSynopsis() : "No description available");

        if (book.getCategories() != null) {
            holder.synopsis.setText(book.getCategories().toString());
        }


        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(holder.cover);
        }
        else {
            holder.cover.setImageResource(book.getCover());
        }

        // Handle expanded state separately
        boolean isExpanded = position == expandedPosition;
        holder.expContentLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.collapsedLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

//        // Set click listener
//        holder.itemView.setOnClickListener(view -> {
//            // Your existing click handling
//        });
    }

    public void setOnNoteListener(OnNoteListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
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
