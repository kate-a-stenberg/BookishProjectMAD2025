package com.example.bookishproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bookishproject.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/*
This class represents the app's MainActivity.
It has view binding, a tab layout, a view pager, a view pager adapter
 */
public class MainActivity extends AppCompatActivity implements AppToolbarProvider {

    private ActivityMainBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private VPAdapter adapter;
    private ImageButton backButton, helpButton, searchButton, addButton;
    private TextView searchInput;
    private ToolbarBuilder toolbarBuilder;
    private Navigator navigator;
    public int currentBackgroundColor, currentPrimaryColor, currentTextColor, currentCardColor, currentDarkColor, currentInterestColor;
    private int currentSection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupKeyboardVisibilityListener(); // Add this line

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // Initialize ViewPager2 and its adapter
        viewPager = binding.viewPager;
        adapter = new VPAdapter(this, 4);
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < 0.5 && position > -0.5) {
                    Fragment fragment = adapter.getFragment(viewPager.getCurrentItem());
                    if (fragment instanceof ColorUpdatable) {
                        ((ColorUpdatable) fragment).updateColors();
                    }
                }
            }
        });

        // Set up TabLayout with ViewPager2
        tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentSection = position;

                // Apply colors based on position
                int primaryColor, backgroundColor, textColor, tabIndicatorColor, darkColor, cardColor;

                switch (position) {
                    case 0: // Welcome
                        primaryColor = getResources().getColor(R.color.my_theme_main_medium, null);
                        backgroundColor = getResources().getColor(R.color.my_theme_main_super_light, null);
                        cardColor = getResources().getColor(R.color.my_theme_main_light, null);
                        textColor = getResources().getColor(R.color.my_theme_main_super_dark, null);
                        darkColor = getResources().getColor(R.color.my_theme_main_dark, null);
                        tabIndicatorColor = getResources().getColor(R.color.my_theme_main_medium, null);
                        binding.backButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.helpButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.searchButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.addButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.searchInput.setBackground(getResources().getDrawable(R.drawable.input_background_main, null));
                        break;
                    case 1: // Books
                        primaryColor = getResources().getColor(R.color.my_theme_books_medium, null);
                        backgroundColor = getResources().getColor(R.color.my_theme_books_background, null);
                        cardColor = getResources().getColor(R.color.my_theme_books_alt_background, null);
                        textColor = getResources().getColor(R.color.my_theme_books_text, null);
                        darkColor = getResources().getColor(R.color.my_theme_books_dark, null);
                        tabIndicatorColor = getResources().getColor(R.color.my_theme_books_interest, null);
                        binding.backButton.setBackground(getResources().getDrawable(R.drawable.button_background_books, null));
                        binding.helpButton.setBackground(getResources().getDrawable(R.drawable.button_background_books, null));
                        binding.searchButton.setBackground(getResources().getDrawable(R.drawable.button_background_books, null));
                        binding.addButton.setBackground(getResources().getDrawable(R.drawable.button_background_books, null));
                        binding.searchInput.setBackground(getResources().getDrawable(R.drawable.input_background_books, null));
                        break;
                    case 2: // Recs
                        primaryColor = getResources().getColor(R.color.my_theme_recs_medium, null);
                        backgroundColor = getResources().getColor(R.color.my_theme_recs_background, null);
                        cardColor = getResources().getColor(R.color.my_theme_recs_alt_background, null);
                        textColor = getResources().getColor(R.color.my_theme_recs_text, null);
                        darkColor = getResources().getColor(R.color.my_theme_recs_dark, null);
                        tabIndicatorColor = getResources().getColor(R.color.my_theme_recs_interest, null);
                        binding.backButton.setBackground(getResources().getDrawable(R.drawable.button_background_recs, null));
                        binding.helpButton.setBackground(getResources().getDrawable(R.drawable.button_background_recs, null));
                        binding.searchButton.setBackground(getResources().getDrawable(R.drawable.button_background_recs, null));
                        binding.addButton.setBackground(getResources().getDrawable(R.drawable.button_background_recs, null));
                        binding.searchInput.setBackground(getResources().getDrawable(R.drawable.input_background_recs, null));
                        break;
                    case 3: // Journal
                        primaryColor = getResources().getColor(R.color.my_theme_journal_medium, null);
                        backgroundColor = getResources().getColor(R.color.my_theme_journal_background, null);
                        cardColor = getResources().getColor(R.color.my_theme_journal_alt_background, null);
                        textColor = getResources().getColor(R.color.my_theme_journal_text, null);
                        darkColor = getResources().getColor(R.color.my_theme_journal_dark, null);
                        tabIndicatorColor = getResources().getColor(R.color.my_theme_journal_interest, null);
                        binding.backButton.setBackground(getResources().getDrawable(R.drawable.button_background_journal, null));
                        binding.helpButton.setBackground(getResources().getDrawable(R.drawable.button_background_journal, null));
                        binding.searchButton.setBackground(getResources().getDrawable(R.drawable.button_background_journal, null));
                        binding.addButton.setBackground(getResources().getDrawable(R.drawable.button_background_journal, null));
                        binding.searchInput.setBackground(getResources().getDrawable(R.drawable.input_background_journal, null));
                        break;
                    default:
                        primaryColor = getResources().getColor(R.color.my_theme_main_medium, null);
                        backgroundColor = getResources().getColor(R.color.my_theme_main_super_light, null);
                        cardColor = getResources().getColor(R.color.my_theme_main_light, null);
                        textColor = getResources().getColor(R.color.my_theme_main_super_dark, null);
                        darkColor = getResources().getColor(R.color.my_theme_main_dark, null);
                        tabIndicatorColor = getResources().getColor(R.color.my_theme_main_medium, null);
                        binding.backButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.helpButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.searchButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.addButton.setBackground(getResources().getDrawable(R.drawable.button_background_main, null));
                        binding.searchInput.setBackground(getResources().getDrawable(R.drawable.input_background_main, null));
                }

                // Apply UI color changes
                tabLayout.setSelectedTabIndicatorColor(primaryColor);
                tabLayout.setBackgroundColor(textColor);
                tabLayout.setTabTextColors(ColorStateList.valueOf(backgroundColor));
                binding.main.setBackgroundColor(getResources().getColor(R.color.md_theme_onPrimary, null));
                binding.materialToolbar.setBackgroundColor(textColor);
                getWindow().setStatusBarColor(textColor);
            }
        });

        backButton = binding.backButton;
        helpButton = binding.helpButton;
        searchButton = binding.searchButton;
        addButton = binding.addButton;
        searchInput = binding.searchInput;

        navigator = new Navigator(this, this);

        toolbarBuilder = new ToolbarBuilder(this);
        toolbarBuilder.setProvider(this);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    Helper method to get BooksFragment
     */
    public BooksFragment getBooksFragment() {
        return (BooksFragment) adapter.getFragment(1);
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public VPAdapter getVPAdapter() {
        return adapter;
    }

    @Override
    public Navigator getNavigator() {
        return this.navigator;
    }
    @Override
    public ImageButton getBackButton() {
        return backButton;
    }

    @Override
    public ImageButton getHelpButton() {
        return helpButton;
    }

    @Override
    public TextView getSearchInput() {
        return searchInput;
    }

    @Override
    public ImageButton getSearchButton() {
        return searchButton;
    }

    @Override
    public ImageButton getAddButton() {
        return addButton;
    }

    public int getCurrentSection() {
        return currentSection;
    }

    public void setToolbar(Fragment fragment) {
        toolbarBuilder.buildToolbar(fragment);
    }

    public void applyThemeColors(View rootView, int position) {
        // Get colors based on section
        int primaryColor, backgroundColor, cardColor, textColor, tabIndicatorColor, darkColor;

        switch (position) {
            case 0: // Welcome
                primaryColor = getResources().getColor(R.color.my_theme_main_dark, null);
                backgroundColor = getResources().getColor(R.color.my_theme_main_super_light, null);
                textColor = getResources().getColor(R.color.my_theme_main_super_dark, null);
                tabIndicatorColor = getResources().getColor(R.color.my_theme_main_medium, null);
                cardColor = getResources().getColor(R.color.my_theme_main_light, null);
                darkColor = getResources().getColor(R.color.my_theme_main_dark, null);

                break;
            case 1: // Books
                primaryColor = getResources().getColor(R.color.my_theme_books_medium, null);
                backgroundColor = getResources().getColor(R.color.my_theme_books_background, null);
                textColor = getResources().getColor(R.color.my_theme_books_text, null);
                tabIndicatorColor = getResources().getColor(R.color.my_theme_books_interest, null);
                cardColor = getResources().getColor(R.color.my_theme_books_alt_background, null);
                darkColor = getResources().getColor(R.color.my_theme_books_dark, null);
                break;
            case 2: // Recs
                primaryColor = getResources().getColor(R.color.my_theme_recs_dark, null);
                backgroundColor = getResources().getColor(R.color.my_theme_recs_background, null);
                textColor = getResources().getColor(R.color.my_theme_recs_text, null);
                tabIndicatorColor = getResources().getColor(R.color.my_theme_recs_interest, null);
                cardColor = getResources().getColor(R.color.my_theme_recs_alt_background, null);
                darkColor = getResources().getColor(R.color.my_theme_recs_dark, null);
                break;
            case 3: // Journal
                primaryColor = getResources().getColor(R.color.my_theme_journal_medium, null);
                backgroundColor = getResources().getColor(R.color.my_theme_journal_background, null);
                textColor = getResources().getColor(R.color.my_theme_journal_text, null);
                tabIndicatorColor = getResources().getColor(R.color.my_theme_journal_interest, null);
                cardColor = getResources().getColor(R.color.my_theme_journal_alt_background, null);
                darkColor = getResources().getColor(R.color.my_theme_journal_dark, null);
                break;
            default:
                primaryColor = getResources().getColor(R.color.my_theme_main_dark, null);
                backgroundColor = getResources().getColor(R.color.my_theme_main_super_light, null);
                textColor = getResources().getColor(R.color.my_theme_main_super_dark, null);
                tabIndicatorColor = getResources().getColor(R.color.my_theme_main_medium, null);
                cardColor = getResources().getColor(R.color.my_theme_main_light, null);
                darkColor = getResources().getColor(R.color.my_theme_books_dark, null);
        }

        // Apply background color to root view
        rootView.setBackgroundColor(getResources().getColor(R.color.md_theme_onPrimary, null));

        currentPrimaryColor = primaryColor;
        currentTextColor = textColor;
        currentBackgroundColor = backgroundColor;
        currentCardColor = cardColor;
        currentDarkColor = darkColor;
        currentInterestColor = tabIndicatorColor;

        // Apply to other common views as needed
    }

    private void setupKeyboardVisibilityListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean keyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int visibleHeight = r.height();

                boolean isKeyboardNowVisible = visibleHeight < screenHeight * 0.85;

                if (isKeyboardNowVisible != keyboardVisible) {
                    keyboardVisible = isKeyboardNowVisible;

                    // Simply request a layout update - Android's adjustResize will handle the rest
                    binding.viewPager.requestLayout();

                    // Make sure toolbar color is restored if keyboard is hidden
                    if (!keyboardVisible) {
                        binding.materialToolbar.setBackgroundColor(currentTextColor);
                    }
                }
            }
        });
    }

    // Helper method to check if a view is in the toolbar
    private boolean isViewInToolbar(View view) {
        return view == binding.searchInput ||
                binding.materialToolbar.findViewById(view.getId()) != null ||
                binding.linearLayout3.findViewById(view.getId()) != null ||
                binding.linearLayout4.findViewById(view.getId()) != null;
    }

}