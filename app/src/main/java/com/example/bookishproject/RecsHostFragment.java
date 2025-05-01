package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import com.example.bookishproject.databinding.FragmentRecsHostBinding;

public class RecsHostFragment extends Fragment implements BackPressHandler, HostFragment {

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";

    private String currentFragmentTag = "recs"; // Default starting fragment
    FragmentRecsHostBinding binding;

    public RecsHostFragment() {
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
        binding = FragmentRecsHostBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            // Load the default "landing" fragment for this section
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.recsFragmentContainerView, new RecsFragment())
                    .commit();
        }

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
                    .replace(R.id.recsFragmentContainerView, new RecsFragment(), "recs")
                    .commitNow();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag);
    }

    public void navigateToRecs() {
        RecsFragment fragment = new RecsFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.recsFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToHabits() {}

    public void navigateToPrefs() {}

    public void navigateToMatchSearch() {
        MatchSearchFragment fragment = new MatchSearchFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.recsFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchOptions(List<Book> books) {
        MatchOptionsFragment fragment = new MatchOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("BOOK_RESULTS", new ArrayList<>(books));
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.recsFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchResults(List<Book> matchingBooks) {
        MatchResultsFragment fragment = new MatchResultsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("MATCHING_BOOKS", new ArrayList<>(matchingBooks));
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.recsFragmentContainerView, fragment)
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
        return "RecsHostFragment{" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.recsFragmentContainerView);
        Log.d("FragmentDebug", "RecsHostFragment.getCurrentVisibleFragment: " +
                (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        return fragment;
    }

}