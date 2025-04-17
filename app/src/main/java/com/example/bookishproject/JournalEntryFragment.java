package com.example.bookishproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.bookishproject.databinding.FragmentJournalEntryBinding;

/*
This class represents a journal entry
A journal entry displays view-only information about an reading activity that a user entered.
It has view binding, layout fields and elements, an Entry that it's based on.
It also has a static final variable
 */
public class JournalEntryFragment extends Fragment {

    // this variable is the name of the Bundle that contains information on the Entry whose information to populate its fields with
    // it receives this from JournalFragment via MainActivity
    private static final String ARG_ENTRY = "entry";

    private FragmentJournalEntryBinding binding;
    private TextView title, author, date, comments, description;
    private ImageView cover;
    private Entry entry;

    // a constructor using an Entry object as a parameter / information source
    public JournalEntryFragment(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get the Entry whose data will populate the fragment will come from the bundle with this name
            entry = getArguments().getParcelable("entry");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalEntryBinding.inflate(inflater, container, false);

        // set all variables for layout fields
        title = binding.textEntryTitle;
        author = binding.textJournalEntryAuthor;
        cover = binding.imageJournalEntryCover;
        date = binding.textJournalEntryDate;
        description = binding.textJournalEntryDescription;
        comments = binding.textEntryComments;

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (entry != null) {

            // set all fields with data from the Entry
            title.setText(entry.getBook().getTitle());
            author.setText(entry.getBook().getAuthor());
            date.setText(entry.getDate().displayDate());
            description.setText(entry.getDescription());
            comments.setText(entry.getComments());
            if (entry.getBook().getCoverUrl() != null && !entry.getBook().getCoverUrl().isEmpty()) {
                Glide.with(cover.getContext()).load(entry.getBook().getCoverUrl()).placeholder(R.drawable.pwrf).error(R.drawable.pwrf).into(cover);
            }
            else {
                cover.setImageResource(entry.getBook().getCover());
            }

        }

        // back button will go back to JournalFragment
        binding.buttonEntryBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                // ask the MainActivity to go to JournalFragment
                ((MainActivity)getActivity()).navigateToJournalFragment();
            }
        });
    }

}
