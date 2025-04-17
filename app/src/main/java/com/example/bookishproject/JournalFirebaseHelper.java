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

/*
A class to handle interactions with the firebase database.
Uses a Database Reference object
 */
public class JournalFirebaseHelper {

    private DatabaseReference dbRef;

    /*
    No-argument constructor
     */
    public JournalFirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.dbRef = database.getReference("journal_entries");
    }

    /*
    Method to add a new Entry to the database
     */
    public void addEntry(Entry entry) {

        // if the Entry doesn't already have an id
        if (entry.getId() == null || entry.getId().isEmpty()) {

            // push the Entry to the database, get its key
            String entryId = dbRef.push().getKey();
            // use that key as the entryId
            entry.setId(entryId);
        }

        // ask the database to set the Entry as the child of the entryId in the database
        dbRef.child(entry.getId()).setValue(entry);

    }

    /*
    Method to get all the entries from the database
     */
    public void getAllEntries(FirebaseCallback callback) {
        // Add a new ValueEventListener--listens for when something in the database changes
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // create a new array list of Entries
                List<Entry> entries = new ArrayList<>();

                // go through all the data points in the database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // create an Entry out of that data point
                    Entry entry = snapshot.getValue(Entry.class);
                    // if the entry isn't null, add it to the entry list
                    if (entry != null) {
                        entries.add(entry);
                    }
                }

                // give the entries list to the app
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
        // TODO: implement this
    }

    public void updateEntry(Entry entry) {
        // probably not necessary. future version
    }

    public void deleteEntry(String entryId) {
        // not currently necessary
    }

    /*
    interface for a FirebaseCallback type class
    */
    public interface FirebaseCallback {
        void onCallback(List<Entry> entries);
    }

}
