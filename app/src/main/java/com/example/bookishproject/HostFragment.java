package com.example.bookishproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookishproject.databinding.FragmentHostBinding;

abstract public class HostFragment extends Fragment implements BackPressHandler {

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";
    private FragmentHostBinding binding;
    private String currentFragmentTag;

    public HostFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // if we have a saved instance state
        if (savedInstanceState != null) {
            // The FragmentManager will automatically restore the fragments in the back stack
            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT);
        } else {
            // Add initial fragment BooksFragment
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, createInitialFragment(), getInitialFragmentTag())
                    .commitNow();
        }
    }

    protected abstract Fragment createInitialFragment();
    protected abstract String getInitialFragmentTag();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag);
    }

    public Fragment getCurrentVisibleFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragmentContainerView);
        return fragment;
    }

    public boolean onBackPressed() {
        // if there is something on the back stack
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            // pop the current fragment off the back stack
            getChildFragmentManager().popBackStack();

            // Use post to ensure the fragment transaction completes first
            new android.os.Handler(getActivity().getMainLooper()).postDelayed(() -> {
                if (getActivity() instanceof MainActivity) {
                    // update toolbar for the active fragment
                    ((MainActivity) getActivity()).setToolbar(this);
                }
            }, 100); // Short delay to ensure the transaction completes
            // return true if back was handled
            return true;
        }
        // if there was nothing on the back stack, return false
        return false;
    }

}