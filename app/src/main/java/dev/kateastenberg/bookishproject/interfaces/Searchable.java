package dev.kateastenberg.bookishproject.interfaces;

public interface Searchable {
    void performSearch(String query);
    String getCurrentSearchQuery();
    void clearSearch();
    void setSearchQuery(String query);
}
