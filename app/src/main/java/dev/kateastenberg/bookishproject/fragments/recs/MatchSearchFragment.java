package dev.kateastenberg.bookishproject.fragments.recs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.databinding.FragmentMatchSearchBinding;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a MatchSearchFragment.
A MatchSearchFragment is for the user to enter a book (by title and/or author) that they would like similar books to.
It has view binding and layout fields and elements.
 */
public class MatchSearchFragment extends Fragment {

    private static final String KEY_SEARCH_TITLE = "search_title";
    private static final String KEY_SEARCH_AUTHOR = "search_author";

    private String currentSearchTitle = "";
    private String currentSearchAuthor = "";
    private EditText inputTitle, inputAuthor;
    private Button buttonSearch;

    /*
    Required empty constructor
    */
    public MatchSearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentMatchSearchBinding binding = FragmentMatchSearchBinding.inflate(inflater, container, false);

        // assign layout element variables
        inputTitle = binding.textTitleSearch;
        inputAuthor = binding.textAuthorSearch;
        buttonSearch = binding.buttonBookSearch;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(KEY_SEARCH_TITLE)) {
            currentSearchTitle = getArguments().getString(KEY_SEARCH_TITLE, "");
        }
        if (getArguments() != null && getArguments().containsKey(KEY_SEARCH_AUTHOR)) {
            currentSearchAuthor = getArguments().getString(KEY_SEARCH_AUTHOR, "");
        }

        if (savedInstanceState != null) {
            currentSearchTitle = savedInstanceState.getString(KEY_SEARCH_TITLE, "");
            currentSearchAuthor = savedInstanceState.getString(KEY_SEARCH_AUTHOR, "");

        }

        // search button will perform internal search
        buttonSearch.setOnClickListener(v -> performInternalSearch());

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {

            MainActivity activity = (MainActivity)getActivity();
            activity.setToolbar((HostFragment)this.getParentFragment());

            if (!currentSearchTitle.isEmpty()) {
                inputTitle.setText(currentSearchTitle);
            }
            if (!currentSearchAuthor.isEmpty()) {
                inputAuthor.setText(currentSearchAuthor);
            }

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_TITLE, currentSearchTitle);
        outState.putString(KEY_SEARCH_AUTHOR, currentSearchAuthor);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentSearchTitle = savedInstanceState.getString(KEY_SEARCH_TITLE, "");
            currentSearchAuthor = savedInstanceState.getString(KEY_SEARCH_AUTHOR, "");

        }
    }

    /*
    Method to search the user's book collection (firebase database) for books matching search terms
    Will send resulting list to MainActivity to populate a MatchOptionsFragment
     */
    private void performInternalSearch() {

        String title = inputTitle.getText().toString().trim();
        String author = inputAuthor.getText().toString().trim();

        if ((title == null || title.isEmpty()) && (author == null || author.isEmpty())) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getParentFragment() instanceof RecsHostFragment) {
            // then ask the MainActivity to go to a BookResultsFragment using this query to populate its search
            ((RecsHostFragment) getParentFragment()).navigateToMatchOptions(title, author);
        }
    }

}