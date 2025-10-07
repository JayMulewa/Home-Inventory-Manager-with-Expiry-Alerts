import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class InventoryApp extends Application {

    private InventoryManager manager = new InventoryManager();
    private TableView<Item> table = new TableView<>();
    private ObservableList<Item> tableData = FXCollections.observableArrayList();
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private boolean autoNotify = true;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventory Manager");

        // ===== Navbar =====
        HBox navbar = new HBox();
        navbar.setStyle("-fx-background-color: #232f3e; -fx-padding: 10px;");
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setSpacing(20);

        Button menuBtn = new Button("☰");
        menuBtn.getStyleClass().add("menu-button");

        Label title = new Label("Inventory Manager");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((obs, old, val) -> refreshTable());

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All", "Food", "Medicine", "Electronics", "Other");
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> refreshTable());

        navbar.getChildren().addAll(menuBtn, title, searchField, categoryFilter);

        // ===== Side menu =====
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(10));
        sideMenu.setStyle("-fx-background-color: #37475a; -fx-pref-width: 0;");
        sideMenu.setMinWidth(0);

        Button addBtn = new Button("➕ Add Item");
        Button editBtn = new Button("✏️ Edit Item");
        Button removeBtn = new Button("🗑 Remove Item");
        Button exportBtn = new Button("📤 Export CSV");
        Button importBtn = new Button("📥 Import CSV");
        Button expiringBtn = new Button("⚠️ Expiring Soon");
        Button dashboardBtn = new Button("📊 Dashboard");
        Button calendarBtn = new Button("📅 Calendar View");
        CheckBox autoNotifyCheck = new CheckBox("🔔 Auto Notifications");
        autoNotifyCheck.setSelected(true);
        autoNotifyCheck.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        sideMenu.getChildren().addAll(addBtn, editBtn, removeBtn, exportBtn, importBtn, expiringBtn, dashboardBtn,
                calendarBtn, autoNotifyCheck);

        // ===== Table =====
        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> {
            String emoji = c.getValue().isExpired() ? "❌ " : c.getValue().isExpiringSoon() ? "⚠️ " : "✅ ";
            return new SimpleStringProperty(emoji + c.getValue().getName());
        });

        TableColumn<Item, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        TableColumn<Item, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayQuantity()));

        TableColumn<Item, LocalDate> expCol = new TableColumn<>("Expiry Date");
        expCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getExpiryDate()));

        table.getColumns().addAll(nameCol, catCol, qtyCol, expCol);
        table.setItems(tableData);

        // ===== Row highlighting =====
        table.setRowFactory(tv -> new TableRow<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (getTableView().getSelectionModel().getSelectedItems().contains(item)) {
                    setStyle("-fx-background-color: #3399ff; -fx-text-fill: white; -fx-font-weight: bold;");
                } else if (item.isExpired()) {
                    setStyle("-fx-background-color: #ffcccc; -fx-text-fill: black; -fx-font-weight: bold;");
                } else if (item.isExpiringSoon()) {
                    setStyle("-fx-background-color: #ffe0b3; -fx-text-fill: black; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: black; -fx-font-weight: bold;");
                }
            }
        });

        for (TableColumn<Item, ?> col : table.getColumns()) {
            @SuppressWarnings("unchecked")
            TableColumn<Item, Object> c = (TableColumn<Item, Object>) col;

            c.setCellFactory(tc -> new TableCell<Item, Object>() {
                @Override
                protected void updateItem(Object value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(value.toString());
                        setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                    }
                }
            });
        }

        HBox mainLayout = new HBox();
        mainLayout.getChildren().addAll(sideMenu, table);
        HBox.setHgrow(table, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add("data:text/css," +
                ".menu-button { -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; }" +
                ".menu-button:hover { -fx-background-color: #37475a; }" +
                "Button { -fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; }"
                +
                "Button:hover { -fx-background-color: #e68a00; }" +
                "TableView { -fx-background-color: #f4f4f4; -fx-table-cell-border-color: #d9d9d9; }" +
                "TableColumn-header { -fx-background-color: #232f3e; -fx-text-fill: white; }");

        primaryStage.setScene(scene);
        primaryStage.show();

        // ===== Menu animation =====
        menuBtn.setOnAction(e -> {
            double targetWidth = sideMenu.getWidth() > 0 ? 0 : 200;
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sideMenu.prefWidthProperty(), targetWidth);
            KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        });

        // ===== Button actions =====
        addBtn.setOnAction(e -> showAddDialog());
        editBtn.setOnAction(e -> showEditDialog());
        removeBtn.setOnAction(e -> removeSelected());
        exportBtn.setOnAction(e -> exportCSV(primaryStage));
        importBtn.setOnAction(e -> importCSV(primaryStage)); // ✅ Updated CSV import
        expiringBtn.setOnAction(e -> showExpiringSoon());
        dashboardBtn.setOnAction(e -> showDashboard());
        autoNotifyCheck.setOnAction(e -> autoNotify = autoNotifyCheck.isSelected());

        // ===== Auto Expiry Alert =====
        Platform.runLater(() -> {
            if (autoNotify)
                checkExpiringItems();
        });
    }

    // ===== Automatic Expiry Alert =====
    private void checkExpiringItems() {
        List<Item> expiring = manager.getExpiringItems();
        if (!expiring.isEmpty()) {
            StringBuilder sb = new StringBuilder("⚠️ Items expiring soon:\n\n");
            for (Item i : expiring) {
                sb.append(i.getName()).append(" (").append(i.getDisplayQuantity()).append(") → ")
                        .append(i.getExpiryDate())
                        .append(" (in ").append(i.daysToExpiry()).append(" days)\n");
            }
            showAlert("Expiry Alert", sb.toString());
        }
    }

    private void refreshTable() {
        String search = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        List<Item> filtered = manager.getAllItems();

        if (!search.isEmpty())
            filtered = manager.searchItems(search);
        if (!category.equals("All"))
            filtered = manager.filterByCategory(category);

        tableData.setAll(filtered);
    }

    // ===== Add / Edit / Remove =====
    private void showAddDialog() {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add Item");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Medicine", "Electronics", "Other");
        categoryBox.setValue("Food");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.getItems().addAll("kg", "g", "liter", "pcs");
        unitBox.setValue("kg");

        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setValue(LocalDate.now().plusDays(1));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Unit:"), 0, 3);
        grid.add(unitBox, 1, 3);
        grid.add(new Label("Expiry Date:"), 0, 4);
        grid.add(expiryPicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double qty = Double.parseDouble(quantityField.getText().trim());
                    return new Item(
                            nameField.getText().trim(),
                            categoryBox.getValue(),
                            qty,
                            unitBox.getValue(),
                            expiryPicker.getValue());
                } catch (Exception e) {
                    showAlert("Error", "Please enter valid data!");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            manager.addItem(item);
            refreshTable();
            if (autoNotify)
                checkExpiringItems();
        });
    }

    private void showEditDialog() {
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Edit Item", "Please select an item to edit.");
            return;
        }

        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Medicine", "Electronics", "Other");
        categoryBox.setValue(selected.getCategory());

        TextField quantityField = new TextField(String.valueOf(selected.getQuantity()));

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.getItems().addAll("kg", "g", "liter", "pcs");
        unitBox.setValue(selected.getUnit());

        DatePicker expiryPicker = new DatePicker(selected.getExpiryDate());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Unit:"), 0, 3);
        grid.add(unitBox, 1, 3);
        grid.add(new Label("Expiry Date:"), 0, 4);
        grid.add(expiryPicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double qty = Double.parseDouble(quantityField.getText().trim());
                    selected.setName(nameField.getText().trim());
                    selected.setCategory(categoryBox.getValue());
                    selected.setQuantity(qty);
                    selected.setUnit(unitBox.getValue());
                    selected.setExpiryDate(expiryPicker.getValue());
                    return selected;
                } catch (Exception e) {
                    showAlert("Error", "Please enter valid data!");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            refreshTable();
            if (autoNotify)
                checkExpiringItems();
        });
    }

    private void removeSelected() {
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Remove Item", "Please select an item to remove.");
            return;
        }
        manager.removeItem(selected);
        refreshTable();
    }

    // ===== Export CSV =====
    private void exportCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Item item : manager.getAllItems()) {
                    writer.println(item.getName() + "," + item.getCategory() + "," + item.getQuantity() + "," +
                            item.getUnit() + "," + item.getExpiryDate());
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to export CSV.");
            }
        }
    }

    // ===== ✅ Updated Import CSV =====
    private void importCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1)
                        continue; // skip header
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        showAlert("CSV Error", "Invalid format at line " + lineNumber);
                        continue;
                    }
                    try {
                        double quantity = Double.parseDouble(parts[2].trim());
                        String unit = parts[3].trim();
                        LocalDate expiry = LocalDate.parse(parts[4].trim());
                        Item item = new Item(parts[0].trim(), parts[1].trim(), quantity, unit, expiry);
                        manager.addItem(item);
                    } catch (Exception e) {
                        showAlert("CSV Error", "Invalid number or date at line " + lineNumber);
                    }
                }
                refreshTable();
                if (autoNotify)
                    checkExpiringItems();
            } catch (Exception e) {
                showAlert("Error",
                        "Failed to import CSV. Format: Name,Category,Quantity,Unit,Expiry yyyy-mm-dd");
            }
        }
    }

    private void showExpiringSoon() {
        List<Item> expiring = manager.getExpiringItems();
        if (expiring.isEmpty())
            showAlert("Expiring Soon", "No items are expiring in the next 3 days.");
        else {
            StringBuilder sb = new StringBuilder();
            for (Item i : expiring)
                sb.append("⚠️ ").append(i.getName())
                        .append(" → ").append(i.getExpiryDate())
                        .append(" (in ").append(i.daysToExpiry()).append(" days)\n");
            showAlert("Expiring Soon", sb.toString());
        }
    }

    private void showDashboard() {
        long total = manager.getAllItems().size();
        long expired = manager.getAllItems().stream().filter(Item::isExpired).count();
        long expSoon = manager.getAllItems().stream().filter(Item::isExpiringSoon).count();
        long safe = total - expired - expSoon;

        Alert dashboard = new Alert(Alert.AlertType.INFORMATION);
        dashboard.setTitle("📊 Dashboard Summary");
        dashboard.setHeaderText("Inventory Overview");
        String msg = "Total Items: " + total + "\n" +
                "❌ Expired: " + expired + "\n" +
                "⚠️ Expiring Soon: " + expSoon + "\n" +
                "✅ Safe: " + safe;
        dashboard.setContentText(msg);
        dashboard.showAndWait();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
