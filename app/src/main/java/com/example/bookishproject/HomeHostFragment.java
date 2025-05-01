package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookishproject.databinding.FragmentHomeHostBinding;

/*
A HomeHostFragment is a host fragment for the Home section.
It manages all fragments in the Home section, including navigation, back navigation, and argument passing.
 */
public class HomeHostFragment extends HostFragment {

    public HomeHostFragment() {
        // Required empty public constructor
    }

    @Override
    protected Fragment createInitialFragment() {
        return new WelcomeFragment();
    }

    @Override
    protected String getInitialFragmentTag() {
        return "home";
    }

}