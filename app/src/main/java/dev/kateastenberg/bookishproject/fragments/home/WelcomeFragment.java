package dev.kateastenberg.bookishproject.fragments.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import dev.kateastenberg.bookishproject.databinding.FragmentWelcomeBinding;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;

/*
This class represents the Welcome Fragment.
This is the landing or information page when someone opens the app.
It's basically just text and instructions.
 */
public class WelcomeFragment extends Fragment {

    FragmentWelcomeBinding binding;
    private Button accountButton, aboutButton;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(getLayoutInflater(), container, false);

        accountButton = binding.accountButton;
        aboutButton = binding.aboutButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accountButton.setOnClickListener(v -> {
            if (getParentFragment() instanceof HomeHostFragment) {
                ((HomeHostFragment) getParentFragment()).navigateToAccount();
            }
        });
        aboutButton.setOnClickListener(v -> {
            if (getParentFragment() instanceof HomeHostFragment) {
                ((HomeHostFragment) getParentFragment()).navigateToAbout();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) getParentFragment());
        }
    }

}