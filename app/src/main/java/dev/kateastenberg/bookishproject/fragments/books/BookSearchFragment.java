package dev.kateastenberg.bookishproject.fragments.books;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dev.kateastenberg.bookishproject.databinding.FragmentBookSearchBinding;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This is a class for a BookSearchFragment.
A BookSearchFragment allows the user to enter information to create a query to search
either the universal books collection or through an API (if admin user)
It has view binding and layout fields and elements.
 */
public class BookSearchFragment extends Fragment {

    private static final String KEY_SEARCH_TITLE = "search_title";
    private static final String KEY_SEARCH_AUTHOR = "search_author";

    private String currentSearchTitle = "";
    private String currentSearchAuthor = "";
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private EditText inputTitle, inputAuthor;
    private Button buttonSearch, buttonApiSearch;
    private Boolean isAdminUser = false;

    /*
    empty constructor
     */
    public BookSearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentBookSearchBinding binding = FragmentBookSearchBinding.inflate(inflater, container, false);

        // set variables for layout fields and elements
        inputTitle = binding.textTitleSearch;
        inputAuthor = binding.textAuthorSearch;
        buttonSearch = binding.buttonBookSearch;
        buttonApiSearch = binding.buttonAPISearch;

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bvm = new ViewModelProvider(this).get(BooksViewModel.class);

        observeViewModel();

        processIntent(new BooksIntent.AdminCheck());

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

        // the search button will search
        buttonSearch.setOnClickListener(v -> processIntent(new BooksIntent.GoSearch(currentSearchTitle, currentSearchAuthor)));

        if (!isAdminUser) {
            buttonApiSearch.setVisibility(View.GONE);
        }
        else {
            buttonApiSearch.setOnClickListener(v -> processIntent(new BooksIntent.GoApiSearch(currentSearchTitle, currentSearchAuthor)));
        }

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

    public void observeViewModel() {
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

        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof BooksIntent.GoSearch) {
                if (getParentFragment() instanceof BooksHostFragment) {
                    ((BooksHostFragment)getParentFragment()).navigateToBookResults(currentSearchTitle, currentSearchAuthor, false);
                }
            }
            else if (intent instanceof BooksIntent.GoApiSearch) {
                if (getParentFragment() instanceof BooksHostFragment) {
                    ((BooksHostFragment)getParentFragment()).navigateToBookResults(currentSearchTitle, currentSearchAuthor, true);
                }
            }
            else if (intent instanceof BooksIntent.AdminCheck) {
                bvm.handleIntent(intent);
            }
        });

    }

    private void processIntent(BooksIntent intent) {
        intentSubject.onNext(intent);
    }

}