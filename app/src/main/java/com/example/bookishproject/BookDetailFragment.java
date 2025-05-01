package com.example.bookishproject;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
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
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
A class for a BookFragment.
A BookFragment has two modes: view-only and editable.
View-only mode displays information about a Book object's attributes.
Editable mode allows the user to change the book's attributes or delete the book from their collection.
It uses view binding, a Book object, and fields from the fragment layout.
It also has a static final variable.
 */
public class BookDetailFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Book whose information to populate its fields with
    // it receives this from BooksFragment
    private static final String ARG_BOOK = "book";

    private FragmentBookBinding binding;
    private TextView title, author, bookDetails, seriesInput, seriesInputEdit, numberInput, numberInputEdit, pubDateInput, pubDateInputEdit, themesInput, themesInputEdit, synopsis, sheetTitle, review, deleteLabel;
    private TextInputLayout seriesLayout, seriesLayoutEditable, numberLayout, numberLayoutEditable, pubDateLayout, pubDateLayoutEditable, themesLayout, themesLayoutEditable;
    private CardView genreCard, genreCardEditable, ageRangeCard, ageRangeCardEditable, editMessage;
    private ScrollView scrollView;
    private RadioButton unread, currentlyReading, read, dnf;
    private Spinner genre, genreEdit, ageRange, ageRangeEdit;
    private ImageView cover;
    private MaterialButton details;
    private RatingBar rating;
    private Button submit, delete;
    private ExtendedFloatingActionButton edit, save;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private Book book;
    private BookFirebaseHelper fbHelper;


    // a constructor using a Book object as a parameter / information source
    public BookDetailFragment(Book book) {
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
        scrollView = binding.scrollView2;
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
        sheetTitle.setText(book.getTitle());
        review.setText(book.getReview());
        rating.setRating(book.getRating());

        // set the status radio button group based on the book's status
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

        // set operations for the "My Details" button
        details.setOnClickListener(v -> {
            // pull up the "my details" bottom sheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // set operations for the "Save my details" button
        submit.setOnClickListener(v -> {
            // remember the book's previous status
            String previousStatus = book.getStatus();
            // but also plan the new status based on which radio group button is checked
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

            // if there has been a status change
            if (!previousStatus.equals(newStatus)) {

                // we gotta make a new entry for our reading journal to track the activity!
                Entry newEntry = new Entry(book);
                // set the date to today
                LocalDate today = LocalDate.now();
                newEntry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));

                // set the activity type for the entry based on the new status
                if (newStatus.equals("Currently reading")) {
                    newEntry.setType("Started");
                }
                if (newStatus.equals("Read") && previousStatus.equals("Currently reading")) {
                    newEntry.setType("Finished");
                }
                if (newStatus.equals("DNF") && previousStatus.equals("Currently reading")) {
                    newEntry.setType("Abandoned");
                }

                // update the entry's description based on the activity type
                newEntry.updateDescription();
                // add the entry to our database
                JournalFirebaseHelper fbHelper = new JournalFirebaseHelper();
                fbHelper.addEntry(newEntry);

            }

            // set the book's rating
            book.setRating(rating.getRating());

            // set the book's review
            if (!review.getText().toString().isEmpty()) {
                book.setReview(review.getText().toString());
            }

            // update the book in the database
            fbHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                @Override
                public void onCallback(List<Book> bookList) {

                    // check for null activity
                    if (getActivity() == null) {
                        return;
                    }

                    // moves operations from a background thread to the UI thread to update the recycler view with Books
                    getActivity().runOnUiThread(() -> {

                        // alert the user
                        Toast.makeText(getContext(), "Book updated successfully!", Toast.LENGTH_SHORT).show();
                        // put away the bottom sheet
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    });
                }
            });
        });

        // set operations for the "edit" button
        edit.setOnClickListener(v -> {
            // change to edit mode
            setEditable();
        });

        // set operations for the "save" button
        save.setOnClickListener(v -> {

            // we are currently in edit mode

            // if the user has input a series
            if (seriesInputEdit != null) {
                if (seriesInputEdit.getText() != null && !seriesInputEdit.getText().toString().isEmpty()) {
                    // set this as the book's series
                    book.setSeries(seriesInputEdit.getText().toString());
                }
            }

            // if the user has input a number
            if (numberInputEdit != null && numberInputEdit.getText() != null && !numberInputEdit.getText().toString().isEmpty()) {
                try {
                    // set this as the book's number
                    book.setNumber(Integer.parseInt(numberInputEdit.getText().toString()));
                } catch (NumberFormatException e) {
                    // but make sure it's an integer
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }

            // if the user has entered a genre
            if (!genreEdit.getSelectedItem().toString().equals("Select genre...")){
                // set this as the book's genre
                book.setGenre(genreEdit.getSelectedItem().toString());
            }

//            // set up a spinner array adapter so we can interact with the spinner
//            // user the strings.xml genre_array values as the options
//            // use layout/spinner_item as the layout for the spinner items
//            ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
//            // use the spinner_dropdown_item_books as the dropdown view
//            genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
//            // set this adapter as our view-only spinner adapter
//            genre.setAdapter(genreAdapter);
//            // if the book has a genre
//            if (book.getGenre() != null && !book.getGenre().isEmpty()) {
//                // check each item in the view-only genre spinner to see if it matches the book genre
//                for (int i = 0; i < genreAdapter.getCount(); i++) {
//                    if (genreAdapter.getItem(i).toString().equals(book.getGenre())) {
//                        // if it does, set that item to the selected item of the spinner
//                        genre.setSelection(i);
//                        break;
//                    }
//                }
//            }
            // disable the view-only genre spinner
            genre.setEnabled(false);

            // if the user has entered an age range
            if (!ageRangeEdit.getSelectedItem().toString().equals("Select age range...")){
                // set this as the book's age range
                book.setAgeRange(ageRangeEdit.getSelectedItem().toString());
            }

//            // set up a spinner array adapter so we can interact with the spinner
//            // user the strings.xml age_array values as the options
//            // use layout/spinner_item as the layout for the spinner items
//            ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
//            // use the spinner_dropdown_item_books as the dropdown view
//            ageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
//            // set this adapter as our view-only age range adapter
//            ageRange.setAdapter(ageAdapter);
//            // if the book has an age range
//            if (book.getAgeRange() != null && !book.getAgeRange().isEmpty()) {
//                // check each item in the view-only age range spinner to see if it matches the book age range
//                for (int i = 0; i < ageAdapter.getCount(); i++) {
//                    if (ageAdapter.getItem(i).toString().equals(book.getAgeRange())) {
//                        // if it does, set that item to the selected item of the spinner
//                        ageRange.setSelection(i);
//                        break;
//                    }
//                }
//            }
            // disable the view-only age range spinner
            ageRange.setEnabled(false);

            // if the user has input themes
            if (themesInputEdit != null) {
                if (themesInputEdit.getText() != null && !themesInputEdit.getText().toString().isEmpty()) {
                    // convert the input themes to a string
                    String categoriesString = themesInputEdit.getText().toString();
                    // make a new themes list
                    List<String> bookCategories = new ArrayList<>();
                    // split the input string by commas and store them in an array
                    String[] categoriesArray = categoriesString.split(",");
                    // for each item in the array
                    for (String category : categoriesArray) {
                        // remove the whitespace
                        category.trim();
                        // add it to the themes list
                        bookCategories.add(category);
                    }
                    // set this themes list as the book's categories
                    book.setCategories(bookCategories);
                }
            }

            // update the book in the database
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
                        // switch to view-only mode
                        setViewOnly();

                    });
                }
            });

        });

        // set operations for delete button
        delete.setOnClickListener(v -> {
            // get the api id of the book
            String id = book.getApiId();

            // dialog box: do you really want to delete this book?
            // if no ("cancel"), close the box and never mind
            // if yes ("delete"), delete the book from the database and go back
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

        // make sure the keyboard doesn't get in the way
        setupKeyboardAdjustment(view);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }
    }

    /*
    Method to set a BookFragment to edit mode
    This will set all the view-only elements to gone and all the editable elements to visible
    Also will set all the editable elements' values to the books values where present
    Also will show the save button
     */
    public void setEditable() {

        // set all the view-only fields to gone
        bookDetails.setText("Edit book details");
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

        // set all the edit-mode fields to visible
        editMessage.setVisibility(View.VISIBLE);
        seriesLayoutEditable.setVisibility(View.VISIBLE);
        seriesInputEdit.setVisibility(View.VISIBLE);
        numberLayoutEditable.setVisibility(View.VISIBLE);
        numberInputEdit.setVisibility(View.VISIBLE);
        pubDateLayoutEditable.setVisibility(View.VISIBLE);
        pubDateInputEdit.setVisibility(View.VISIBLE);
        themesLayoutEditable.setVisibility(View.VISIBLE);
        themesInputEdit.setVisibility(View.VISIBLE);
        genreCardEditable.setVisibility(View.VISIBLE);
        genreEdit.setVisibility(View.VISIBLE);
        ageRangeCardEditable.setVisibility(View.VISIBLE);
        ageRangeEdit.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        deleteLabel.setVisibility(View.VISIBLE);

        // Populate all the text fields with the book's information

        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            seriesInputEdit.setText(book.getSeries());
        }
        else {
            seriesInputEdit.setHint("No series set");
        }

        if (book.getNumber() != null && book.getNumber() > 0) {
            numberInputEdit.setText(book.getNumber().toString());
        }
        else {
            numberInputEdit.setHint("0");
        }

        if (book.getPubYear() != null && !book.getPubYear().isEmpty()) {
            pubDateInputEdit.setText(book.getPubYear());
        }
        else {
            pubDateInputEdit.setHint("No set publication date");
        }

        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            themesInputEdit.setText(String.join(",", book.getCategories()));
        }
        else {
            themesInputEdit.setHint("No tags set");
        }

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



    }

    /*
    Method to set fragment to view-only
     */
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

        // set editable fields/elements to gone

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

        // set view-only fields and elements to visible

        seriesLayout.setVisibility(View.VISIBLE);
        seriesInput.setVisibility(View.VISIBLE);
        numberLayout.setVisibility(View.VISIBLE);
        numberInput.setVisibility(View.VISIBLE);
        pubDateLayout.setVisibility(View.VISIBLE);
        pubDateInput.setVisibility(View.VISIBLE);
        themesLayout.setVisibility(View.VISIBLE);
        themesInput.setVisibility(View.VISIBLE);
        genreCard.setVisibility(View.VISIBLE);
        genre.setVisibility(View.VISIBLE);
        ageRangeCard.setVisibility(View.VISIBLE);
        ageRange.setVisibility(View.VISIBLE);
        edit.setVisibility(View.VISIBLE);

        // populate view-only fields with book's attributes

        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            seriesInput.setText(book.getSeries());
        }
        else {
            seriesInput.setHint("No series set");
        }

        if (book.getNumber() != null && book.getNumber() > 0) {
            numberInput.setText(book.getNumber().toString());
        }
        else {
            numberInput.setHint("0");
        }

        if (book.getPubYear() != null && !book.getPubYear().isEmpty()) {
            pubDateInput.setText(book.getPubYear());
        }
        else {
            pubDateInput.setHint("No publication date found");
        }

        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            themesInput.setText(String.join(",", book.getCategories()));
        }
        else {
            themesInput.setHint("No tags assigned to this book");
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

    }

    /*
    Method to ensure keyboard doesn't block view
     */
    private void setupKeyboardAdjustment(View rootView) {

        // create listener for ViewTreeObserver - monitors when layout changes occur (i.e., when a keyboard appears or disappears)
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // create new rectangle
                Rect r = new Rect();
                // fill rectangle with current visible area of window
                rootView.getWindowVisibleDisplayFrame(r);
                // get full heigh of window screen
                int screenHeight = rootView.getRootView().getHeight();
                // use these two measurements to determine current height of the keyboard
                int keyboardHeight = screenHeight - r.bottom;

                // if the keyboard heigh is significatn (if keyboard is currently visible)
                if (keyboardHeight > screenHeight * 0.15) {
                    // Find the currently focused view
                    View focused = getActivity().getCurrentFocus();
                    if (focused != null) {
                        // create an array to hold x,y coordinates
                        int[] location = new int[2];
                        // fills array with absolute screen position of focused view
                        focused.getLocationOnScreen(location);
                        // scrolls to location
                        // makes sure location is visible above keyboard
                        scrollView.smoothScrollTo(0, location[1] - keyboardHeight);
                    }
                }
            }
        });
    }

}