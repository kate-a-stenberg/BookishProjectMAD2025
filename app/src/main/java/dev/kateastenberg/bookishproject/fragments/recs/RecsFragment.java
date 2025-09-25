package dev.kateastenberg.bookishproject.fragments.recs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.databinding.FragmentRecsBinding;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;

/*
This class represents a RecsFragment.
A Recommendations Fragment gives users the choice about what kind of recommendations they want.
It uses view binding and layout buttons.
 */
public class RecsFragment extends Fragment {

    FragmentRecsBinding binding;
    private Button buttonMatch, buttonPrefs, buttonHabits;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRecsBinding.inflate(inflater, container, false);

        buttonPrefs = binding.buttonRecsPreferences;
        buttonMatch = binding.buttonRecsMatch;
        buttonHabits = binding.buttonRecsHabits;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // preferences button does nothing yet
        buttonPrefs.setOnClickListener(v -> Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show());

        // Match button goes to MatchSearchFragment
        buttonMatch.setOnClickListener(v -> {
            if (getParentFragment() instanceof RecsHostFragment) {
                ((RecsHostFragment)getParentFragment()).navigateToMatchSearch();
            }
        });

        // habits button does nothing yet
        buttonHabits.setOnClickListener(v -> Toast.makeText(getContext(), "This functionality coming soon!", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }
    }

}