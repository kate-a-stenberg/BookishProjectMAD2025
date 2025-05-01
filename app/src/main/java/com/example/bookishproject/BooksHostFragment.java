package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookishproject.databinding.FragmentBooksHostBinding;

public class BooksHostFragment extends Fragment implements BackPressHandler, HostFragment {

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";

    private String currentFragmentTag = "books_list"; // Default starting fragment
    FragmentBooksHostBinding binding;

    public BooksHostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBooksHostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            // The FragmentManager will automatically restore the fragments in the back stack
            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT);
        } else {
            // Add initial fragment
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.booksFragmentContainerView, new BooksFragment(), "books_list")
                    .commitNow();
        }
    }

    public void navigateToBook(Book book) {
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.booksFragmentContainerView);

        if (currentFragment instanceof BooksFragment) {
            // Let the BooksFragment save any important state
            ((BooksFragment) currentFragment).saveScrollPosition();
        }

        BookFragment fragment = new BookFragment(book);
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.booksFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToBookResults(String query) {
        BookResultsFragment fragment = BookResultsFragment.newInstance(query);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.booksFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToBookSearch() {
        BookSearchFragment fragment = new BookSearchFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.booksFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToBooks() {
        BooksFragment fragment = new BooksFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.booksFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onBackPressed() {
        Log.d("FragmentDebug", "BooksHostFragment: onBackPressed");
        // Return true if back was handled by this fragment
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            // Use post to ensure the fragment transaction completes first
            new android.os.Handler(getActivity().getMainLooper()).postDelayed(() -> {
                if (getActivity() instanceof MainActivity) {
                    Log.d("FragmentDebug", "BooksHostFragment onBackPressed: if getActivity() instanceof MainActivity yes");
                    ((MainActivity) getActivity()).getToolbarBuilder().updateToolbarForActiveFragment(this);
                    Log.d("FragmentDebug", "BooksHostFragment onBackPressed: called toolbarBuilder's updateToolbarForActiveFragment()");
                }
            }, 100); // Short delay to ensure the transaction completes
            Log.d("FragmentDebug", "BooksHostFragment onBackPressed: returning true");
            return true;
        }
        Log.d("FragmentDebug", "BooksHostFragment onBackPressed: returning false");
        return false;
    }

    public String toString() {
        return "BooksHostFragment{" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.booksFragmentContainerView);
        Log.d("FragmentDebug", "BooksHostFragment.getCurrentVisibleFragment: " +
                (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        return fragment;
    }

}