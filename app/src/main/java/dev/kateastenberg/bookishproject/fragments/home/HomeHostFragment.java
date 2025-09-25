package dev.kateastenberg.bookishproject.fragments.home;

import androidx.fragment.app.Fragment;

import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;

/*
A HomeHostFragment is a host fragment for the Home section.
It manages all fragments in the Home section, including navigation, back navigation, and argument passing.
 */
public class HomeHostFragment extends HostFragment {

    public HomeHostFragment() {
        // Required empty public constructor
    }

    /*
    Method to navigate to AccountFragment
     */
    public void navigateToAccount() {
        AccountFragment fragment = new AccountFragment();
        Fragment currentFragment = getCurrentVisibleFragment();

        currentFragment.setExitTransition(createFadeIn());
        currentFragment.setReenterTransition(createFadeOut());

        fragment.setEnterTransition(createSlideInBottom());
        fragment.setReturnTransition(createSlideOutBottom());

        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to AboutFragment
     */
    public void navigateToAbout() {
        Fragment currentFragment = getCurrentVisibleFragment();

        AboutFragment fragment = new AboutFragment();

        if (currentFragment instanceof WelcomeFragment) {
            currentFragment.setExitTransition(createFadeIn());
            currentFragment.setReenterTransition(createFadeOut());
        }

        fragment.setEnterTransition(createSlideInBottom());
        fragment.setReturnTransition(createSlideOutBottom());

        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
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