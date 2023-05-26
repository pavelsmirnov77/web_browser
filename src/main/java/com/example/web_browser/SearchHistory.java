package com.example.web_browser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class SearchHistory {
    private ObservableList<String> entries;

    public SearchHistory() {
        entries = FXCollections.observableArrayList();
    }

    public void addEntry(String entry) {
        entries.add(entry);
    }

    public ObservableList<String> getEntries() {
        return entries;
    }
}
