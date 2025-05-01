package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookishproject.databinding.FragmentJournalHostBinding;


public class JournalHostFragment extends Fragment implements BackPressHandler, HostFragment {

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";

    private String currentFragmentTag = "books_list"; // Default starting fragment
    FragmentJournalHostBinding binding;

    public JournalHostFragment() {
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
        binding = FragmentJournalHostBinding.inflate(inflater, container, false);

        return  binding.getRoot();
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
                    .replace(R.id.journalFragmentContainerView, new JournalFragment(), "books_list")
                    .commitNow();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag);
    }

    public void navigateToJournalEntry(Entry entry) {
        JournalEntryFragment fragment = new JournalEntryFragment(entry);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("entry", entry);
        args.putBoolean("edit_mode", false); // Set to view mode
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToJournalEntry(Book book) {
        JournalEntryFragment fragment = new JournalEntryFragment(book);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        args.putBoolean("edit_mode", true); // Set to view mode
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToOpenBooks() {
        OpenBooksFragment fragment = new OpenBooksFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMyBooks() {
        MyBooksFragment fragment = new MyBooksFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToJournal() {
        JournalFragment fragment = new JournalFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToBook(Book book) {
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.journalFragmentContainerView);

        if (currentFragment instanceof BooksFragment) {
            // Let the BooksFragment save any important state
            ((BooksFragment) currentFragment).saveScrollPosition();
        }

        BookFragment fragment = new BookFragment(book);
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.journalFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onBackPressed() {
        // Return true if back was handled by this fragment
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            // Use post to ensure the fragment transaction completes first
            new android.os.Handler(getActivity().getMainLooper()).postDelayed(() -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getToolbarBuilder().updateToolbarForActiveFragment(this);
                }
            }, 100); // Short delay to ensure the transaction completes
            return true;
        }
        return false;
    }

    public String toString() {
        return "JournalHostFragment{" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.journalFragmentContainerView);
        Log.d("FragmentDebug", "JournalHostFragment.getCurrentVisibleFragment: " +
                (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        return fragment;
    }

}