package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentBookEditBinding;
import com.example.bookishproject.databinding.LayoutBookDetailsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
This class represents a BookEditFragment.
A Book Edit Fragment allows a user to edit a book's information or delete it.
 */
public class BookEditFragment extends Fragment {

    private static final String ARG_ENTRY = "book";

    private Book book;
    private TextView title, author, series, number, categories;
    private Spinner genre, ageRange;
    private ImageView cover;
    private FloatingActionButton detailsButton;
    private RadioButton unread, currentlyReading, read, dnf;
    private RatingBar rating;
    private TextInputEditText review;
    private Button saveDetails, saveMyDetails;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private BookFirebaseHelper fbHelper;

    public BookEditFragment() {
        // Required empty public constructor
    }

    public BookEditFragment(Book book) {
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

        FragmentBookEditBinding binding = FragmentBookEditBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        View bottomSheetView = view.findViewById(R.id.book_details_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        LayoutBookDetailsBottomSheetBinding bottomSheetBinding = LayoutBookDetailsBottomSheetBinding.bind(bottomSheetView);


        fbHelper = new BookFirebaseHelper();

        title = binding.bookEditTitle;
        author = binding.bookEditAuthor;
        series = binding.bookEditSeries;
        number = binding.bookEditNumber;
        categories = binding.bookEditCategories;
        genre = binding.bookEditGenre;
        ageRange = binding.bookEditAgeRange;
        detailsButton = binding.fabShowDetails;
        cover = binding.bookEditCover;
        saveDetails = binding.bookEditSubmit;

        unread = bottomSheetBinding.statusToRead;
        currentlyReading = bottomSheetBinding.statusCurrentlyReading;
        read = bottomSheetBinding.statusRead;
        dnf = bottomSheetBinding.statusDNF;
        rating = bottomSheetBinding.bookEditStarRating;
        review = bottomSheetBinding.bookEditReview;
        saveMyDetails = bottomSheetBinding.buttonBookEditSave;

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set all fields with data from the Book
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(cover.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(cover);
        }
        else {
            cover.setImageResource(book.getCover());
        }
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            series.setHint(book.getSeries());
        }
        else {
            series.setHint("Series");
        }
        if (book.getNumber() != null) {
            number.setHint(String.valueOf(book.getNumber()));
        }
        else {
            number.setHint("number");
        }

        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
        genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genre.setAdapter(genreAdapter);
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            for (int i = 0; i < genreAdapter.getCount(); i++) {
                if (genreAdapter.getItem(i).toString().equals(book.getGenre())) {
                    genre.setSelection(i);
                    break;
                }
            }
        }

        ArrayAdapter<CharSequence> ageRangeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
        ageRangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        ageRange.setAdapter(ageRangeAdapter);
        if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
            for (int i = 0; i < ageRangeAdapter.getCount(); i++) {
                if (ageRangeAdapter.getItem(i).toString().equals(book.getAgeRange())) {
                    ageRange.setSelection(i);
                    break;
                }
            }
        }

        categories.setText(String.join(",", book.getCategories()));

        if (book.getStatus() != null && !book.getStatus().isEmpty()) {
            if (book.getStatus().equals("Read")) {
                read.setChecked(true);
            }
            if (book.getStatus().equals("Currently reading")) {
                currentlyReading.setChecked(true);
            }
            if (book.getStatus().equals("Want to read")) {
                unread.setChecked(true);
            }
            if (book.getStatus().equals("DNF")) {
                dnf.setChecked(true);
            }
        }
        rating.setRating(book.getRating());
        review.setText(book.getReview());

        saveDetails.setOnClickListener(v -> {
            if (series != null) {
                if (series.getText() != null && !series.getText().toString().isEmpty()) {
                    book.setSeries(series.getText().toString());
                }
            }
            if (number != null && number.getText() != null && !number.getText().toString().isEmpty()) {
                try {
                    book.setNumber(Integer.parseInt(number.getText().toString()));
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }

            if (!genre.getSelectedItem().toString().equals("Select genre...")){
                book.setGenre(genre.getSelectedItem().toString());
            }

            if (!ageRange.getSelectedItem().toString().equals("Select age range...")){
                book.setAgeRange(ageRange.getSelectedItem().toString());
            }

            if (categories != null) {
                if (categories.getText() != null && !categories.getText().toString().isEmpty()) {
                    String categoriesString = categories.getText().toString();
                    List<String> bookCategories = new ArrayList<>();
                    String[] categoriesArray = categoriesString.split(",");
                    for (String category : categoriesArray) {
                        category.trim();
                        bookCategories.add(category);
                    }
                    book.setCategories(bookCategories);
                }
            }

            fbHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                @Override
                public void onCallback(List<Book> bookList) {

                    // check for null activity
                    if (getActivity() == null) {
                        return;
                    }

                    // moves operations from a background thread to the UI thread to update the recycler view with Books
                    getActivity().runOnUiThread(() -> {

                        Toast.makeText(getContext(), "Book updated successfully!", Toast.LENGTH_SHORT).show();

                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).getNavigator().navigateToBookFragment(book);
                        }

                    });

                }
            });

        });

        detailsButton.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        saveMyDetails.setOnClickListener(v -> {

            String previousStatus = book.getStatus();
            String newStatus;

            if (unread.isChecked()) {
                newStatus = "Want to read";
            }
            else if (currentlyReading.isChecked()) {
                newStatus = "Currently reading";
            }
            else if (read.isChecked()) {
                newStatus = "Read";
            }
            else if (dnf.isChecked()) {
                newStatus = "DNF";
            }
            else {
                newStatus = "";
            }

            if (!previousStatus.equals(newStatus)) {

                Entry newEntry = new Entry(book);
                LocalDate today = LocalDate.now();
                newEntry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));

                if (newStatus.equals("Currently reading")) {
                    newEntry.setType(EntryType.STARTED);
                }
                if (newStatus.equals("Read") && previousStatus.equals("Currently reading")) {
                    newEntry.setType(EntryType.FINISHED);
                }
                if (newStatus.equals("DNF") && previousStatus.equals("Currently reading")) {
                    newEntry.setType(EntryType.ABANDONED);
                }

                newEntry.updateDescription();
                JournalFirebaseHelper fbHelper = new JournalFirebaseHelper();
                fbHelper.addEntry(newEntry);

            }

            book.setRating(rating.getRating());

            if (!review.getText().toString().isEmpty()) {
                book.setReview(review.getText().toString());
            }

            fbHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                @Override
                public void onCallback(List<Book> bookList) {

                    // check for null activity
                    if (getActivity() == null) {
                        return;
                    }

                    // moves operations from a background thread to the UI thread to update the recycler view with Books
                    getActivity().runOnUiThread(() -> {

                        Toast.makeText(getContext(), "Book updated successfully!", Toast.LENGTH_SHORT).show();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    });

                }
            });

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setToolbar(this);
        }
    }

}