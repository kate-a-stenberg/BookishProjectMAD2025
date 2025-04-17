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

    FragmentBookBinding binding;
    Book book;
    private TextView title, author, pubDate, genre, ageRange, synopsis, categories;
    private ImageView cover;
    private ImageButton backButton;


    // a contructor using a Book object as a parameter / information source
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookBinding.inflate(inflater, container, false);

        // set all the layout fields we want to manipulate
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
            categories.setText(book.getCategories().toString());
        }
        // something complicated with cover images. I don't really know about this, I looked it up
        // I think basically:
        // if the book has a cover url:
        // ask Glide send that coverUrl into the cover field, and if the url is not accessible then use this image as a placeholder
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(cover.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(cover);
        }
        else {
            // TODO: bring in a better placeholder cover as a user-created Book object will not have a cover. pwrf is a dumb one
            cover.setImageResource(book.getCover());
        }

        // the back button will ask MainActivity to go back to BooksFragment
        // yes this is cheating by creating a new BooksFragment but it was necessary
        // THIS IS THE ONLY WAY THIS WORKS OKAY
        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToBooksFragment();
            }
        });
    }

}