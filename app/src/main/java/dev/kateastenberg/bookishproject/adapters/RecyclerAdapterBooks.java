package dev.kateastenberg.bookishproject.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.BookCardviewBackBinding;
import dev.kateastenberg.bookishproject.databinding.BookCardviewBinding;
import dev.kateastenberg.bookishproject.databinding.BookCardviewFrontBinding;

import java.util.ArrayList;
import java.util.List;

/*
This class represents a RecyclerAdapterBooks object.
A RecyclerAdapterBooks handles the recycler view layouts specific to Book objects.
It has a list of books, a context, a listener, and an expanded position variable.
 */
public class RecyclerAdapterBooks extends RecyclerView.Adapter<RecyclerAdapterBooks.ViewHolder> {

    /*
    This interface defines methods for onNoteLongClick.
    This class will assign the implementations of these methods to the cards it generate.
     */
    public interface OnNoteListener {
        void onNoteLongClick(int position);
        void onNoteClick(int position);
    }

    private final List<Book> books;
    private int flippedPosition = -1;
    private OnNoteListener mListener;

    /*
    required no-argument constructor
     */
    public RecyclerAdapterBooks() {
        super();
        books = new ArrayList<>();
    }

    /*
    Constructor
     */
    public RecyclerAdapterBooks(List<Book> bookList) {
        this.books = bookList;
    }

    /*
    Static class ViewHolder
    Keeps track of the UI elements for the RecyclerAdapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView coverFront, coverBack;
        CardView cardFront, cardBack;
        LinearLayout cardInfo;
        TextView title, author, series;
        OnNoteListener onNoteListener;

        public ViewHolder(@NonNull BookCardviewBinding binding, BookCardviewFrontBinding bindingFront, BookCardviewBackBinding bindingBack, final OnNoteListener listener) {
            super(binding.getRoot());
            this.onNoteListener = listener;

            cardFront = bindingFront.bookCardViewFront;
            cardBack = bindingBack.bookCardViewBack;

            coverFront = bindingFront.coverImageFront;
            coverBack = bindingBack.coverImageBack;

            title = bindingBack.textTitle;
            author = bindingBack.textAuthor;
            series = bindingBack.textSeries;

            cardInfo = bindingBack.bookCardBack;

            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.GONE);

            coverFront.setTransitionName("front_cover_transition_" + getAdapterPosition());
            coverBack.setTransitionName("back_cover_transition_" + getAdapterPosition());

            binding.getRoot().setOnLongClickListener(v -> {
                if (onNoteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onNoteListener.onNoteLongClick(position);
                        return true;
                    }
                }
                return false;
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onNoteClick(position);
                    }
                }
            });

        }

        private void flipCard(View view) {

            float scale = view.getContext().getResources().getDisplayMetrics().density;
            float cameraDist = 16000 * scale;

            // Set the camera distance
            cardFront.setCameraDistance(cameraDist);
            cardBack.setCameraDistance(cameraDist);

            // Create first half of the animation (front -> hidden)
            ObjectAnimator frontAnim = ObjectAnimator.ofFloat(cardFront, "rotationY", 0f, 90f);
            frontAnim.setDuration(300);

            // Create second half of the animation (hidden -> back)
            ObjectAnimator backAnim = ObjectAnimator.ofFloat(cardBack, "rotationY", -90f, 0f);
            backAnim.setDuration(300);

            // Set listeners to handle visibility changes
            frontAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    cardFront.setVisibility(View.GONE);
                    cardBack.setVisibility(View.VISIBLE);
                    backAnim.start();

                }
            });

            backAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                }
            });

            // Start the animation
            frontAnim.start();
        }

        private void flipCardBack(View view) {
            float scale = view.getContext().getResources().getDisplayMetrics().density;
            float cameraDist = 16000 * scale;

            // Set the camera distance
            cardFront.setCameraDistance(cameraDist);
            cardBack.setCameraDistance(cameraDist);

            ObjectAnimator backAnim = ObjectAnimator.ofFloat(cardBack, "rotationY", 0f, 90f);
            backAnim.setDuration(300);

            ObjectAnimator frontAnim = ObjectAnimator.ofFloat(cardFront, "rotationY", -90f, 0f);
            frontAnim.setDuration(300);

            // Add a listener to change visibility between animations
            backAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cardBack.setVisibility(View.GONE);
                    cardFront.setVisibility(View.VISIBLE);
                    cardFront.setRotationY(-90f);

                    // Start the front animation
                    frontAnim.start();
                }
            });

            // Start the first animation
            backAnim.start();
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BookCardviewBinding binding = BookCardviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        BookCardviewFrontBinding bindingFront = BookCardviewFrontBinding.inflate(LayoutInflater.from(parent.getContext()), binding.cardContainer, true);
        BookCardviewBackBinding bindingBack = BookCardviewBackBinding.inflate(LayoutInflater.from(parent.getContext()), binding.cardContainer, true);

        return new ViewHolder(binding, bindingFront, bindingBack, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {

        int position = holder.getAdapterPosition();

        // safety check
        if (position == RecyclerView.NO_POSITION || position >= books.size()) {
            return;
        }

        Book book = books.get(position);

        // Set initial visibility based on flipped state
        if (position == flippedPosition) {
            holder.cardFront.setVisibility(View.GONE);
            holder.cardBack.setVisibility(View.VISIBLE);
            // Reset any rotation values
        } else {
            holder.cardFront.setVisibility(View.VISIBLE);
            holder.cardBack.setVisibility(View.GONE);
            // Reset any rotation values
        }
        holder.cardFront.setRotationY(0f);
        holder.cardBack.setRotationY(0f);

        // set data in cards
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(holder.coverFront);
            Glide.with(holder.itemView.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(holder.coverBack);
        }
        else {
            holder.coverFront.setImageResource(book.getCover());
            holder.coverBack.setImageResource(book.getCover());
        }
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        if (book.getSeries() == null || book.getSeries().isEmpty() || book.getSeries().equals("Standalone")) {
            holder.series.setVisibility(View.GONE);
        }
        else {
            holder.series.setText(book.getSeries());
            holder.series.setVisibility(View.VISIBLE);
        }
        if (book.getNumber() != null && book.getNumber() > 0) {
            holder.series.append(" #" + book.getNumber().toString());
        }

    }

    public void setOnNoteListener(OnNoteListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    public void toggleFlip(int position) {
        int oldFlippedPosition = flippedPosition;

        if (flippedPosition == position) {
            flippedPosition = -1;
        }
        else {
            flippedPosition = position;
        }

        if (oldFlippedPosition != -1 && oldFlippedPosition != position) {
            notifyItemChanged(oldFlippedPosition);
        }
        notifyItemChanged(position);
    }

}
