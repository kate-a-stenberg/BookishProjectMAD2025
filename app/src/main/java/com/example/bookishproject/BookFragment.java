package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentBookBinding;

/*
A class for a BookFragment.
BookFragment is a non-editable view of a Book object's full attributes.
It uses view binding, a Book object, and fields from the fragment layout.
It also has a static final variable
 */
public class BookFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Book whose information to populate its fields with
    // it receives this from BooksFragment
    private static final String ARG_ENTRY = "book";

    private Book book;
    private TextView title, author, pubDate, genre, ageRange, synopsis, categories;
    private ImageView cover;

    // a constructor using a Book object as a parameter / information source
    public BookFragment (Book book) {
        this.book = book;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // the Book whose data will populate the fragment will come from the bundle with this name
            book = getArguments().getParcelable(ARG_ENTRY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentBookBinding binding = FragmentBookBinding.inflate(inflater, container, false);

        // set all the layout fields we want to manipulate
        title = binding.textBookTitle;
        author = binding.textBookAuthor;
        pubDate = binding.textBookPubYear;
        genre = binding.textBookGenre;
        ageRange = binding.textBookAge;
        synopsis = binding.textSynopsis;
        categories = binding.textBookThemes;
        cover = binding.imageCover;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set all fields with data from the Book
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
            categories.setText(String.join(",", book.getCategories()));
        }
        // something complicated with cover images. I don't really know about this, I looked it up
        // I think basically:
        // if the book has a cover url:
        // ask Glide send that coverUrl into the cover field, and if the url is not accessible then use this image as a placeholder
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(cover.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(cover);
        }
        else {
            cover.setImageResource(book.getCover());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setToolbar(this);
        }
    }

}