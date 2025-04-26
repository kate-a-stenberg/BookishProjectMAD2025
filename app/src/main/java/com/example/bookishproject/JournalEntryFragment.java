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
A journal entry displays view-only information about an reading activity that a user entered.
It has view binding, layout fields and elements, an Entry that it's based on.
It also has a static final variable
 */
public class JournalEntryFragment extends Fragment implements ColorUpdatable {

    // this variable is the name of the Bundle that contains information on the Entry whose information to populate its fields with
    // it receives this from JournalFragment via MainActivity
    private static final String ARG_ENTRY = "entry";

    private FragmentJournalEntryBinding binding;
    private ImageView cover;
    private TextView title, series, author, entryHeading, dateInput, dateInputEditable, commentsInput, commentsInputEditable, editMessage, pagesReadInput, pagesReadInputEditable, pagesReadLabel;
    private CardView editModeIndicator, bookInfoCard, activityCard, activityCardEditable;
    private ScrollView scrollView;
    private FloatingActionButton bookDetails;
    private TextInputLayout dateLayout, dateLayoutEditable, pagesReadLayout, pagesReadLayoutEditable, commentsLayout, commentsLayoutEditable;
    private Spinner activitySpinner, activitySpinnerEditable;
    private Button edit, submit;
    private Entry entry;
    private Book book;
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
        if (getArguments() != null) {
            boolean editMode = getArguments().getBoolean("edit_mode", false);

            if (editMode) {
                // Coming from OpenBooksFragment (new entry)
                Book book = getArguments().getParcelable("book");
                if (book != null) {
                    // Create a new empty entry with just the book info
                    entry = new Entry(book);
                    // Will call setEditable() in onViewCreated
                }
            } else {
                // Coming from JournalFragment (view existing entry)
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

        bookInfoCard = binding.bookInfoCard;
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
        submit = binding.journalSubmit;


        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (entry != null) {

            // set all fields with data from the Entry
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

            boolean editMode = getArguments() != null && getArguments().getBoolean("edit_mode", false);
            if (editMode) {
                setEditable(entry.getBook());
            } else {
                setViewOnly(entry);
            }

        }

        submit.setOnClickListener(v -> {
            // TODO: save info to journal entry

            /*
            TODO: things to set
            book - set on onCreate()
            type - set here
            pages read - set here
            date - set in setEditable()
            description - set here
            comments - set here
            timestamp - set here
             */

            if (!activitySpinnerEditable.getSelectedItem().toString().equals("Select activity...")) {
                entry.setType(activitySpinnerEditable.getSelectedItem().toString());
            }
            if (entry.getType().equals("Pages read")) {
                if (pagesReadInputEditable.getText() != null && !pagesReadInputEditable.getText().toString().isEmpty()) {
                    entry.setPagesRead(Integer.parseInt(pagesReadInputEditable.getText().toString()));
                    entry.updateDescription();
                }
            }
            if (commentsInputEditable.getText() != null && !commentsInputEditable.getText().toString().isEmpty()) {
                entry.setComments(commentsInputEditable.getText().toString());
            }

            long time = System.currentTimeMillis();
            entry.setTimestamp(time);

            if (activitySpinnerEditable.getSelectedItem().toString().equals("Select activity...")) {
                Toast.makeText(getContext(), "Please enter a valid activity type", Toast.LENGTH_SHORT).show();
            }
            else {
                fbHelper.addEntry(entry);
                setViewOnly(entry);
            }

        });

        updateColors();

        setupKeyboardAdjustment(view);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setToolbar(this);
            updateColors();
        }
    }

    public void setEditable(Book book) {

        if (entry == null) {
            entry = new Entry(book);
        }

        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));

        entryHeading.setText("New Reading Activity");

        editModeIndicator.setVisibility(View.VISIBLE);
        editMessage.setVisibility(View.VISIBLE);

        dateInputEditable.setVisibility(View.GONE);
        dateLayoutEditable.setVisibility(View.GONE);
        dateLayout.setVisibility(View.VISIBLE);
        dateInput.setVisibility(View.VISIBLE);

        dateInput.setText(entry.getDate().displayDate());

        activitySpinner.setVisibility(View.GONE);
        activityCard.setVisibility(View.GONE);
        activityCardEditable.setVisibility(View.VISIBLE);
        activitySpinnerEditable.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.entry_array, R.layout.spinner_item);
        activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_journal);
        activitySpinnerEditable.setAdapter(activityAdapter);

        activitySpinnerEditable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  String selectedActivity = parent.getItemAtPosition(position).toString();

                  // Show pagesRead fields only for "Pages read" activity
                  if ("Pages read".equals(selectedActivity)) {
                      pagesReadLabel.setVisibility(View.VISIBLE);
                      pagesReadInputEditable.setVisibility(View.VISIBLE);
                      pagesReadLayoutEditable.setVisibility(View.VISIBLE);
                  } else {
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

        pagesReadLabel.setVisibility(View.GONE);
        pagesReadInput.setVisibility(View.GONE);
        pagesReadLayout.setVisibility(View.GONE);
        pagesReadInputEditable.setVisibility(View.GONE);
        pagesReadLayoutEditable.setVisibility(View.GONE);

        commentsInput.setVisibility(View.GONE);
        commentsLayout.setVisibility(View.GONE);
        commentsLayoutEditable.setVisibility(View.VISIBLE);
        commentsInputEditable.setVisibility(View.VISIBLE);

        edit.setVisibility(View.GONE);
        submit.setVisibility(View.VISIBLE);

    }

    public void setViewOnly(Entry entry) {

        entryHeading.setText("Reading Activity Details");

        editModeIndicator.setVisibility(View.GONE);
        editMessage.setVisibility(View.GONE);

        dateInputEditable.setVisibility(View.GONE);
        dateLayoutEditable.setVisibility(View.GONE);
        dateLayout.setVisibility(View.VISIBLE);
        dateInput.setVisibility(View.VISIBLE);

        dateInput.setText(entry.getDate().displayDate());

        activitySpinnerEditable.setVisibility(View.GONE);
        activityCardEditable.setVisibility(View.GONE);
        activityCard.setVisibility(View.VISIBLE);
        activitySpinner.setVisibility(View.VISIBLE);

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
        activitySpinner.setEnabled(false);

        pagesReadLabel.setVisibility(View.GONE);
        pagesReadInputEditable.setVisibility(View.GONE);
        pagesReadLayoutEditable.setVisibility(View.GONE);
        pagesReadLayout.setVisibility(View.GONE);
        pagesReadInput.setVisibility(View.GONE);

        if (activitySpinner.getSelectedItem().equals("Pages read")) {
            pagesReadLabel.setVisibility(View.VISIBLE);
            pagesReadLayout.setVisibility(View.VISIBLE);
            pagesReadInput.setVisibility(View.VISIBLE);

            pagesReadInput.setText(String.valueOf(entry.getPagesRead()));
        }

        commentsInputEditable.setVisibility(View.GONE);
        commentsLayoutEditable.setVisibility(View.GONE);
        commentsLayout.setVisibility(View.VISIBLE);
        commentsInput.setVisibility(View.VISIBLE);

        commentsInput.setText(entry.getComments());

        submit.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);
        edit.setBackgroundColor(getResources().getColor(R.color.my_theme_main_medium, null));
        edit.setEnabled(false);

    }

    @Override
    public void updateColors() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.applyThemeColors(binding.getRoot(), activity.getCurrentSection());

            editModeIndicator.setCardBackgroundColor(activity.currentInterestColor);
            editMessage.setTextColor(activity.currentBackgroundColor);
            binding.bookInfoCard.setCardBackgroundColor(activity.currentCardColor);
        }
    }

    // Add this to your Fragment or Activity
    private void setupKeyboardAdjustment(View rootView) {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keyboardHeight = screenHeight - r.bottom;

                if (keyboardHeight > screenHeight * 0.15) { // Keyboard is visible
                    // Find the currently focused view
                    View focused = getActivity().getCurrentFocus();
                    if (focused != null) {
                        // Scroll to the focused view
                        int[] location = new int[2];
                        focused.getLocationOnScreen(location);
                        scrollView.smoothScrollTo(0, location[1] - keyboardHeight);
                    }
                }
            }
        });
    }

}
