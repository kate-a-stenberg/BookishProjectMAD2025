package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentRecsBinding;

/*
This class represents a RecsFragment.
A Recommendations Fragment gives users the choice about what kind of recommendations they want.
It uses view binding and layout buttons.
 */
public class RecsFragment extends Fragment implements ColorUpdatable {

    FragmentRecsBinding binding;
    private Button buttonMatch, buttonPrefs, buttonHabits;

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

        updateColors();

        // preferences button does nothing yet
        buttonPrefs.setOnClickListener(v -> {
            Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Match button goes to MatchSearchFragment
        buttonMatch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).getNavigator().navigateToMatchSearchFragment();
            }
        });

        // habits button does nothing yet
        buttonHabits.setOnClickListener(v -> {
            Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar(this);
            updateColors();
        }
    }

    @Override
    public void updateColors() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.applyThemeColors(binding.getRoot(), activity.getCurrentSection());

            binding.buttonRecsHabits.setBackgroundColor(activity.currentInterestColor);
            binding.buttonRecsMatch.setBackgroundColor(activity.currentInterestColor);
            binding.buttonRecsPreferences.setBackgroundColor(activity.currentInterestColor);
        }
    }
}