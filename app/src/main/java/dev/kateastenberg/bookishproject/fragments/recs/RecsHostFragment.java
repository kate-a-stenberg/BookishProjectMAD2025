package dev.kateastenberg.bookishproject.fragments.recs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.R;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;

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

    public void navigateToMatchSearch() {
        MatchSearchFragment fragment = new MatchSearchFragment();
        Fragment currentFragment = getCurrentVisibleFragment();

        currentFragment.setExitTransition(createFadeOut());
        currentFragment.setReenterTransition(createFadeIn());

        fragment.setEnterTransition(createFadeIn());
        fragment.setReturnTransition(createFadeOut());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchOptions(String title, String author) {

        Fragment currentFragment = getCurrentVisibleFragment();

        MatchOptionsFragment fragment = MatchOptionsFragment.newInstance(title, author);

        fragment.setEnterTransition(createSlideInRight());
        fragment.setReturnTransition(createSlideOutRight());

        currentFragment.setExitTransition(createSlideOutLeft());
        currentFragment.setReenterTransition(createSlideInLeft());

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToMatchResults(UserBook userBook) {
        MatchResultsFragment fragment = new MatchResultsFragment();
        Fragment currentFragment = getCurrentVisibleFragment();

        currentFragment.setExitTransition(createSlideOutLeft());
        currentFragment.setReenterTransition(createSlideInLeft());

        fragment.setEnterTransition(createSlideInRight());
        fragment.setReturnTransition(createSlideOutRight());

        Bundle args = new Bundle();
        args.putParcelable("USER_BOOK", userBook);
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

}