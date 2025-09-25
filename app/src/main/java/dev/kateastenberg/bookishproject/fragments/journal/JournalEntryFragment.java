package dev.kateastenberg.bookishproject.fragments.journal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import dev.kateastenberg.bookishproject.intents.ReadingSessionIntent;
import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.models.Date;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.helpers.firebase.JournalFirebaseHelper;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.FragmentJournalEntryBinding;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.ReadingSessionViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a journal entry
A journal entry has two modes: editable and view-only
A view-only journal entry is created when the user long taps on an existing journal entry in the Journal fragment.
An editable journal entry is created when the user long taps on a book in OpenBooks.
A JournalEntry will always have a book associated with it and an entry.
If accessed from an existing journal entry, the book and entry are both already provided.
If accessed from an OpenBook, the book is provided and a new entry is created.
It has view binding, layout fields and elements, an Entry that it's based on.
It also has a static final variable
 */
public class JournalEntryFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Entry whose information to populate its fields with
    // it receives this from JournalFragment via MainActivity
    private static final String ARG_ENTRY = "entry";

    private FragmentJournalEntryBinding binding;
    private ImageView cover;
    private TextView title, series, author, dateInput, commentsInput, editMessage, pagesReadInput, pagesReadLabel;
    private TextInputLayout pagesReadLayout;
    private Button bookDetails;
    private CardView editModeIndicator;
    private Spinner activitySpinner;
    private Button edit, submit;
    private Entry entry;
    private UserBook userBook;
    private Book book;
    private ReadingSessionViewModel rvm;
    private PublishSubject<ReadingSessionIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private boolean editMode;
    private JournalFirebaseHelper fbHelper;

//    // a constructor using an Entry object as a parameter / information source
//    public JournalEntryFragment(Entry entry) {
//        this.entry = entry;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if there are arguments
        if (getArguments() != null) {
            // figure out if we should be in edit mode
            this.editMode = getArguments().getBoolean("edit_mode", false);
            this.entry = getArguments().getParcelable("entry");
            this.userBook = entry.getUserBook();
            this.book = userBook.getBook();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalEntryBinding.inflate(inflater, container, false);

        fbHelper = new JournalFirebaseHelper();

        // set all variables for layout elements

        // CONSTANT FIELDS

        cover = binding.imageCover;
        title = binding.entryBookTitle;
        series = binding.textSeries;
        author = binding.entryBookAuthor;
        pagesReadLabel = binding.pagesReadLabel;
        bookDetails = binding.bookDetails;

        // EDITABLE FIELDS

        editModeIndicator = binding.editModeIndicator;
        editMessage = binding.editMessage;

        dateInput = binding.dateInput;

        activitySpinner = binding.activitySpinner;

        pagesReadInput = binding.pagesReadInput;
        pagesReadLayout = binding.pagesReadLayout;

        commentsInput = binding.commentsInput;

        edit = binding.journalEdit;
        submit = binding.journalSave;

        String transitionName = null;
        if (getArguments() != null) {
            transitionName = getArguments().getString("transition_name");
        }
        if (transitionName != null) {
            cover.setTransitionName(transitionName);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvm = new ViewModelProvider(this).get(ReadingSessionViewModel.class);

        observeViewModel();

        if (entry != null) {

            // set all constant fields with data from the Entry

            title.setText(book.getTitle());
            author.setText(book.getAuthor());
            if (book.getSeries() != null
                    && !book.getSeries().isEmpty()) {
                series.setText(book.getSeries());
                if (book.getNumber() != null
                        && book.getNumber() > 0) {
                    series.append(" #" + book.getNumber().toString());
                }
            }
            else {
                series.setVisibility(View.GONE);
            }
            if (book.getCoverUrl() != null
                    && !book.getCoverUrl().isEmpty()) {
                Glide.with(cover.getContext())
                        .load(book.getCoverUrl())
                        .placeholder(R.drawable.book_cover_background)
                        .error(R.drawable.book_cover_background)
                        .into(cover);
            }
            else {
                cover.setImageResource(book.getCover());
            }

            // create activity spinner adapter and set to activity spinner
            if (getContext() != null) {
                ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter
                        .createFromResource(getContext(), R.array.entry_array, R.layout.spinner_item);
                activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_journal);
                activitySpinner.setAdapter(activityAdapter);
                if (entry.getType() != null && !entry.getType().isEmpty()) {
                    for (int i = 0; i < activityAdapter.getCount(); i++) {
                        if (String.valueOf(activityAdapter.getItem(i)).equals(entry.getType())) {
                            activitySpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            // if we're in editMode
            if (editMode) {
                // go into editable based on the book
                processIntent(new ReadingSessionIntent.SetEditable());
            } else {
                // otherwise, go into view-only mode with the existing entry
                processIntent(new ReadingSessionIntent.SetViewOnly());
            }

        }

        // set operations for the submit button
        // this will be to submit the new entry. this means updating the entry's
        submit.setOnClickListener(v -> {

            // get all user's input and set them to the entry's attributes

            // entry type
            if (!activitySpinner.getSelectedItem().toString().equals("Select activity...")) {
                entry.setType(activitySpinner.getSelectedItem().toString());
            }
            // possibly get pages read
            if (entry.getType().equals("Pages read")) {
                if (pagesReadInput.getText() != null && !pagesReadInput.getText().toString().isEmpty()) {
                    entry.setPagesRead(Integer.parseInt(pagesReadInput.getText().toString()));
                    entry.updateDescription();
                }
            }
            // comments
            if (commentsInput.getText() != null && !commentsInput.getText().toString().isEmpty()) {
                entry.setComments(commentsInput.getText().toString());
            }

            // timestamp
            long time = System.currentTimeMillis();
            entry.setTimestamp(time);

            // if the user didn't enter an activity type
            if (activitySpinner.getSelectedItem().toString().equals("Started")) {
                processIntent(new ReadingSessionIntent.OpenBook(entry));
                processIntent(new ReadingSessionIntent.SetViewOnly());
            }
            else if (activitySpinner.getSelectedItem().toString().equals("Pages read")) {
                processIntent(new ReadingSessionIntent.UpdateReading(entry));
                processIntent(new ReadingSessionIntent.SetViewOnly());
            }
            else if (activitySpinner.getSelectedItem().toString().equals("Finished")) {
                processIntent (new ReadingSessionIntent.CloseBook(entry));
                processIntent(new ReadingSessionIntent.SetViewOnly());
            }
            else if (activitySpinner.getSelectedItem().toString().equals("Abandoned")) {
                processIntent(new ReadingSessionIntent.AbandonBook(entry));
                processIntent(new ReadingSessionIntent.SetViewOnly());
            }
            else {
                Toast.makeText(getContext(), "Please enter a valid activity type", Toast.LENGTH_SHORT).show();
            }

        });

        // set operations for book detail button
        bookDetails.setOnClickListener(v -> {
            if (getParentFragment() instanceof JournalHostFragment) {
                // open a book fragment for the book
                ((JournalHostFragment) getParentFragment()).navigateToBook(this.userBook, binding.imageCover, "book_transition");
            }
        });

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
    Method to enter editing mode for this Journal Entry
    Currently only works when creating a new entry
    Currently user may not edit existing journal entries
     */
    private void setEditable() {

        // get today's date and give it to the entry
        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));

        // set elements to editable, except for date

        setSpinnerEditable(activitySpinner, true);
        setTextEditable(commentsInput, true);
        setTextEditable(pagesReadInput, true);
        edit.setVisibility(View.GONE);
        editModeIndicator.setVisibility(View.VISIBLE);
        editMessage.setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);

        // set date textview to the date
        dateInput.setText(entry.getDate().displayDate());

        // set activity spinner listener
        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  String selectedActivity = parent.getItemAtPosition(position).toString();

                  // if selected activity is "pages read"
                  if ("Pages read".equals(selectedActivity)) {
                      // set editable pages read elements to visible
                      pagesReadLabel.setVisibility(View.VISIBLE);
                      pagesReadLayout.setVisibility(View.VISIBLE);
                      pagesReadInput.setVisibility(View.VISIBLE);
                  } else {
                      // otherwise set them to gone
                      pagesReadLabel.setVisibility(View.GONE);
                      pagesReadLayout.setVisibility(View.GONE);
                      pagesReadInput.setVisibility(View.GONE);
                  }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {
                  // Hide pagesRead fields by default
                  pagesReadLabel.setVisibility(View.GONE);
                  pagesReadLayout.setVisibility(View.GONE);
                  pagesReadInput.setVisibility(View.GONE);
              }
        });

    }

    /*
    Method to set fragment to view-only mode
     */
    private void setViewOnly() {

        editModeIndicator.setVisibility(View.GONE);
        editMessage.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);

        setSpinnerEditable(activitySpinner, false);
        setTextEditable(pagesReadInput, false);
        setTextEditable(commentsInput, false);

        pagesReadLabel.setVisibility(View.GONE);
        pagesReadLayout.setVisibility(View.GONE);
        pagesReadInput.setVisibility(View.GONE);

        // set text views based on entry's attributes

        // date
        dateInput.setText(entry.getDate().displayDate());

        // activity spinner
        if (getContext() != null) {
            ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.entry_array, R.layout.spinner_item);
            activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_journal);
            activitySpinner.setAdapter(activityAdapter);
            if (entry.getType() != null && !entry.getType().isEmpty()) {
                for (int i = 0; i < activityAdapter.getCount(); i++) {
                    if (String.valueOf(activityAdapter.getItem(i)).equals(entry.getType())) {
                        activitySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }

        if (activitySpinner.getSelectedItem().equals("Pages read")) {
            pagesReadLabel.setVisibility(View.VISIBLE);
            pagesReadLayout.setVisibility(View.VISIBLE);
            pagesReadInput.setVisibility(View.VISIBLE);
        }

        // pages read
        if (entry.getPagesRead() > 0) {
            pagesReadInput.setText(String.valueOf(entry.getPagesRead()));
        }

        // comments
        commentsInput.setText(entry.getComments());

    }

    /*
    Method to set text input fields editable or not
     */
    private void setTextEditable(TextView text, boolean value) {
        text.setFocusable(value);
        text.setFocusableInTouchMode(value);
        text.setCursorVisible(value);
    }

    /*
    Method to set spinners editable or not
     */
    private void setSpinnerEditable(Spinner spinner, boolean value) {
        spinner.setFocusable(value);
        spinner.setFocusableInTouchMode(value);
        spinner.setEnabled(value);
    }

    private void observeViewModel() {

        intentDisposable = intentSubject.subscribe(intent -> {
           if (intent instanceof ReadingSessionIntent.SetViewOnly) {
               setViewOnly();
           }
           else if (intent instanceof ReadingSessionIntent.SetEditable) {
               setEditable();
           }
           else {
               rvm.handleIntent(intent);
           }
        });
    }

    private void processIntent(ReadingSessionIntent intent) {
        intentSubject.onNext(intent);
    }

}
