package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentBookBinding;


public class BookFragment extends Fragment {

    FragmentBookBinding binding;
    private static final String ARG_ENTRY = "book";
    Book book;
    private TextView title, author, pubDate, genre, ageRange, synopsis, categories;
    private ImageView cover;
    private ImageButton backButton;


    public BookFragment (Book book) {
        this.book = book;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable("book");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookBinding.inflate(inflater, container, false);

        title = binding.textBookTitle;
        author = binding.textBookAuthor;
        pubDate = binding.textBookPubYear;
        genre = binding.textBookGenre;
        ageRange = binding.textBookAge;
        synopsis = binding.textSynopsis;
        categories = binding.textBookThemes;
        cover = binding.imageCover;

        backButton = binding.buttonBack;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (book.getTitle() != null && !book.getTitle().isEmpty()) {
            title.setText(book.getTitle());
        }
        if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
            author.setText(book.getAuthor());
        }
        if (book.getPubYear() != null && !book.getPubYear().isEmpty()) {
            pubDate.append(book.getPubYear());
        }
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            genre.append(book.getGenre());
        }
        if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
            ageRange.append(book.getAgeRange());
        }
        if (book.getSynopsis() != null && !book.getSynopsis().isEmpty()) {
            synopsis.setText(book.getSynopsis());
        }
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            categories.setText(book.getCategories().toString());
        }
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(cover.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(cover);
        }
        else {
            cover.setImageResource(book.getCover());
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();

                // For simplicity, we'll just go back to the search fragment
                // If you need to maintain state, you'd need to store the filtered books
                BooksFragment bookFragment = new BooksFragment();

                // Get the current position in the ViewPager
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace current fragment
                activity.getAdapter().replaceFragment(currentPosition, bookFragment);

                // Update ViewPager
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
        });
    }

}