package dev.kateastenberg.bookishproject.fragments.common;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Fade;
import androidx.transition.Slide;

import com.google.android.material.transition.MaterialContainerTransform;

import dev.kateastenberg.bookishproject.interfaces.BackPressHandler;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.FragmentHostBinding;

import dev.kateastenberg.bookishproject.activities.MainActivity;

/*
This class represents a HostFragment.
A Host Fragment manages all fragments in its section, including navigation and back navigation.
It must has transitions, life cycle methods and fragment getters.
 */
abstract public class HostFragment extends Fragment implements BackPressHandler {

    protected static final int TRANSITION_DURATION_SHORT = 300;
    protected static final int TRANSITION_DURATION_MEDIUM = 500;
    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";
    private String currentFragmentTag;

    public HostFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHostBinding binding = FragmentHostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag);
    }

    @Override
    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();

            getChildFragmentManager().executePendingTransactions();

            // Important: After popping, update toolbar with a slight delay
            // to ensure fragment transition is complete
            if (getActivity() instanceof MainActivity) {
                new android.os.Handler().postDelayed(() -> {
                    if (getActivity() != null) {
                        ((MainActivity) getActivity()).setToolbar(this);
                    }
                }, 100);
            }

            return true;
        }
        return false;
    }

    public Fragment getCurrentVisibleFragment() {
        return getChildFragmentManager().findFragmentById(R.id.fragmentContainerView);
    }

    protected abstract Fragment createInitialFragment();
    protected abstract String getInitialFragmentTag();

    // TRANSITIONS

    protected Fade createFadeIn() {
        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(TRANSITION_DURATION_SHORT);
        return fadeIn;
    }

    protected Fade createFadeOut() {
        Fade fadeIn = new Fade(Fade.OUT);
        fadeIn.setDuration(TRANSITION_DURATION_SHORT);
        return fadeIn;
    }

    protected Slide createSlideInRight() {
        Slide slideIn = new Slide(Gravity.END);
        slideIn.setDuration(TRANSITION_DURATION_MEDIUM);
        return slideIn;
    }

    protected Slide createSlideOutLeft() {
        Slide slideOut = new Slide(Gravity.START);
        slideOut.setDuration(TRANSITION_DURATION_SHORT);
        return slideOut;
    }

    protected Slide createSlideInLeft() {
        Slide slideIn = new Slide(Gravity.START);
        slideIn.setDuration(TRANSITION_DURATION_MEDIUM);
        return slideIn;
    }

    protected Slide createSlideOutRight() {
        Slide slideOut = new Slide(Gravity.END);
        slideOut.setDuration(TRANSITION_DURATION_SHORT);
        return slideOut;
    }

    protected Slide createSlideInBottom() {
        Slide slideUp = new Slide(Gravity.BOTTOM);
        slideUp.setDuration(TRANSITION_DURATION_SHORT);
        return slideUp;
    }

    protected Slide createSlideOutBottom() {
        Slide slideDown = new Slide(Gravity.BOTTOM);
        slideDown.setDuration(TRANSITION_DURATION_SHORT);
        return slideDown;
    }

    protected MaterialContainerTransform createContainerTransform() {
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.setDuration(TRANSITION_DURATION_MEDIUM);
        transform.setScrimColor(Color.TRANSPARENT);
        transform.setDrawingViewId(R.id.fragmentContainerView);
        int surfaceColor = ContextCompat.getColor(requireContext(), R.color.md_theme_surface);
        transform.setAllContainerColors(surfaceColor);
        return transform;
    }

}