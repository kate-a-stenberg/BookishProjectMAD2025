package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookishproject.databinding.FragmentHomeHostBinding;

public class HomeHostFragment extends Fragment implements HostFragment {

    FragmentHomeHostBinding binding;

    public HomeHostFragment() {
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
        binding = FragmentHomeHostBinding.inflate(inflater, container, false);

        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            // Add initial fragment with a tag
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.homeFragmentContainerView, new WelcomeFragment(), "welcome_tag")
                    .commitNow(); // Use commitNow to ensure it happens immediately
        }
    }

    public boolean onBackPressed() {
        // Return true if back was handled by this fragment
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "HomeHostFragment{" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        // First try to find by tag
        Fragment fragment = getChildFragmentManager().findFragmentByTag("welcome_tag");

        // If that fails, try by ID
        if (fragment == null) {
            fragment = getChildFragmentManager().findFragmentById(R.id.homeFragmentContainerView);
        }

        // If still null and not during initialization, create a default fragment
        if (fragment == null && isAdded()) {
            fragment = new WelcomeFragment();
        }

        Log.d("FragmentDebug", "HomeHostFragment.getCurrentVisibleFragment: " +
                (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        return fragment;
    }

}