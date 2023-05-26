package com.example.web_browser;

import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SearchHistoryTab extends Tab {

    public SearchHistoryTab(SearchHistory searchHistory) {
        setText("История поиска");
        ListView<String> historyListView = new ListView<>();
        historyListView.setItems(searchHistory.getEntries());
        setContent(historyListView);

        historyListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedEntry = historyListView.getSelectionModel().getSelectedItem();
                if (selectedEntry != null) {
                    WebView webView = new WebView();
                    WebEngine webEngine = webView.getEngine();
                    webEngine.load(selectedEntry);
                    setContent(webView);
                }
            }
        });
    }
}
