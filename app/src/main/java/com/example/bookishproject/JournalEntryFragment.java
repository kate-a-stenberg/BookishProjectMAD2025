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

public class JournalEntryFragment extends Fragment {
    private FragmentJournalEntryBinding binding;
    private static final String ARG_ENTRY = "entry";
    private TextView title, author, date, comments, description;
    private ImageView cover;
    private Entry entry;

    public JournalEntryFragment(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entry = getArguments().getParcelable("entry");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalEntryBinding.inflate(inflater, container, false);

        title = binding.textEntryTitle;
        author = binding.textJournalEntryAuthor;
        cover = binding.imageJournalEntryCover;
        date = binding.textJournalEntryDate;
        description = binding.textJournalEntryDescription;
        comments = binding.textEntryComments;

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

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (entry != null) {
            binding.textEntryTitle.setText(entry.getBook().getTitle());
            binding.textJournalEntryAuthor.setText(entry.getBook().getAuthor());
        }

        binding.buttonEntryBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToJournalFragment();
            }
        });
    }

}
