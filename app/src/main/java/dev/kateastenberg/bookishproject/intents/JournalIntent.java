package dev.kateastenberg.bookishproject.intents;

public abstract class JournalIntent {

    public static class LoadEntries extends JournalIntent {}

    public static class SearchEntries extends JournalIntent {
        public final String query;
        public SearchEntries (String query) {
            this.query = query;
        }
    }

    public static class EntryClicked extends JournalIntent {
        public final int position;
        public EntryClicked (int position) {
            this.position = position;
        }
    }

    public static class EntryLongClicked extends JournalIntent {
        public final int position;
        public EntryLongClicked (int position) {
            this.position = position;
        }
    }

    public static class AddEntry extends JournalIntent {}

}
