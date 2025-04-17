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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentNewEntryBinding;

import java.time.LocalDate;
import java.util.List;

/*
This class represents a NewEntryFragment.
A New Journal Entry Fragment allows a user to enter information about a recent reading activity.
It uses view binding, layout elements, a Book, a JournalFirebaseHelper, and a BookFirebaseHelper.
 */
public class NewEntryFragment extends Fragment {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if there are arguments and one of those arguments is labeled "selected_book"
        if (getArguments() != null && getArguments().containsKey("selected_book")) {
            // then this fragment's book is whatever is in that parcel
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

        textTitle.setText(book.getTitle());
        textAuthor.setText(book.getAuthor());

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(coverImage.getContext()).load(book.getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(coverImage);
        }
        else {
            coverImage.setImageResource(book.getCover());
        }

        // back button will ask the MainActivity to go back to OpenBooksFragment
        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToOpenBooksFragment();
            }
        });

        // submit button will create a new Entry with the information entered by the user and store it to the database
        buttonSubmit.setOnClickListener(v -> {

            if (book == null) {
                Toast.makeText(getContext(), "No book selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // make a new Entry based on this book
            Entry newEntry = new Entry(book);
            // get today's date and set that as the Entry's date
            LocalDate today = LocalDate.now();
            newEntry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));
            // user's comments go to the Entry
            String commentString = comments.getText().toString();
            if (commentString != null && !commentString.isEmpty()) {
                newEntry.setComments(commentString);
            }

            // if the user checked "pages read"
            if (buttonPages.isChecked()) {
                // set the Entry type as PAGES_READ
                newEntry.setType(EntryType.PAGES_READ);
                //  and number of pages read
                newEntry.setPagesRead(Integer.parseInt(textPages.getText().toString()));
            }
            // if the user checked "finished"
            else if (buttonFinished.isChecked()) {
                // set the Entry type as FINISHED
                newEntry.setType(EntryType.FINISHED);
                // change the Book status to "Read"
                book.setStatus("Read");
                // ask the BookFirebaseHelper to update the book's information in firebase
                bookFirebaseHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                   @Override
                   public void onCallback(List<Book> bookList) {
                   }
                });
            }
            // if the user checked "Abandoned"
            else if (buttonAbandoned.isChecked()) {
                // set Entry type to ABANDONED
                newEntry.setType(EntryType.ABANDONED);
                // change Book status to "DNF"
                book.setStatus("DNF");
                // ask BookFirebaseHelper to change the book's info in the database
                bookFirebaseHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
                    @Override
                    public void onCallback(List<Book> bookList) {
                    }
                });
            }
            // if no activity options were checked
            else {
               Toast.makeText(getContext(), "You need to choose an activity type!", Toast.LENGTH_SHORT).show();
            }

            // now that the Entry has a type, have it update the description based on the type
            newEntry.updateDescription();

            // if we created a valid Entry
            if (newEntry.getType() != null && !newEntry.getDescription().isEmpty()) {

                if (getActivity() instanceof MainActivity) {
                    // move on to the view only journal entry
                    ((MainActivity)getActivity()).navigateToJournalEntry(newEntry);
                }

                // add entry to the database
                fbHelper.addEntry(newEntry);
            }

            // if we didn't create a valid entry
            if (getActivity() instanceof MainActivity) {
                // go back to the JournalFragment
                ((MainActivity)getActivity()).navigateToJournalFragment();
            }

        });

    }

}