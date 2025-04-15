package com.example.bookishproject;

import android.util.Log;

import java.util.List;

public class ComparerBasic implements Comparer {

    private Book book;

    public ComparerBasic(Book book) {
        this.book = book;
    }

    @Override
    public float compareBooks(Book compareBook) {
        float matchScore = 0;
        float earnedScore = 0;
        float totalScore = 0;
        Log.d("BookComparison", "Inside compareBooks(): trying " + book.getTitle() + " and " +compareBook.getTitle());
        earnedScore += compareAuthor(compareBook);
        totalScore += 5;
        Log.d("BookComparison", "Compared Authors. Earned score so far: " + earnedScore);
        earnedScore += compareGenre(compareBook);
        totalScore += 5;
        Log.d("BookComparison", "Compared Genres. Earned score so far: " + earnedScore);
        earnedScore += compareAgeRange(compareBook);
        totalScore += 5;
        Log.d("BookComparison", "Compared Authors. Earned score so far: " + earnedScore);
        earnedScore += compareCategories(compareBook);
        totalScore += 5;
        Log.d("BookComparison", "Compared Authors. Earned score so far: " + earnedScore);
        matchScore = earnedScore/totalScore;
        Log.d("BookComparison", "Earned score: " + earnedScore);
        Log.d("BookComparison", "Total score: " + totalScore);
        Log.d("BookComparison", "Match score: " + matchScore);
        return matchScore;
    }

    private float compareAuthor(Book compareBook) {
        float score = 0;
        Log.d("BookComparison", "book author: " + book.getAuthor());
        Log.d("BookComparison", "compareBook author: " + compareBook.getAuthor());

        if (book.getAuthor() != null && compareBook.getAuthor() != null) {
            // Exact match check
            if (book.getAuthor().equals(compareBook.getAuthor())) {
                score = 5;
            }
            // Partial match check (for multiple authors)
            else {
                String[] authors1 = book.getAuthor().split(",");
                String[] authors2 = compareBook.getAuthor().split(",");

                // Trim whitespace from each author name
                for (int i = 0; i < authors1.length; i++) {
                    authors1[i] = authors1[i].trim();
                }
                for (int i = 0; i < authors2.length; i++) {
                    authors2[i] = authors2[i].trim();
                }

                // Check for any overlap
                boolean foundMatch = false;
                for (String author1 : authors1) {
                    for (String author2 : authors2) {
                        if (author1.equals(author2)) {
//                            Log.d("BookComparison", "Partial author match: " + author1);
                            score = 3; // Give partial credit (3 instead of 5)
                            foundMatch = true;
                            break;
                        }
                    }
                    if (foundMatch) break;
                }
            }
        }
        return score;
    }

    private float compareGenre(Book compareBook) {
        float score = 0;
        Log.d("BookComparison", "book genre: " + book.getGenre());
        Log.d("BookComparison", "compareBook genre: " + compareBook.getGenre());
        if (book.getGenre() != null && compareBook.getGenre() != null && book.getGenre().equals(compareBook.getGenre())) {
            score = 5;
        }
        return score;
    }

    private float compareAgeRange(Book compareBook) {
        Log.d("BookComparison", "book age range: " + book.getAgeRange());
        Log.d("BookComparison", "compareBook age range: " + compareBook.getAgeRange());
        float score;
        if (book.getAgeRange() != null && compareBook.getAgeRange() != null && book.getAgeRange().equals(compareBook.getAgeRange())) {
            score = 5;
        }
        else {
            score = 0;
        }
        return score;
    }

    private float compareCategories(Book compareBook) {
        Log.d("BookComparison", "book categories: " + book.getCategories());
        Log.d("BookComparison", "compareBook categories: " + compareBook.getCategories());
        float score = 0;
        List<String> catsBook = book.getCategories();
        List<String> catsComp = compareBook.getCategories();

        if (catsBook != null && catsComp != null && !catsBook.isEmpty() && !catsComp.isEmpty()) {
            for (String cat : catsBook) {
                if (catsComp.contains(cat)) {
                    score += 1; // Add 1 point per shared category
                }
            }
        }
        score = Math.max(5, catsBook.size()); // Cap at reasonable max
        return score;
    }

}
