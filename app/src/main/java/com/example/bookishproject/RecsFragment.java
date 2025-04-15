package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentRecsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecsFragment extends Fragment {

    FragmentRecsBinding binding;
    private Button buttonMatch;
    private Button buttonPrefs;
    private Button buttonHabits;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRecsBinding.inflate(inflater, container, false);

        buttonPrefs = binding.buttonRecsPreferences;
        buttonMatch = binding.buttonRecsMatch;
        buttonHabits = binding.buttonRecsHabits;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonPrefs.setOnClickListener(v -> {
            Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        buttonMatch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToMatchSearchFragment();
            }
        });

        buttonHabits.setOnClickListener(v -> {
            Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

    }
}