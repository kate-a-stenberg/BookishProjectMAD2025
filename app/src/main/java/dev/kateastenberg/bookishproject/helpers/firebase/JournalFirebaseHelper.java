package dev.kateastenberg.bookishproject.helpers.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kateastenberg.bookishproject.models.Entry;

/*
A class to handle interactions with the firebase database.
Uses a Database Reference object
 */
public class JournalFirebaseHelper {

    private final DatabaseReference dbRef;

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
    public void addEntry(Entry entry, FirebaseCallback callback) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("JournalFirebaseHelper", "No current authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        entry.setUserId(userId);

        // if the Entry doesn't already have an id
        if (entry.getId() == null || entry.getId().isEmpty()) {

            // push the Entry to the database, get its key
            String entryId = dbRef.child(userId).push().getKey();
            // use that key as the entryId
            entry.setId(entryId);
        }

        // ask the database to set the Entry as the child of the entryId in the database
        dbRef.child(userId).child(entry.getId()).setValue(entry)
                .addOnSuccessListener(aVoid -> {
                    // Success - but your callback expects List<Entry>, not void
                    // You might need a different callback interface for single operations
                    callback.onCallback(Collections.singletonList(entry));
                })
                .addOnFailureListener(e -> {
                    Log.e("Journal", "Error saving entry to database: " + e.getMessage(), e);
                    callback.onCallback(new ArrayList<>());
                });

    }

    /*
    Method to get all the entries from the database
     */
    public void getAllEntries(FirebaseCallback callback) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("JournalFirebaseHelper", "No current authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Query entries ordered by timestamp (oldest first)
        dbRef.child(userId).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
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

                // Reverse to get newest first
                Collections.reverse(entries);
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

    public void getEntries(final FirebaseCallback callback, String query) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("Journal Firebase Helper", "No authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                List<Entry> entryList = new ArrayList<>();

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    Entry entry = snapshot.getValue(Entry.class);
                    if (entry != null) {
                        String title = entry.getUserBook().getBook().getTitle();
                        String author = entry.getUserBook().getBook().getAuthor();
                        boolean titleMatch = title != null && title.toLowerCase().contains(query.toLowerCase());
                        boolean authorMatch = author != null && author.toLowerCase().contains(query.toLowerCase());

                        if (titleMatch || authorMatch) {
                            entryList.add(entry);
                        }
                    }
                }

                callback.onCallback(entryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    /*
    interface for a FirebaseCallback type class
    */
    public interface FirebaseCallback {
        void onCallback(List<Entry> entries);
    }

    public interface SingleEntryCallback {
        void onSuccess(Entry entry);
        void onError(String error);
    }

}
