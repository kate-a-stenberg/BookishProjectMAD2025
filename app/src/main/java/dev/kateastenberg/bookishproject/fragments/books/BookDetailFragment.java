package dev.kateastenberg.bookishproject.fragments.books;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

import dev.kateastenberg.bookishproject.helpers.firebase.UserBookFirebaseHelper;
import dev.kateastenberg.bookishproject.helpers.firebase.BookFirebaseHelper;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.models.Date;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.helpers.firebase.JournalFirebaseHelper;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.FragmentBookDetailBinding;
import dev.kateastenberg.bookishproject.databinding.LayoutBookDetailsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
A class for a BookDetailFragment.
A BookDetailFragment has two modes: view-only and editable.
View-only mode displays information about a Book object's attributes.
Editable mode allows the user to change the book's attributes or delete the book from their collection.
Edit mode is only available to admin users.
It uses view binding, a UserBook object, and fields from the fragment layout.
It also has a static final variable.
 */
public class BookDetailFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Book whose information to populate its fields with
    // it receives this from BooksFragment
    private static final String ARG_USER_BOOK = "user_book";

    private TextView title, author, bookDetails, seriesInput, numberInput, pubDateInput, themesInput, synopsis, sheetTitle, review, deleteLabel, removeLabel;
    private CardView editMessage;
    private RadioButton unread, currentlyReading, read, dnf;
    private Spinner genre, ageRange;
    private ImageView cover;
    private MaterialButton details;
    private RatingBar rating;
    private Button submit, delete, remove;
    private ExtendedFloatingActionButton edit, save;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private UserBook userBook;
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;


    // a constructor using a Book object as a parameter / information source
    public BookDetailFragment(UserBook userBook) {
        this.userBook = userBook;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // the Book whose data will populate the fragment will come from the bundle with this name
            userBook = getArguments().getParcelable(ARG_USER_BOOK);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentBookDetailBinding binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        View bottomSheetView = view.findViewById(R.id.bookDetailsSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        LayoutBookDetailsBottomSheetBinding bottomSheetBinding = LayoutBookDetailsBottomSheetBinding.bind(bottomSheetView);

        // CONSTANT FIELDS - MAIN VIEW

        cover = binding.imageCover;
        title = binding.titleInput;
        author = binding.authorInput;
        synopsis = binding.synopsisInput;
        editMessage = binding.editModeIndicator;
        details = binding.myDetails;

        // EDITABLE - MAIN VIEW

        bookDetails = binding.bookDetails;
        seriesInput = binding.seriesInput;
        numberInput = binding.numberInput;
        pubDateInput = binding.pubDateInput;
        themesInput = binding.themesInput;

        deleteLabel = binding.deleteLabel;
        removeLabel = binding.removeLabel;

        genre = binding.genreSpinner;
        ageRange = binding.ageSpinner;

        edit = binding.bookEdit;
        save = binding.bookSave;
        submit = bottomSheetBinding.bookSubmit;
        delete = binding.deleteButton;
        remove = binding.removeButton;

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

        bvm = new ViewModelProvider(this).get(BooksViewModel.class);

        observeViewModel();

        setViewOnly();

        // something complicated with cover images. I don't really know about this, I looked it up
        // I think basically:
        // if the book has a cover url:
        // ask Glide send that coverUrl into the cover field, and if the url is not accessible then use this image as a placeholder
        if (userBook.getBook().getCoverUrl() != null && !userBook.getBook().getCoverUrl().isEmpty()) {
            Glide.with(cover.getContext())
                    .load(userBook.getBook().getCoverUrl())
                    .placeholder(R.drawable.book_cover_background)
                    .error(R.drawable.book_cover_background)
                    .into(cover);
        }
        else {
            cover.setImageResource(userBook.getBook().getCover());
        }

        title.setText(userBook.getBook().getTitle());
        author.setText(userBook.getBook().getAuthor());
        synopsis.setText(userBook.getBook().getSynopsis());
        sheetTitle.setText(userBook.getBook().getTitle());
        review.setText(userBook.getReview());
        rating.setRating(userBook.getRating());

        if (userBook.getBook().getSeries() != null && !userBook.getBook().getSeries().isEmpty()) {
            seriesInput.setText(userBook.getBook().getSeries());
        }
        else {
            seriesInput.setHint("No series set");
        }

        if (userBook.getBook().getNumber() != null && userBook.getBook().getNumber() > 0) {
            numberInput.setText(String.valueOf(userBook.getBook().getNumber()));
        }
        else {
            numberInput.setHint("0");
        }

        if (userBook.getBook().getPubYear() != null && !userBook.getBook().getPubYear().isEmpty()) {
            pubDateInput.setText(userBook.getBook().getPubYear());
        }
        else {
            pubDateInput.setHint("No publication date found");
        }

        if (userBook.getBook().getCategories() != null && !userBook.getBook().getCategories().isEmpty()) {
            themesInput.setText(String.join(",", userBook.getBook().getCategories()));
        }
        else {
            themesInput.setHint("No tags assigned to this book");
        }

        if (getContext() != null) {
            ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genre_array, R.layout.spinner_item);
            genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
            genre.setAdapter(genreAdapter);
            if (userBook.getBook().getGenre() != null && !userBook.getBook().getGenre().isEmpty()) {
                for (int i = 0; i < genreAdapter.getCount(); i++) {
                    if (String.valueOf(genreAdapter.getItem(i)).equals(userBook.getBook().getGenre())) {
                        genre.setSelection(i);
                        break;
                    }
                }
            }

            ArrayAdapter<CharSequence> ageRangeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.age_array, R.layout.spinner_item);
            ageRangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_books);
            ageRange.setAdapter(ageRangeAdapter);
            if (userBook.getBook().getAgeRange() != null && !userBook.getBook().getAgeRange().isEmpty()) {
                for (int i = 0; i < ageRangeAdapter.getCount(); i++) {
                    if (String.valueOf(ageRangeAdapter.getItem(i)).equals(userBook.getBook().getAgeRange())) {
                        ageRange.setSelection(i);
                        break;
                    }
                }
            }
        }

        // set the status radio button group based on the book's status
        if (userBook.getStatus() != null && !userBook.getStatus().isEmpty()) {
            if (userBook.getStatus().equals("Read")) {
                read.setChecked(true);
            }
            if (userBook.getStatus().equals("Currently reading")) {
                currentlyReading.setChecked(true);
            }
            if (userBook.getStatus().equals("Want to read")) {
                unread.setChecked(true);
            }
            if (userBook.getStatus().equals("DNF")) {
                dnf.setChecked(true);
            }
        }


        String transitionName = null;
        if (getArguments() != null) {
            transitionName = getArguments().getString("transition_name");
        }
        if (transitionName != null) {
            cover.setTransitionName(transitionName);
        }

        // set operations for the "My Details" button
        details.setOnClickListener(v -> {
            processIntent(new BooksIntent.SeeUserDetails());
        });

        // set operations for the "Save my details" button
        submit.setOnClickListener(v -> {

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

            userBook.setStatus(newStatus);

            // set the book's rating
            userBook.setRating(rating.getRating());

            // set the book's review
            if (!review.getText().toString().isEmpty()) {
                userBook.setReview(review.getText().toString());
            }

            processIntent(new BooksIntent.UpdateUserBook(userBook));

            processIntent(new BooksIntent.HideUserDetails());

        });

        // set operations for the "edit" button
        edit.setOnClickListener(v -> {
            // change to edit mode
            processIntent(new BooksIntent.SetEditable());
        });

        // set operations for the "save" button
        save.setOnClickListener(v -> {

            // we are currently in edit mode

            // if the user has input a series
            if (seriesInput != null) {
                if (seriesInput.getText() != null && !seriesInput.getText().toString().isEmpty()) {
                    // set this as the book's series
                    userBook.getBook().setSeries(seriesInput.getText().toString());
                }
            }

            // if the user has input a number
            if (numberInput != null && numberInput.getText() != null && !numberInput.getText().toString().isEmpty()) {
                try {
                    // set this as the book's number
                    userBook.getBook().setNumber(Integer.parseInt(numberInput.getText().toString()));
                } catch (NumberFormatException e) {
                    // but make sure it's an integer
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }

            // if the user has entered a genre
            if (!genre.getSelectedItem().toString().equals("Select genre...")){
                // set this as the book's genre
                userBook.getBook().setGenre(genre.getSelectedItem().toString());
            }

            // if the user has entered an age range
            if (!ageRange.getSelectedItem().toString().equals("Select age range...")){
                // set this as the book's age range
                userBook.getBook().setAgeRange(ageRange.getSelectedItem().toString());
            }

            // if the user has input themes
            if (themesInput != null) {
                if (themesInput.getText() != null && !themesInput.getText().toString().isEmpty()) {
                    // convert the input themes to a string
                    String categoriesString = themesInput.getText().toString();
                    // make a new themes list
                    List<String> bookCategories = getCategories(categoriesString);
                    // set this themes list as the book's categories
                    userBook.getBook().setCategories(bookCategories);
                }
            }

            processIntent(new BooksIntent.UpdateBook(userBook));
            setViewOnly();

        });

        // set operations for delete button
        // this will enable the admin to delete a book from the universal collection
        delete.setOnClickListener(v -> {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("Delete book?" )
                    .setMessage("Do you really want to delete \"" + userBook.getBook().getTitle()
                            + "\" from the universal collection? All journal entries for this book will be preserved in your reading log.")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // User clicked Cancel, do nothing
                        dialog.dismiss();
                    }).setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed deletion, now call the delete method
                        processIntent(new BooksIntent.DeleteBook(userBook.getBook()));
                        if (getParentFragment() instanceof BooksHostFragment) {
                            ((BooksHostFragment) getParentFragment()).onBackPressed();
                        }
                    });
            builder.show();
        });

        // this will enable a user to remove a book from their collection
        remove.setOnClickListener(v -> {

            processIntent(new BooksIntent.RemoveUserBook(userBook));

            // dialog box: do you really want to delete this book?
            // if no ("cancel"), close the box and never mind
            // if yes ("delete"), delete the book from the database and go back
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("Remove book?" )
                    .setMessage("Do you really want to remove \"" + userBook.getBook().getTitle()
                            + "\" from your collection? Your journal entries for this book will be preserved in your reading log.")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // User clicked Cancel, do nothing
                        dialog.dismiss();
                    }).setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed deletion, now call the delete method
                        processIntent(new BooksIntent.RemoveUserBook(userBook));
                        if (getParentFragment() instanceof BooksHostFragment) {
                            ((BooksHostFragment) getParentFragment()).onBackPressed();
                        }
                    });
            builder.show();
        });

    }

    /*
    Method to convert a list of categories from a String to an Array List
     */
    @NonNull
    private static List<String> getCategories(String categoriesString) {
        List<String> bookCategories = new ArrayList<>();
        // split the input string by commas and store them in an array
        String[] categoriesArray = categoriesString.split(",");
        // for each item in the array
        for (String category : categoriesArray) {
            // remove the whitespace
            String c = category.trim();
            // add it to the themes list
            bookCategories.add(c);
        }
        return bookCategories;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }
    }

    @Override
    public void onDestroy() {
        // Dispose to prevent memory leaks
        if (intentDisposable != null && !intentDisposable.isDisposed()) {
            intentDisposable.dispose();
        }

        if (intentSubject != null && !intentSubject.hasComplete()) {
            intentSubject.onComplete();
        }
        super.onDestroy();
    }

    /*
    Method to set a BookFragment to edit mode
     */
    @SuppressLint("SetTextI18n")
    private void setEditable() {

        // SET ALL FIELDS TO EDITABLE

        bookDetails.setText("Edit book details");

        setTextEditable(seriesInput, true);
        setTextEditable(numberInput, true);
        setTextEditable(pubDateInput, true);
        setTextEditable(themesInput, true);
        setSpinnerEditable(genre, true);
        setSpinnerEditable(ageRange, true);

        edit.setVisibility(View.GONE);

        // SET ALL EDIT MODE ELEMENTS TO VISIBLE

        editMessage.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        deleteLabel.setVisibility(View.VISIBLE);
        remove.setVisibility(View.VISIBLE);
        removeLabel.setVisibility(View.VISIBLE);

    }

    /*
    Method to set fragment to view-only
     */
    @SuppressLint("SetTextI18n")
    private void setViewOnly() {

        bookDetails.setText("Book details");

        // set edit-mode elements to gone

        save.setVisibility(View.GONE);
        editMessage.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        deleteLabel.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);
        removeLabel.setVisibility(View.GONE);

        // set fields as disabled and not focusable

        setTextEditable(seriesInput, false);
        setTextEditable(numberInput, false);
        setTextEditable(pubDateInput, false);
        setTextEditable(themesInput, false);
        setSpinnerEditable(genre, false);
        setSpinnerEditable(ageRange, false);
    }

    private void setTextEditable(TextView text, boolean value) {
        text.setFocusable(value);
        text.setFocusableInTouchMode(value);
        text.setCursorVisible(value);
    }

    private void setSpinnerEditable(Spinner spinner, boolean value) {
        spinner.setFocusable(value);
        spinner.setFocusableInTouchMode(value);
        spinner.setEnabled(value);
    }

    public ImageView getCover() {
        return this.cover;
    }

    private void observeViewModel() {
        bvm.getUserBook().observe(getViewLifecycleOwner(), book -> {
           this.userBook = book;
        });
        bvm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        bvm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
        bvm.getAdminStatus().observe(getViewLifecycleOwner(), isAdminUser -> {
            if (isAdminUser) {
                edit.setVisibility(View.VISIBLE);
                edit.setOnClickListener(v -> {
                    processIntent(new BooksIntent.SetEditable());
                });
            }
            else {
                edit.setVisibility(View.GONE);
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof BooksIntent.SeeUserDetails) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            else if (intent instanceof BooksIntent.AdminCheck) {
                bvm.handleIntent(intent);
            }
            else if (intent instanceof BooksIntent.SetEditable) {
                setEditable();
            }
            else if (intent instanceof BooksIntent.UpdateUserBook) {
                bvm.handleIntent(intent);
            }
            else if (intent instanceof BooksIntent.HideUserDetails) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            else if (intent instanceof BooksIntent.UpdateBook) {
                bvm.handleIntent(intent);
            }
            else if (intent instanceof BooksIntent.DeleteBook) {
                bvm.handleIntent(intent);
            }
            else if (intent instanceof BooksIntent.RemoveUserBook) {
                bvm.handleIntent(intent);
            }
        });


    }

    private void processIntent(BooksIntent intent) {
        intentSubject.onNext(intent);
    }

}