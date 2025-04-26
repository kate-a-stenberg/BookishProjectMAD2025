package com.example.bookishproject;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        TextView title, author, title2, author2, pubYear, genre, age, categories, series, series2;
        View divider;
        private final List<Book> books;
        OnNoteListener onNoteListener;
        CardView card;
        LinearLayout expContentLayout;
        LinearLayout collapsedLayout;

        public ViewHolder(@NonNull BookCardviewBinding binding, final OnNoteListener listener, List<Book> books) {
            super(binding.getRoot());
            this.books = books;
            this.onNoteListener = listener;

            card = binding.bookCardView;

            cover = binding.coverImage;
            divider = binding.divider;

            title = binding.textTitle;
            author = binding.textAuthor;
            series = binding.textSeries;

            title2 = binding.textTitle2;
            author2 = binding.textAuthor2;
            series2 = binding.textSeries2;
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
            Glide.with(holder.itemView.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(holder.cover);
        }
        else {
            holder.cover.setImageResource(book.getCover());
        }
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        if (book.getSeries() == null || book.getSeries().isEmpty()) {
            holder.series.setVisibility(View.GONE);
        }
        else if (!book.getSeries().equals("Standalone")) {
            holder.series.setText(book.getSeries());
            holder.series.setVisibility(View.VISIBLE);
        }
        if (book.getNumber() != null && book.getNumber() > 0) {
            holder.series.append(" #" + book.getNumber().toString());
        }
        // set data in expanded cards
        holder.title2.setText(book.getTitle());
        holder.author2.setText(book.getAuthor());

        if (book.getSeries() == null || book.getSeries().isEmpty()) {
            holder.series2.setVisibility(View.GONE);
        }
        else if (!book.getSeries().equals("Standalone")) {
            holder.series2.setText(book.getSeries());
            holder.series2.setVisibility(View.VISIBLE);

        }
        if (book.getNumber() != null && book.getNumber() > 0) {
            holder.series2.append(" #" + book.getNumber().toString());
        }

        holder.pubYear.setText("Published: " + book.getPubYear());
        holder.genre.setText("Genre: " + (book.getGenre() != null ? book.getGenre() : "Unknown"));
        holder.age.setText("Age: " + (book.getAgeRange() != null ? book.getAgeRange() : "Not specified"));
        if (book.getCategories() != null) {
            holder.categories.setText("");
            holder.categories.append(String.join(",", book.getCategories()));
        }

        // Handle expanded state separately
        boolean isExpanded = position == expandedPosition;
        holder.expContentLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.collapsedLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        if (context instanceof MainActivity) {
            int section = ((MainActivity) context).getCurrentSection();

            int cardColor, textColor;

            switch (section) {
                case 1: // Books
                    cardColor = context.getResources().getColor(R.color.my_theme_books_background, null);
                    textColor = context.getResources().getColor(R.color.my_theme_books_text, null);
                    break;
                case 2: // Recs
                    cardColor = context.getResources().getColor(R.color.my_theme_recs_background, null);
                    textColor = context.getResources().getColor(R.color.my_theme_recs_text, null);
                    break;
                case 3: // Journal
                    cardColor = context.getResources().getColor(R.color.my_theme_journal_background, null);
                    textColor = context.getResources().getColor(R.color.my_theme_journal_text, null);
                    break;
                default:
                    cardColor = context.getResources().getColor(R.color.my_theme_main_super_light, null);
                    textColor = context.getResources().getColor(R.color.my_theme_main_super_dark, null);
            }

            holder.card.setCardBackgroundColor(cardColor);
        }

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
