package com.example.web_browser;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class WebApplication extends Application {

    private TabPane tabPane;
    private Scene scene;

    private TextField urlField;
    private SearchHistory searchHistory;
    private TextField searchField;
    private boolean isDarkMode = true;
    private Button themeToggleButton;

    @Override
    public void start(Stage primaryStage) {
        tabPane = new TabPane();
        urlField = new TextField();
        urlField.setPrefWidth(850);

        urlField.getStyleClass().add("custom-textfield");

        urlField.setOnAction(e -> loadURL(urlField.getText()));

        searchHistory = new SearchHistory();

        Button newTabButton = createStyledButton("+");
        newTabButton.setOnAction(e -> createNewTab("Новая вкладка"));

        Button backButton = createStyledButton("<");
        backButton.setOnAction(e -> goBack());

        Button forwardButton = createStyledButton(">");
        forwardButton.setOnAction(e -> goForward());

        Button refreshButton = createStyledButton("\uD83D\uDD04");
        refreshButton.setOnAction(e -> refreshPage());

        Button clearButton = createStyledButton("X");
        clearButton.setOnAction(e -> urlField.clear());

        themeToggleButton = createStyledButton("☀");
        themeToggleButton.setOnAction(e -> toggleTheme());


        bindTextFieldHeight(urlField, newTabButton, backButton, forwardButton, refreshButton, clearButton);

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("toolbar");
        toolbar.getChildren().addAll(newTabButton, backButton, forwardButton, refreshButton, urlField, clearButton, themeToggleButton);


        VBox root = new VBox();
        root.getChildren().addAll(createMenuBar(), toolbar, tabPane);

        searchField = new TextField();
        searchField.setPromptText("Поиск на странице");
        Button searchButton = createStyledButton("🔎");
        searchButton.setOnAction(e -> searchOnPage());

        toolbar.getChildren().addAll(searchField, searchButton);


        scene = new Scene(root, 1460, 750);
        scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Мой Браузер");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("img.png")));
        primaryStage.show();

        createNewTab("Новая вкладка");
        searchHistory = new SearchHistory();
    }

    private void bindTextFieldHeight(TextField textField, Button... buttons) {
        for (Button button : buttons) {
            textField.minHeightProperty().bind(button.heightProperty());
            textField.maxHeightProperty().bind(button.heightProperty());
            textField.prefHeightProperty().bind(button.heightProperty());
        }
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("styled-button");
        button.setOnMouseEntered(event -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.2), button);
            scaleIn.setToX(1.2);
            scaleIn.setToY(1.2);
            scaleIn.play();
        });

        button.setOnMouseExited(event -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(0.2), button);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
        return button;
    }

    private void createNewTab(String title) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine(); // Создаем новый экземпляр WebEngine для каждой вкладки

        Tab tab = new Tab();
        tab.setClosable(true);
        tab.setText(title);
        tab.setContent(webView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        tab.setOnClosed(e -> {
            tabPane.getTabs().remove(tab);
            if (tabPane.getTabs().isEmpty()) {
                createNewTab(webEngine.getTitle());
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                webView.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), webView);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                String pageTitle = webEngine.getTitle();
                if (pageTitle != null && !pageTitle.isEmpty()) {
                    tab.setText(pageTitle);
                } else {
                    tab.setText("Новая вкладка");
                }
                String url = webEngine.getLocation();
                searchHistory.addEntry(url);
            }
        });

        ListView<String> historyListView = new ListView<>();
        historyListView.setItems(searchHistory.getEntries());

        historyListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedEntry = historyListView.getSelectionModel().getSelectedItem();
                if (selectedEntry != null) {
                    webEngine.load(selectedEntry);
                }
            }
        });

        webEngine.load("https://www.google.com");
    }

    private void loadURL(String url) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            WebView webView = (WebView) currentTab.getContent();
            WebEngine webEngine = webView.getEngine();

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            webEngine.load(url);
        }
    }

    private void goBack() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            WebView webView = (WebView) currentTab.getContent();
            WebEngine webEngine = webView.getEngine();

            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        }
    }

    private void goForward() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            WebView webView = (WebView) currentTab.getContent();
            WebEngine webEngine = webView.getEngine();

            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
            }
        }
    }

    private void refreshPage() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            WebView webView = (WebView) currentTab.getContent();
            WebEngine webEngine = webView.getEngine();
            webEngine.reload();
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Окна");
        MenuItem newWindowItem = new MenuItem("Новое окно");
        MenuItem newTabItem = new MenuItem("Новая вкладка");
        fileMenu.getItems().addAll(newWindowItem, newTabItem);

        Menu viewMenu = new Menu("Вид");
        MenuItem zoomItem = new MenuItem("Масштаб");
        viewMenu.getItems().add(zoomItem);

        Menu historyMenu = new Menu("История");
        MenuItem searchHistoryItem = new MenuItem("История поиска");
        historyMenu.getItems().add(searchHistoryItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, historyMenu);

        newWindowItem.setOnAction(e -> createNewWindow());
        newTabItem.setOnAction(e -> createNewTab("Новая вкладка"));
        zoomItem.setOnAction(e -> showZoomDialog());
        searchHistoryItem.setOnAction(e -> showSearchHistory());

        return menuBar;
    }

    private void createNewWindow() {
        WebApplication newWebApplication = new WebApplication();
        Stage newStage = new Stage();
        newWebApplication.start(newStage);
    }

    private void showZoomDialog() {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Выбор масштаба");
        dialog.setHeaderText("Выберите масштаб:");

        ButtonType applyButton = new ButtonType("Применить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);

        Slider zoomSlider = new Slider(0.25, 2.0, 1.0);
        zoomSlider.setMajorTickUnit(0.25);
        zoomSlider.setMinorTickCount(0);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);

        Label zoomLabel = new Label();
        zoomLabel.textProperty().bind(Bindings.format("%.0f%%", zoomSlider.valueProperty().multiply(100)));

        dialog.getDialogPane().setContent(new VBox(10, zoomSlider, zoomLabel));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButton) {
                return zoomSlider.getValue();
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(zoomValue -> {
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (currentTab != null) {
                WebView webView = (WebView) currentTab.getContent();
                WebEngine webEngine = webView.getEngine();
                webView.setZoom(zoomValue);
                webEngine.executeScript("document.body.style.zoom = " + zoomValue);
            }
        });
    }

    private void showSearchHistory() {
        SearchHistoryTab searchHistoryTab = new SearchHistoryTab(searchHistory);
        tabPane.getTabs().add(searchHistoryTab);
        tabPane.getSelectionModel().select(searchHistoryTab);
    }

    private void searchOnPage() {
        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (currentTab != null) {
                WebView webView = (WebView) currentTab.getContent();
                WebEngine webEngine = webView.getEngine();

                webEngine.executeScript("window.find('" + searchText + "')");
            }
        }
    }

    private void toggleTheme() {
        if (isDarkMode) {
            themeToggleButton.setText("\uD83C\uDF19");
            scene.getStylesheets().remove(getClass().getResource("light-theme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        } else {
            themeToggleButton.setText("☀");
            scene.getStylesheets().remove(getClass().getResource("dark-theme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
        }
        isDarkMode = !isDarkMode;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
