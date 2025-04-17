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

/*
This class represents a RecyclerAdapterBooks object.
A RecyclerAdapterBooks handles the recycler view layouts specific to Book objects.
It has a list of books, a context, a listener, and an expanded position variable.
 */
public class RecyclerAdapterBooks extends RecyclerView.Adapter<RecyclerAdapterBooks.ViewHolder> {

    /*
    This interface defines methods for onNoteClick and onNoteLongClick.
    This class will assign the implementations of these methods to the cards it generate.
     */
    public interface OnNoteListener {
        void onNoteClick(Book book);
        void onNoteLongClick(Book book);
    }

    private List<Book> books;
    private BookCardviewBinding binding;
    private int expandedPosition = -1;
    private OnNoteListener mListener;
    private Context context;

    /*
    required no-argument constructor
     */
    public RecyclerAdapterBooks() {
        super();
        books = new ArrayList<Book>();
    }

    /*
    Constructor
     */
    public RecyclerAdapterBooks(Context context, List<Book> bookList) {
        this.books = bookList;
        this.context = context;
    }

    /*
    Static class ViewHolder
    Keeps track of the UI elements for the RecyclerAdapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView cover;
        TextView title;
        TextView author;
        TextView title2;
        TextView author2;
        TextView pubYear;
        TextView genre;
        TextView age;
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
            categories = binding.textBookThemes;

            expContentLayout = binding.expandedLayout;
            collapsedLayout = binding.collapsedLayout;

            // sets an onLongClickListener for the recycler view
            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    // get the position of the item cicked
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // tell the listener which book was clicked on and have it do whatever it does
                        onNoteListener.onNoteLongClick(books.get(position));
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
                        // tell the listener which book was clicked on and have it do whatever it does
                        listener.onNoteClick(books.get(position));
                    }
                }
            });

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

        // safety check
        if (position == RecyclerView.NO_POSITION || position >= books.size()) {
            return;
        }

        Book book = books.get(position);

        // set all cards to collapsed rather than expanded
        holder.expContentLayout.setVisibility(View.GONE);
        holder.collapsedLayout.setVisibility(View.VISIBLE);

        // set data in cards
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(holder.cover);
        }
        else {
            holder.cover.setImageResource(book.getCover());
        }
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        // set data in expanded cards
        holder.title2.setText(book.getTitle());
        holder.author2.setText(book.getAuthor());
        holder.pubYear.setText("Published: " + book.getPubYear());
        holder.genre.setText("Genre: " + (book.getGenre() != null ? book.getGenre() : "Unknown"));
        holder.age.setText("Age: " + (book.getAgeRange() != null ? book.getAgeRange() : "Not specified"));
        if (book.getCategories() != null) {
            holder.categories.setText(book.getCategories().toString());
        }

        // Handle expanded state separately
        boolean isExpanded = position == expandedPosition;
        holder.expContentLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.collapsedLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

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
