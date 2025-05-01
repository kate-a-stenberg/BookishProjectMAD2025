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

/*
This class represents a RecsHostFragment.
A RecsHostFragment hosts other fragments in the recs section.
It handles navigation and back navigation for these fragments, as well as passing arguments between them.
 */
public class RecsHostFragment extends HostFragment {

    public RecsHostFragment() {
        // Required empty public constructor
    }

    @Override
    protected Fragment createInitialFragment() {
        return new RecsFragment();
    }

    @Override
    protected String getInitialFragmentTag() {
        return "recs";
    }

    public void navigateToRecs() {
        RecsFragment fragment = new RecsFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToHabits() {}

    public void navigateToPrefs() {}

    public void navigateToMatchSearch() {
        MatchSearchFragment fragment = new MatchSearchFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchOptions(List<Book> books) {
        MatchOptionsFragment fragment = new MatchOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("BOOK_RESULTS", new ArrayList<>(books));
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchResults(List<Book> matchingBooks) {
        MatchResultsFragment fragment = new MatchResultsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("MATCHING_BOOKS", new ArrayList<>(matchingBooks));
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

}