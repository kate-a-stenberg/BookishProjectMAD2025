package com.example.bookishproject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/*
This class represents a VPAdapter.
A View Pager Adapter handles operations within the view pager, including creating new fragments and swapping fragments.
It has a list of fragments and a list of their titles.
 */
public class VPAdapter extends FragmentStateAdapter {

    private final List<String> pageTitles = new ArrayList<>();
    private Fragment[] fragments;

    public VPAdapter(@NonNull MainActivity fragmentActivity, int numTabs) {
        super(fragmentActivity);
        fragments = new Fragment[numTabs];

        pageTitles.add("Welcome");
        pageTitles.add("Books");
        pageTitles.add("Recs");
        pageTitles.add("Journal");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (fragments[position] == null) {
            switch (position) {
                case 0: fragments[position] = new WelcomeFragment(); break;
                case 1: fragments[position] = new BooksFragment(); break;
                case 2: fragments[position] = new RecsFragment(); break;
                case 3: fragments[position] = new JournalFragment(); break;
            }
        }
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public void replaceFragment(int position, Fragment fragment) {
        fragments[position] = fragment;
        notifyItemChanged(position);
    }

    public CharSequence getPageTitle(int position) {
        return pageTitles.get(position);
    }

    public Fragment getFragment(int position) {
        return fragments[position];
    }
}
