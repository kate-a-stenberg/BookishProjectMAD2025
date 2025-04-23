package com.example.bookishproject;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
    private ImageButton backButton, helpButton, searchButton, addButton, editButton;
    private TextView searchInput;
    private ToolbarBuilder toolbarBuilder;
    private Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // Initialize ViewPager2 and its adapter
        viewPager = binding.viewPager;
        adapter = new VPAdapter(this, 4);
        viewPager.setAdapter(adapter);

        // Set up TabLayout with ViewPager2
        tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();

        backButton = binding.backButton;
        helpButton = binding.helpButton;
        searchButton = binding.searchButton;
        addButton = binding.addButton;
        editButton = binding.editButton;
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

    @Override
    public ImageButton getEditButton() {
        return editButton;
    }

    public void setToolbar(Fragment fragment) {
        toolbarBuilder.buildToolbar(fragment);
    }
}