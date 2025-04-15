package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentNewEntryBinding;

import java.time.LocalDate;
import java.util.List;

public class NewEntryFragment extends Fragment {
    private static final String TAG = "Entry To Firebase";


    FragmentNewEntryBinding binding;
    TextView textTitle, textAuthor, textPages;
    EditText comments;
    ImageView coverImage;
    RadioButton buttonPages, buttonFinished, buttonAbandoned;
    ImageButton backButton;
    Button buttonSubmit;
    Book book;
    JournalFirebaseHelper fbHelper;
    BookFirebaseHelper bookFirebaseHelper;

    public NewEntryFragment() {
        // Required empty public constructor
    }

    public NewEntryFragment(Book book) {
        this.book = book;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("selected_book")) {
            book = getArguments().getParcelable("selected_book");
        }
        fbHelper = new JournalFirebaseHelper();
        bookFirebaseHelper = new BookFirebaseHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNewEntryBinding.inflate(inflater, container, false);

        textTitle = binding.textNewEntryTitle;
        textAuthor = binding.textNewEntryAuthor;
        textPages = binding.textPages;
        comments = binding.textComments;
        coverImage = binding.imageNewEntryCover;

        textTitle.setText(book.getTitle());
        textAuthor.setText(book.getAuthor());

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(coverImage.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(coverImage);
        }
        else {
            coverImage.setImageResource(book.getCover());
        }

        buttonPages = binding.radioPagesRead;
        buttonFinished = binding.radioFinished;
        buttonAbandoned = binding.radioAbandoned;
        backButton = binding.buttonNewEntryBack;
        buttonSubmit = binding.buttonNewEntrySubmit;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Create a new instance of RecsFragment
                OpenBooksFragment openBooksFragment = new OpenBooksFragment();

                // Get the current position in the ViewPager (should be 2 for the Recs tab)
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with the RecsFragment
                ((MainActivity) getActivity()).getAdapter().replaceFragment(currentPosition, openBooksFragment);

                // This will refresh the ViewPager with the new fragment
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
        });

        buttonSubmit.setOnClickListener(v -> {
            Log.d(TAG, "submit button pressed");

            if (book == null) {
                Toast.makeText(getContext(), "No book selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Entry newEntry = new Entry(book);
            LocalDate today = LocalDate.now();
            newEntry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));
            String commentString = comments.getText().toString();

            if (commentString != null && !commentString.isEmpty()) {
                newEntry.setComments(commentString);
            }

            if (buttonPages.isChecked()) {
               newEntry.setType(ActivityType.PAGES_READ);
               newEntry.setPagesRead(Integer.parseInt(textPages.getText().toString()));
            }
            else if (buttonFinished.isChecked()) {
               newEntry.setType(ActivityType.FINISHED);
               book.setStatus("Read");
               bookFirebaseHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                   @Override
                   public void onCallback(List<Book> bookList) {
                       Log.d(TAG, "Book status updated to Read");
                   }
               });
            }
            else if (buttonAbandoned.isChecked()) {
                newEntry.setType(ActivityType.ABANDONED);
                book.setStatus("DNF");
                bookFirebaseHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                    @Override
                    public void onCallback(List<Book> bookList) {
                        Log.d(TAG, "Book status updated to DNF");
                    }
                });
            }
            else {
               Toast.makeText(getContext(), "You need to choose an activity type!", Toast.LENGTH_SHORT).show();
            }
            newEntry.updateDescription();
            Log.d(TAG, "Entry created and populated. Title: " + newEntry.getBook().getTitle());
            Log.d(TAG, "Entry activity type: " + newEntry.getDescription());


            if (newEntry.getType() != null && !newEntry.displayActivityType().isEmpty()) {

                Log.d(TAG, "Entry not null, not empty");


                if (getActivity() instanceof MainActivity) {
                    Log.d(TAG, "opening Entry");
                    ((MainActivity)getActivity()).navigateToJournalEntry(newEntry);
                }
                // add entry to firebase database

                Log.d(TAG, "about to enter fbHelper.addEntry()");

                fbHelper.addEntry(newEntry);

                Log.d(TAG, "exited fbHelper.addEntry()");

            }

            if (getActivity() instanceof MainActivity) {
                Log.d(TAG, "opening Entry. Entry is null or empty");

                ((MainActivity)getActivity()).navigateToJournalEntry(newEntry);
            }

        });

    }



}