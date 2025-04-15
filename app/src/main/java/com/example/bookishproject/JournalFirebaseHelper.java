package com.example.bookishproject;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalFirebaseHelper {
    private static final String TAG = "Entry To Firebase";

    private DatabaseReference dbRef;

    public JournalFirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.dbRef = database.getReference("journal_entries");
    }

    public interface FirebaseCallback {
        void onCallback(List<Entry> entries);
    }

    public void addEntry(Entry entry) {
        Log.d(TAG, "now inside addEntry()");

        if (entry.getId() == null || entry.getId().isEmpty()) {

            Log.d(TAG, "Entry id is null, or empty. this is expected. will now attempt to set id to dbRef.push().getKey()");
            String entryId = dbRef.push().getKey();
            Log.d(TAG, "id = dbRef.push().getKey() passed. now attempting to set entryId.");
            entry.setId(entryId);
            Log.d(TAG, "entryId set.");
        }

        Log.d(TAG, "about to attempt dbRef.child(entry.getId().setValue(entry)");
        dbRef.child(entry.getId()).setValue(entry);

        Log.d(TAG, "Success. JournalFirebaseHelper.addEntry, returning to JournalEntryFragment");

    }

    public void getAllEntries(FirebaseCallback callback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> entries = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Entry entry = snapshot.getValue(Entry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }

                callback.onCallback(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JournalFirebaseHelper", "Error fetching entries: " + error.getMessage());
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    public void getEntriesForBook(String bookId, FirebaseCallback callback) {
        dbRef.orderByChild("bookId").equalTo(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> entries = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Entry entry = snapshot.getValue(Entry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
                callback.onCallback(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                Log.e("JournalFirebaseHelper", "Error fetching entries: " + error.getMessage());
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    public void updateEntry(Entry entry) {
        // probably not necessary. future version
    }

    public void deleteEntry(String entryId) {
        // not currently necessary
    }

}
