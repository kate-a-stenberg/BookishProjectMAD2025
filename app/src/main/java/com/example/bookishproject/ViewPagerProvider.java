package com.example.bookishproject;

import androidx.viewpager2.widget.ViewPager2;

public interface ViewPagerProvider {

    public ViewPager2 getViewPager();
    public VPAdapter getVPAdapter();

}
