package com.example.bookishproject;

import android.widget.ImageButton;
import android.widget.TextView;

public interface ToolbarProvider {

    public ImageButton getBackButton();
    public ImageButton getHelpButton();
    public TextView getSearchInput();
    public ImageButton getSearchButton();
    public ImageButton getAddButton();
    public ImageButton getEditButton();

}
