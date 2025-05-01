package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
import com.example.bookishproject.databinding.FragmentBookBinding;
import com.example.bookishproject.databinding.LayoutBookDetailsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
A class for a BookFragment.
BookFragment is a non-editable view of a Book object's full attributes.
It uses view binding, a Book object, and fields from the fragment layout.
It also has a static final variable
 */
public class BookFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Book whose information to populate its fields with
    // it receives this from BooksFragment
    private static final String ARG_BOOK = "book";

    FragmentBookBinding binding;
    private Book book;
    private TextView title, author, bookDetails, seriesInput, seriesInputEdit, numberInput, numberInputEdit, pubDateInput, pubDateInputEdit, themesInput, themesInputEdit, synopsis, sheetTitle, review, editWarning, deleteLabel;
    private TextInputLayout seriesLayout, seriesLayoutEditable, numberLayout, numberLayoutEditable, pubDateLayout, pubDateLayoutEditable, themesLayout, themesLayoutEditable, synopsisLayout;
    private CardView genreCard, genreCardEditable, ageRangeCard, ageRangeCardEditable, editMessage;
    private RadioButton unread, currentlyReading, read, dnf;
    private Spinner genre, genreEdit, ageRange, ageRangeEdit;
    private ImageView cover;
    private MaterialButton details;
    private RatingBar rating;
    private Button submit, delete;
    private ExtendedFloatingActionButton edit, save;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private BookFirebaseHelper fbHelper;


    // a constructor using a Book object as a parameter / information source
    public BookFragment (Book book) {
        this.book = book;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // the Book whose data will populate the fragment will come from the bundle with this name
            book = getArguments().getParcelable(ARG_BOOK);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        View bottomSheetView = view.findViewById(R.id.bookDetailsSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        LayoutBookDetailsBottomSheetBinding bottomSheetBinding = LayoutBookDetailsBottomSheetBinding.bind(bottomSheetView);

        fbHelper = new BookFirebaseHelper();

        // CONSTANT FIELDS - MAIN VIEW

        cover = binding.imageCover;
        title = binding.titleInput;
        author = binding.authorInput;
        synopsis = binding.synopsisInput;
        editMessage = binding.editModeIndicator;
        editWarning = binding.editMessage;
        synopsisLayout = binding.synopsisInputLayout;

        details = binding.myDetails;

        // EDITABLE - MAIN VIEW

        bookDetails = binding.bookDetails;
        seriesInput = binding.seriesInput;
        seriesInputEdit = binding.seriesInputEditable;
        numberInput = binding.numberInput;
        numberInputEdit = binding.numberInputEditable;
        pubDateInput = binding.pubDateInput;
        pubDateInputEdit = binding.pubDateInputEditable;
        themesInput = binding.themesInput;
        themesInputEdit = binding.themesInputEditable;

        deleteLabel = binding.deleteLabel;

        seriesLayout = binding.seriesInputLayout;
        seriesLayoutEditable = binding.seriesInputLayoutEditable;
        numberLayout = binding.numberInputLayout;
        numberLayoutEditable = binding.numberInputLayoutEditable;
        pubDateLayout = binding.pubDateInputLayout;
        pubDateLayoutEditable = binding.pubDateInputLayoutEditable;
        themesLayout = binding.themesInputLayout;
        themesLayoutEditable = binding.themesInputLayoutEditable;

        genre = binding.genreSpinner;
        genreEdit = binding.genreSpinnerEditable;
        ageRange = binding.ageSpinner;
        ageRangeEdit = binding.ageSpinnerEditable;

        genreCard = binding.genreCard;
        genreCardEditable = binding.genreCardEditable;
        ageRangeCard = binding.ageCard;
        ageRangeCardEditable = binding.ageCardEditable;

        edit = binding.bookEdit;
        save = binding.bookSave;
        submit = bottomSheetBinding.bookSubmit;
        delete = binding.deleteButton;

        // CONSTANT FIELDS - BOTTOM SHEET

        sheetTitle = bottomSheetBinding.sheetBookTitle;
        review = bottomSheetBinding.bookEditReview;

        unread = bottomSheetBinding.statusToRead;
        currentlyReading = bottomSheetBinding.statusCurrentlyReading;
        read = bottomSheetBinding.statusRead;
        dnf = bottomSheetBinding.statusDNF;

        rating = bottomSheetBinding.bookEditStarRating;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setViewOnly();

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

        title.setText(book.getTitle());
        author.setText(book.getAuthor());

        synopsis.setText(book.getSynopsis());

        details.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        sheetTitle.setText(book.getTitle());
        review.setText(book.getReview());

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

        submit.setOnClickListener(v -> {
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
                    newEntry.setType("Started");
                }
                if (newStatus.equals("Read") && previousStatus.equals("Currently reading")) {
                    newEntry.setType("Finished");
                }
                if (newStatus.equals("DNF") && previousStatus.equals("Currently reading")) {
                    newEntry.setType("Abandoned");
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

        edit.setOnClickListener(v -> {
            setEditable();
        });

        save.setOnClickListener(v -> {

            if (seriesInputEdit != null) {
                if (seriesInputEdit.getText() != null && !seriesInputEdit.getText().toString().isEmpty()) {
                    book.setSeries(seriesInputEdit.getText().toString());
                }
            }

            if (numberInputEdit != null && numberInputEdit.getText() != null && !numberInputEdit.getText().toString().isEmpty()) {
                try {
                    book.setNumber(Integer.parseInt(numberInputEdit.getText().toString()));
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }

            if (!genreEdit.getSelectedItem().toString().equals("Select genre...")){
                book.setGenre(genreEdit.getSelectedItem().toString());
            }
            else {
                book.setGenre(genreEdit.getSelectedItem().toString());
            }
            ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
            genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
            genre.setAdapter(genreAdapter);
            if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                for (int i = 0; i < genreAdapter.getCount(); i++) {
                    if (genreAdapter.getItem(i).toString().equals(book.getGenre())) {
                        genre.setSelection(i);
                        break;
                    }
                }
            }
            genre.setEnabled(false);

            if (!ageRangeEdit.getSelectedItem().toString().equals("Select age range...")){
                book.setAgeRange(ageRangeEdit.getSelectedItem().toString());
            }
            else {
                book.setAgeRange(ageRangeEdit.getSelectedItem().toString());
            }
            ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
            ageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
            ageRange.setAdapter(ageAdapter);
            if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
                for (int i = 0; i < ageAdapter.getCount(); i++) {
                    if (ageAdapter.getItem(i).toString().equals(book.getAgeRange())) {
                        ageRange.setSelection(i);
                        break;
                    }
                }
            }
            ageRange.setEnabled(false);

            if (themesInputEdit != null) {
                if (themesInputEdit.getText() != null && !themesInputEdit.getText().toString().isEmpty()) {
                    String categoriesString = themesInputEdit.getText().toString();
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

                        setViewOnly();

                    });

                }
            });

        });

        delete.setOnClickListener(v -> {
            String id = book.getApiId();

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("Delete book?" )
                    .setMessage("Do you really want to delete \"" + book.getTitle() + "\" from your collection? Your journal entries for this book will be preserved in your reading log.")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // User clicked Cancel, do nothing
                        dialog.dismiss();
                    }).setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed deletion, now call the delete method
                        fbHelper.deleteBook(id);
                        if (getParentFragment() instanceof BooksHostFragment) {
                            ((BooksHostFragment) getParentFragment()).onBackPressed();
                        }
                    });
            builder.show();

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.setToolbar(this);
        }
    }

    public void setEditable() {
        bookDetails.setText("Edit book details");

//        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
//            series.setText(book.getSeries());
//            if (book.getNumber() != null && book.getNumber() > 0) {
//                series.append(" #" + book.getNumber().toString());
//            }
//        }

        seriesInput.setVisibility(View.GONE);
        seriesLayout.setVisibility(View.GONE);
        numberInput.setVisibility(View.GONE);
        numberLayout.setVisibility(View.GONE);
        pubDateInput.setVisibility(View.GONE);
        pubDateLayout.setVisibility(View.GONE);
        themesInput.setVisibility(View.GONE);
        themesLayout.setVisibility(View.GONE);
        genre.setVisibility(View.GONE);
        genreCard.setVisibility(View.GONE);
        ageRange.setVisibility(View.GONE);
        ageRangeCard.setVisibility(View.GONE);

        edit.setVisibility(View.GONE);

        editMessage.setVisibility(View.VISIBLE);

        seriesLayoutEditable.setVisibility(View.VISIBLE);
        seriesInputEdit.setVisibility(View.VISIBLE);
        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            seriesInputEdit.setText(book.getSeries());
        }
        else {
            seriesInputEdit.setHint("No series set");
        }

        numberLayoutEditable.setVisibility(View.VISIBLE);
        numberInputEdit.setVisibility(View.VISIBLE);
        if (book.getNumber() != null && book.getNumber() > 0) {
            numberInputEdit.setText(book.getNumber().toString());
        }
        else {
            numberInputEdit.setHint("0");
        }

        pubDateLayoutEditable.setVisibility(View.VISIBLE);
        pubDateInputEdit.setVisibility(View.VISIBLE);
        if (book.getPubYear() != null && !book.getPubYear().isEmpty()) {
            pubDateInputEdit.setText(book.getPubYear());
        }
        else {
            pubDateInputEdit.setHint("No set publication date");
        }

        themesLayoutEditable.setVisibility(View.VISIBLE);
        themesInputEdit.setVisibility(View.VISIBLE);
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            themesInputEdit.setText(String.join(",", book.getCategories()));
        }
        else {
            themesInputEdit.setHint("No tags set");
        }

        genreCardEditable.setVisibility(View.VISIBLE);
        genreEdit.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
        genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
        genreEdit.setAdapter(genreAdapter);
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            for (int i = 0; i < genreAdapter.getCount(); i++) {
                if (genreAdapter.getItem(i).toString().equals(book.getGenre())) {
                    genreEdit.setSelection(i);
                    break;
                }
            }
        }

        ageRangeCardEditable.setVisibility(View.VISIBLE);
        ageRangeEdit.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> ageRangeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
        ageRangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
        ageRangeEdit.setAdapter(ageRangeAdapter);
        if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
            for (int i = 0; i < ageRangeAdapter.getCount(); i++) {
                if (ageRangeAdapter.getItem(i).toString().equals(book.getAgeRange())) {
                    ageRangeEdit.setSelection(i);
                    break;
                }
            }
        }

        save.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        deleteLabel.setVisibility(View.VISIBLE);

    }

    public void setViewOnly() {
        bookDetails.setText("Book details");

//        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
//            series.setText(book.getSeries());
//            if (book.getNumber() != null && book.getNumber() > 0) {
//                series.append(" #" + book.getNumber().toString());
//            }
//        }
//        else {
//            series.setVisibility(View.GONE);
//        }

        seriesLayoutEditable.setVisibility(View.GONE);
        numberLayoutEditable.setVisibility(View.GONE);
        pubDateLayoutEditable.setVisibility(View.GONE);
        themesLayoutEditable.setVisibility(View.GONE);

        seriesInputEdit.setVisibility(View.GONE);
        numberInputEdit.setVisibility(View.GONE);
        pubDateInputEdit.setVisibility(View.GONE);
        themesInputEdit.setVisibility(View.GONE);
        genreEdit.setVisibility(View.GONE);
        ageRangeEdit.setVisibility(View.GONE);
        genreCardEditable.setVisibility(View.GONE);
        ageRangeCardEditable.setVisibility(View.GONE);

        save.setVisibility(View.GONE);
        editMessage.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        deleteLabel.setVisibility(View.GONE);

        seriesLayout.setVisibility(View.VISIBLE);
        seriesInput.setVisibility(View.VISIBLE);
        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            seriesInput.setText(book.getSeries());
        }
        else {
            seriesInput.setHint("No series set");
        }

        numberLayout.setVisibility(View.VISIBLE);
        numberInput.setVisibility(View.VISIBLE);
        if (book.getNumber() != null && book.getNumber() > 0) {
            numberInput.setText(book.getNumber().toString());
        }
        else {
            numberInput.setHint("0");
        }

        pubDateLayout.setVisibility(View.VISIBLE);
        pubDateInput.setVisibility(View.VISIBLE);
        if (book.getPubYear() != null && !book.getPubYear().isEmpty()) {
            pubDateInput.setText(book.getPubYear());
        }
        else {
            pubDateInput.setHint("No publication date found");
        }

        themesLayout.setVisibility(View.VISIBLE);
        themesInput.setVisibility(View.VISIBLE);
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            themesInput.setText(String.join(",", book.getCategories()));
        }
        else {
            themesInput.setHint("No tags assigned to this book");
        }

        genreCard.setVisibility(View.VISIBLE);
        genre.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
        genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
        genre.setAdapter(genreAdapter);
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            for (int i = 0; i < genreAdapter.getCount(); i++) {
                if (genreAdapter.getItem(i).toString().equals(book.getGenre())) {
                    genre.setSelection(i);
                    break;
                }
            }
        }
        genre.setEnabled(false);

        ageRangeCard.setVisibility(View.VISIBLE);
        ageRange.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> ageRangeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
        ageRangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
        ageRange.setAdapter(ageRangeAdapter);
        if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
            for (int i = 0; i < ageRangeAdapter.getCount(); i++) {
                if (ageRangeAdapter.getItem(i).toString().equals(book.getAgeRange())) {
                    ageRange.setSelection(i);
                    break;
                }
            }
        }
        ageRange.setEnabled(false);

        edit.setVisibility(View.VISIBLE);

    }


}