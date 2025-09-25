package dev.kateastenberg.bookishproject.intents;

import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.models.UserBook;

public abstract class ReadingSessionIntent {

    public static class LoadBooks extends ReadingSessionIntent {
        public String status;
        public LoadBooks () {}
        public LoadBooks(String status) {
            this.status = status;
        }
    }
    public static class OpenBook extends ReadingSessionIntent {
        public final Entry entry;
        public OpenBook (Entry entry) {
            this.entry = entry;
        }
    }

    public static class UpdateReading extends ReadingSessionIntent {
        public final Entry entry;
        public UpdateReading (Entry entry) {
            this.entry = entry;
        }
    }

    public static class CloseBook extends ReadingSessionIntent {
        public final Entry entry;
        public CloseBook (Entry entry) {
            this.entry = entry;
        }
    }

    public static class AbandonBook extends ReadingSessionIntent {
        public final Entry entry;
        public AbandonBook(Entry entry) {
            this.entry = entry;
        }
    }

    public static class SearchBooks extends ReadingSessionIntent {
        public final String title;
        public final String author;
        public SearchBooks (String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    public static class CardFlipped extends ReadingSessionIntent {
        public final int position;
        public CardFlipped (int position) {
            this.position = position;
        }
    }

    public static class AddBook extends ReadingSessionIntent {
        public AddBook () {}
    }

    public static class GoNewEntry extends ReadingSessionIntent {
        public final int position;
        public GoNewEntry(int position) {
            this.position = position;
        }
    }

    public static class UpdateEntry extends ReadingSessionIntent {
        public final Entry entry;
        public UpdateEntry (Entry entry) {
            this.entry = entry;
        }
    }

    public static class SetViewOnly extends ReadingSessionIntent {
        public SetViewOnly() {}
    }

    public static class SetEditable extends ReadingSessionIntent {
        public SetEditable() {}
    }

}
