package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookishproject.databinding.FragmentWelcomeBinding;

/*
This class represents the Welcome Fragment.
This is the landing or information page when someone opens the app.
It's basically just text and instructions.
 */
public class WelcomeFragment extends Fragment {

    FragmentWelcomeBinding binding;

    public WelcomeFragment() {
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
        binding = FragmentWelcomeBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }
}