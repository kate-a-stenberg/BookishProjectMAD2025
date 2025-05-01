package com.example.bookishproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentJournalEntryBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

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
    private TextView title, series, author, entryHeading, dateInput, dateInputEditable, commentsInput, commentsInputEditable, editMessage, pagesReadInput, pagesReadInputEditable, pagesReadLabel;
    private Button bookDetails;
    private CardView editModeIndicator, activityCard, activityCardEditable;
    private ScrollView scrollView;
    private TextInputLayout dateLayout, dateLayoutEditable, pagesReadLayout, pagesReadLayoutEditable, commentsLayout, commentsLayoutEditable;
    private Spinner activitySpinner, activitySpinnerEditable;
    private Button edit, submit;
    private Entry entry;
    private Book book;
    private boolean editMode;
    private JournalFirebaseHelper fbHelper;

    // a constructor using an Entry object as a parameter / information source
    public JournalEntryFragment(Entry entry) {
        this.entry = entry;
    }

    public JournalEntryFragment(Book book) {
        this.book = book;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if there are arguments
        if (getArguments() != null) {
            // figure out if we should be in edit mode
            this.editMode = getArguments().getBoolean("edit_mode", false);

            // if we should be in edit mode
            if (editMode) {
                // Coming from OpenBooksFragment (new entry)
                // get the book from the packaged arguments
                Book book = getArguments().getParcelable("book");
                if (book != null) {
                    // Create a new empty entry with just the book info
                    entry = new Entry(book);
                    // Will call setEditable() in onViewCreated
                }
            } else {
                // Coming from JournalFragment (view existing entry)
                // get the entry from the packaged arguments
                entry = getArguments().getParcelable("entry");
                // Will call setViewOnly() in onViewCreated
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalEntryBinding.inflate(inflater, container, false);

        fbHelper = new JournalFirebaseHelper();

        // set all variables for layout elements

        // CONSTANT FIELDS

        cover = binding.imageCover;
        title = binding.entryBookTitle;
        series = binding.textSeries;
        author = binding.entryBookAuthor;
        entryHeading = binding.entryHeading;
        pagesReadLabel = binding.pagesReadLabel;
        bookDetails = binding.bookDetails;
        scrollView = binding.entryScrollView;

        // EDITABLE FIELDS

        editModeIndicator = binding.editModeIndicator;
        editMessage = binding.editMessage;

        dateInput = binding.dateInput;
        dateLayout = binding.dateLayout;
        dateInputEditable = binding.dateInputEditable;
        dateLayoutEditable = binding.dateLayoutEditable;

        activityCard = binding.activityCard;
        activitySpinner = binding.activitySpinner;
        activityCardEditable = binding.activityCardEditable;
        activitySpinnerEditable = binding.activitySpinnerEditable;

        pagesReadInput = binding.pagesReadInput;
        pagesReadLayout = binding.pagesReadLayout;
        pagesReadInputEditable = binding.pagesReadInputEditable;
        pagesReadLayoutEditable = binding.pagesReadLayoutEditable;

        commentsInput = binding.commentsInput;
        commentsLayout = binding.commentsLayout;
        commentsInputEditable = binding.commentsInputEditable;
        commentsLayoutEditable = binding.commentsLayoutEditable;

        edit = binding.journalEdit;
        submit = binding.journalSave;


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (entry != null) {

            // set all constant fields with data from the Entry
            title.setText(entry.getBook().getTitle());
            author.setText(entry.getBook().getAuthor());
            if (entry.getBook().getSeries() != null && !entry.getBook().getSeries().isEmpty()) {
                series.setText(entry.getBook().getSeries());
                if (entry.getBook().getNumber() != null && entry.getBook().getNumber() > 0) {
                    series.append(" #" + entry.getBook().getNumber().toString());
                }
            }
            else {
                series.setVisibility(View.GONE);
            }
            if (entry.getBook().getCoverUrl() != null && !entry.getBook().getCoverUrl().isEmpty()) {
                Glide.with(cover.getContext()).load(entry.getBook().getCoverUrl()).placeholder(R.drawable.book_cover_background).error(R.drawable.book_cover_background).into(cover);
            }
            else {
                cover.setImageResource(entry.getBook().getCover());
            }

            // if we're in editMode
            if (editMode) {
                // go into editable based on the book
                setEditable(entry.getBook());
            } else {
                // otherwise, go into view-only mode with the existing entry
                setViewOnly(entry);
            }

        }

        // set operations for the submit button
        submit.setOnClickListener(v -> {

            // get all user's input and set them to the entry's attributes

            // entry type
            if (!activitySpinnerEditable.getSelectedItem().toString().equals("Select activity...")) {
                entry.setType(activitySpinnerEditable.getSelectedItem().toString());
            }
            // possibly get pages read
            if (entry.getType().equals("Pages read")) {
                if (pagesReadInputEditable.getText() != null && !pagesReadInputEditable.getText().toString().isEmpty()) {
                    entry.setPagesRead(Integer.parseInt(pagesReadInputEditable.getText().toString()));
                    entry.updateDescription();
                }
            }
            // comments
            if (commentsInputEditable.getText() != null && !commentsInputEditable.getText().toString().isEmpty()) {
                entry.setComments(commentsInputEditable.getText().toString());
            }

            // timestamp
            long time = System.currentTimeMillis();
            entry.setTimestamp(time);

            // if the user didn't enter an activity type
            if (activitySpinnerEditable.getSelectedItem().toString().equals("Select activity...")) {
                Toast.makeText(getContext(), "Please enter a valid activity type", Toast.LENGTH_SHORT).show();
            }
            else {
                // add the entry to the database
                fbHelper.addEntry(entry);
                // switch to view-only mode
                setViewOnly(entry);
            }

        });

        // set operations for book detail button
        bookDetails.setOnClickListener(v -> {
            if (getParentFragment() instanceof JournalHostFragment) {
                // open a book fragment for the book
                ((JournalHostFragment) getParentFragment()).navigateToBook(this.entry.getBook());
            }
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
    Method to enter editing mode for this Journal Entry
    Currently only works when creating a new entry
    Currently user may not edit existing journal entries
     */
    public void setEditable(Book book) {

        // if we don't have an entry, create a new one based on the book
        if (entry == null) {
            entry = new Entry(book);
        }

        // get today's date and give it to the entry
        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));

        entryHeading.setText("New Reading Activity");

        // set view-only mode elements to gone

        dateInputEditable.setVisibility(View.GONE);
        dateLayoutEditable.setVisibility(View.GONE);
        activitySpinner.setVisibility(View.GONE);
        activityCard.setVisibility(View.GONE);
        pagesReadLabel.setVisibility(View.GONE);
        pagesReadInput.setVisibility(View.GONE);
        pagesReadLayout.setVisibility(View.GONE);
        pagesReadInputEditable.setVisibility(View.GONE);
        pagesReadLayoutEditable.setVisibility(View.GONE);
        commentsInput.setVisibility(View.GONE);
        commentsLayout.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);

        // set editable elements to visible

        editModeIndicator.setVisibility(View.VISIBLE);
        editMessage.setVisibility(View.VISIBLE);
        activityCardEditable.setVisibility(View.VISIBLE);
        activitySpinnerEditable.setVisibility(View.VISIBLE);
        commentsLayoutEditable.setVisibility(View.VISIBLE);
        commentsInputEditable.setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);

        // entry date is automatically generated and is not editable
        dateLayout.setVisibility(View.VISIBLE);
        dateInput.setVisibility(View.VISIBLE);

        // set date textview to the date
        dateInput.setText(entry.getDate().displayDate());

        // create activity spinner adapter and set to activity spinner
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.entry_array, R.layout.spinner_item);
        activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_journal);
        activitySpinnerEditable.setAdapter(activityAdapter);

        // set activity spinner listener
        activitySpinnerEditable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  String selectedActivity = parent.getItemAtPosition(position).toString();

                  // if selected activity is "pages read"
                  if ("Pages read".equals(selectedActivity)) {
                      // set editable pages read elements to visible
                      pagesReadLabel.setVisibility(View.VISIBLE);
                      pagesReadInputEditable.setVisibility(View.VISIBLE);
                      pagesReadLayoutEditable.setVisibility(View.VISIBLE);
                  } else {
                      // otherwise set them to gone
                      pagesReadLabel.setVisibility(View.GONE);
                      pagesReadInputEditable.setVisibility(View.GONE);
                      pagesReadLayoutEditable.setVisibility(View.GONE);
                  }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {
                  // Hide pagesRead fields by default
                  pagesReadLabel.setVisibility(View.GONE);
                  pagesReadInputEditable.setVisibility(View.GONE);
                  pagesReadLayoutEditable.setVisibility(View.GONE);
              }
        });

    }

    /*
    Method to set fragment to view-only mode
     */
    public void setViewOnly(Entry entry) {

        // set editable fields to gone

        editModeIndicator.setVisibility(View.GONE);
        editMessage.setVisibility(View.GONE);
        dateInputEditable.setVisibility(View.GONE);
        dateLayoutEditable.setVisibility(View.GONE);
        activitySpinnerEditable.setVisibility(View.GONE);
        activityCardEditable.setVisibility(View.GONE);

        entryHeading.setText("Reading Activity Details");
        pagesReadLabel.setVisibility(View.GONE);
        pagesReadInputEditable.setVisibility(View.GONE);
        pagesReadLayoutEditable.setVisibility(View.GONE);
        pagesReadLayout.setVisibility(View.GONE);
        pagesReadInput.setVisibility(View.GONE);
        commentsInputEditable.setVisibility(View.GONE);
        commentsLayoutEditable.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);

        // set view-only elements to visible

        dateLayout.setVisibility(View.VISIBLE);
        dateInput.setVisibility(View.VISIBLE);
        activityCard.setVisibility(View.VISIBLE);
        activitySpinner.setVisibility(View.VISIBLE);
        if (activitySpinner.getSelectedItem().equals("Pages read")) {
            pagesReadLabel.setVisibility(View.VISIBLE);
            pagesReadLayout.setVisibility(View.VISIBLE);
            pagesReadInput.setVisibility(View.VISIBLE);

            pagesReadInput.setText(String.valueOf(entry.getPagesRead()));
        }
        commentsLayout.setVisibility(View.VISIBLE);
        commentsInput.setVisibility(View.VISIBLE);
        edit.setVisibility(View.VISIBLE);

        // set text views based on entry's attributes

        // date
        dateInput.setText(entry.getDate().displayDate());

        // activity spinner
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.entry_array, R.layout.spinner_item);
        activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_journal);
        activitySpinner.setAdapter(activityAdapter);
        if (entry.getType() != null && !entry.getType().isEmpty()) {
            for (int i = 0; i < activityAdapter.getCount(); i++) {
                if (activityAdapter.getItem(i).toString().equals(entry.getType())) {
                    activitySpinner.setSelection(i);
                    break;
                }
            }
        }
        // disable activity spinner
        activitySpinner.setEnabled(false);

        // comments
        commentsInput.setText(entry.getComments());

        // disable edit button (not currently allowed)
        edit.setEnabled(false);

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
