package com.habitflow.controller;

import com.habitflow.dao.HabitDAO;
import com.habitflow.dao.TaskDAO;
import com.habitflow.model.Habit;
import com.habitflow.model.Task;
import com.habitflow.model.User;
import com.habitflow.service.UserSession;
import com.habitflow.util.AnimationHelper;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MainController
        implements Initializable {

    @FXML private TextField searchField;
    @FXML private TextField quickAddField;
    @FXML private Button    addButton;
    @FXML private Button    profileButton;

    @FXML private Button navMyDay;
    @FXML private Button navImportant;
    @FXML private Button navHabits;
    @FXML private Button navStats;
    @FXML private Button navDosDonts;
    @FXML private Button navCompleted;
    @FXML private Button navDelayed;
    @FXML private Button navDeleted;
    @FXML private Button navNotifyLater;

    @FXML private HBox           headerRow;
    @FXML private Label          sectionTitle;
    @FXML private Label          sectionSubtitle;
    @FXML private ListView<Task> taskListView;
    @FXML private Button         clearAllButton;
    @FXML private Button         addHabitButton;

    private final HabitsController habitsCtrl =
        new HabitsController();
    private final DoDontsController doDontsCtrl =
        new DoDontsController();
    private final AddTaskModal addTaskModal =
        new AddTaskModal();
    private final ProfileMenuController
        profileMenuCtrl =
            new ProfileMenuController();
    private final StatsController statsCtrl =
        new StatsController();
    private final HabitDAO habitDAO =
        new HabitDAO();
    private final TaskDAO taskDAO =
        new TaskDAO();

    private Button       activeNavButton;
    private Task.Section currentSection;
    private boolean      animateCards = false;
    private PauseTransition searchDebounce;

    @Override
    public void initialize(URL url,
            ResourceBundle rb) {
        currentSection  = Task.Section.MY_DAY;
        activeNavButton = navMyDay;
        quickAddField.setOnAction(
            e -> onQuickAddTask());
        searchField.textProperty().addListener(
            (obs, old, val) ->
                onSearchChanged(val));
        taskListView.setCellFactory(
            lv -> new TaskCardCell());
        setupAvatarButton();
        addSidebarHoverEffects();
        loadSection(Task.Section.MY_DAY,
            "My Day");
    }

    public void refreshAvatar() {
        setupAvatarButton();
        if (currentSection ==
                Task.Section.MY_DAY) {
            loadSection(Task.Section.MY_DAY,
                "My Day");
        }
    }

    private void setupAvatarButton() {
        User user =
            UserSession.getCurrentUser();
        if (user == null) return;
        Circle avatarBg = new Circle(16);
        avatarBg.setFill(
            Color.web(user.getAvatarColor()));
        Label initials = new Label(
            user.getInitials());
        initials.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';");
        StackPane avatar = new StackPane(
            avatarBg, initials);
        avatar.setMaxSize(32, 32);
        profileButton.setText("");
        profileButton.setGraphic(avatar);
        profileButton.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-padding: 4px;" +
            "-fx-cursor: hand;");
        AnimationHelper.addHoverScale(
            profileButton, 1.1);
    }

    private void addSidebarHoverEffects() {
        Button[] navButtons = {
            navMyDay, navImportant,
            navHabits, navStats,
            navDosDonts, navCompleted,
            navDelayed, navDeleted,
            navNotifyLater };
        for (Button btn : navButtons) {
            if (btn != null)
                AnimationHelper.addHoverScale(
                    btn, 1.03);
        }
        AnimationHelper.addHoverScale(
            addButton, 1.1);
    }

    // ═══════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════

    @FXML
    private void onNavClicked(
            javafx.event.ActionEvent event) {
        Button clicked =
            (Button) event.getSource();
        if (activeNavButton != null)
            activeNavButton.getStyleClass()
                .remove("nav-button-active");
        clicked.getStyleClass()
            .add("nav-button-active");
        activeNavButton = clicked;
        switch (clicked.getId()) {
            case "navMyDay" ->
                loadSection(
                    Task.Section.MY_DAY,
                    "My Day");
            case "navImportant" ->
                loadSection(
                    Task.Section.IMPORTANT,
                    "Important");
            case "navHabits" ->
                loadSection(
                    Task.Section.HABITS,
                    "Habits");
            case "navDosDonts" ->
                loadDosDonts();
            case "navCompleted" ->
                loadSection(
                    Task.Section.COMPLETED,
                    "Completed");
            case "navDelayed" ->
                loadSection(
                    Task.Section.DELAYED,
                    "Delayed");
            case "navDeleted" ->
                loadSection(
                    Task.Section.DELETED,
                    "Deleted");
            case "navNotifyLater" ->
                loadSection(
                    Task.Section.NOTIFY_LATER,
                    "Notify Later");
            case "navStats" ->
                loadStats();
            default -> {
                sectionTitle.setText(
                    clicked.getText().trim());
                sectionSubtitle.setText("");
            }
        }
    }

    // ═══════════════════════════════════════
    // LOAD SECTION
    // ═══════════════════════════════════════

    private void loadSection(
            Task.Section section,
            String title) {
        currentSection = section;
        sectionTitle.setText(title);
        clearAllButton.setVisible(false);
        clearAllButton.setManaged(false);
        addHabitButton.setVisible(false);
        addHabitButton.setManaged(false);

        VBox center = (VBox)
            taskListView.getParent();
        center.getChildren().removeIf(n ->
            n instanceof ScrollPane ||
            (n instanceof HBox &&
             n != headerRow));

        if (section == Task.Section.HABITS) {
            taskListView.setVisible(false);
            taskListView.setManaged(false);
            addHabitButton.setVisible(true);
            addHabitButton.setManaged(true);
            VBox habitsScreen =
                habitsCtrl.buildHabitsScreen();
            ScrollPane scroll =
                new ScrollPane(habitsScreen);
            scroll.setFitToWidth(true);
            scroll.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: " +
                "transparent;");
            VBox.setVgrow(scroll,
                Priority.ALWAYS);
            center.getChildren().add(scroll);
            AnimationHelper.fadeSlideIn(
                scroll, 300, 20);
            int count =
                habitDAO.getAllActive().size();
            sectionSubtitle.setText(
                count + " habit" +
                (count == 1 ? "" : "s"));
            return;
        }

        taskListView.setVisible(true);
        taskListView.setManaged(true);

        boolean showClear =
            section == Task.Section.COMPLETED ||
            section == Task.Section.DELAYED   ||
            section == Task.Section.DELETED;
        clearAllButton.setVisible(showClear);
        clearAllButton.setManaged(showClear);

        // Enable card animations for
        // this load only
        animateCards = true;

        List<Task> tasks =
            taskDAO.getBySection(section);
        taskListView.getItems().setAll(tasks);

        AnimationHelper.fadeIn(
            sectionTitle, 200);

        updateSubtitle();

        taskListView.setPlaceholder(
            new Label(
                getPlaceholderText(section)));

        AnimationHelper.fadeIn(
            taskListView, 250);
    }

    // ═══════════════════════════════════════
    // LOAD DOS / DONTS
    // ═══════════════════════════════════════

    private void loadDosDonts() {
        currentSection = Task.Section.DO;
        sectionTitle.setText("Dos / Don'ts");
        sectionSubtitle.setText(
            "Track your daily dos and don'ts");
        clearAllButton.setVisible(false);
        clearAllButton.setManaged(false);
        addHabitButton.setVisible(false);
        addHabitButton.setManaged(false);
        taskListView.setVisible(false);
        taskListView.setManaged(false);
        VBox center = (VBox)
            taskListView.getParent();
        center.getChildren().removeIf(n ->
            n instanceof ScrollPane ||
            (n instanceof HBox &&
             n != headerRow));
        HBox dodontScreen =
            doDontsCtrl.buildScreen();
        VBox.setVgrow(dodontScreen,
            Priority.ALWAYS);
        center.getChildren().add(dodontScreen);
        AnimationHelper.fadeSlideIn(
            dodontScreen, 300, 20);
    }

    // ═══════════════════════════════════════
    // LOAD STATS
    // ═══════════════════════════════════════

    private void loadStats() {
        currentSection = Task.Section.MY_DAY;
        sectionTitle.setText("📊  Stats");
        sectionSubtitle.setText(
            "Your productivity at a glance");
        clearAllButton.setVisible(false);
        clearAllButton.setManaged(false);
        addHabitButton.setVisible(false);
        addHabitButton.setManaged(false);
        taskListView.setVisible(false);
        taskListView.setManaged(false);
        VBox center = (VBox)
            taskListView.getParent();
        center.getChildren().removeIf(n ->
            n instanceof ScrollPane ||
            (n instanceof HBox &&
             n != headerRow));
        ScrollPane statsScreen =
            statsCtrl.buildStatsScreen();
        VBox.setVgrow(statsScreen,
            Priority.ALWAYS);
        center.getChildren().add(statsScreen);
        AnimationHelper.fadeSlideIn(
            statsScreen, 400, 25);
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 5)  return "Good night";
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        if (hour < 21) return "Good evening";
        return "Good night";
    }

    private String getPlaceholderText(
            Task.Section section) {
        return switch (section) {
            case MY_DAY ->
                "No tasks today. " +
                "Add one above!";
            case IMPORTANT ->
                "No important tasks yet.";
            case COMPLETED ->
                "No completed tasks yet.";
            case DELAYED ->
                "No delayed tasks.";
            case DELETED ->
                "Deleted items will " +
                "appear here.";
            case NOTIFY_LATER ->
                "No reminders set.";
            default ->
                "Nothing here yet.";
        };
    }

    /**
     * Updates the subtitle text with
     * current item count. Called after
     * marking done/delayed/delete without
     * full section reload.
     */
    private void updateSubtitle() {
        int count =
            taskListView.getItems().size();
        if (currentSection ==
                Task.Section.MY_DAY) {
            User user =
                UserSession.getCurrentUser();
            String name = user != null
                ? user.getFullName() : "";
            String today = LocalDate.now()
                .format(DateTimeFormatter
                    .ofPattern(
                        "EEEE, MMMM d, yyyy"));
            sectionSubtitle.setText(
                getGreeting() + ", " + name +
                "!  •  " + count +
                " task" +
                (count == 1 ? "" : "s") +
                "  •  " + today);
        } else {
            sectionSubtitle.setText(
                count + " item" +
                (count == 1 ? "" : "s"));
        }
    }

    // ═══════════════════════════════════════
    // QUICK ADD
    // ═══════════════════════════════════════

    @FXML
    private void onQuickAddTask() {
        String title =
            quickAddField.getText().trim();
        if (title.isEmpty()) return;
        if (currentSection ==
                Task.Section.HABITS ||
            currentSection ==
                Task.Section.DO ||
            currentSection ==
                Task.Section.DONT) return;
        Task task = new Task(title);
        task.setSection(currentSection);
        task.setDueDate(LocalDate.now());
        taskDAO.save(task);
        loadSection(currentSection,
            sectionTitle.getText());
        quickAddField.clear();
    }

    // ═══════════════════════════════════════
    // SEARCH — debounced 250ms
    // ═══════════════════════════════════════

    private void onSearchChanged(String q) {
        if (searchDebounce != null) {
            searchDebounce.stop();
        }
        searchDebounce = new PauseTransition(
            Duration.millis(250));
        searchDebounce.setOnFinished(e -> {
            if (q == null || q.isBlank()) {
                loadSection(currentSection,
                    sectionTitle.getText());
                return;
            }
            List<Task> all =
                taskDAO.getBySection(
                    currentSection);
            String lower = q.toLowerCase();
            List<Task> filtered =
                all.stream()
                .filter(t ->
                    t.getTitle().toLowerCase()
                        .contains(lower) ||
                    (t.getDescription() != null
                     && t.getDescription()
                        .toLowerCase()
                        .contains(lower)))
                .toList();
            animateCards = true;
            taskListView.getItems()
                .setAll(filtered);
        });
        searchDebounce.play();
    }

    // ═══════════════════════════════════════
    // TOP BAR BUTTONS
    // ═══════════════════════════════════════

    @FXML
    private void onAddButtonClicked() {
        Task.Section sectionToUse =
            currentSection;
        if (currentSection ==
                Task.Section.HABITS ||
            currentSection ==
                Task.Section.DO ||
            currentSection ==
                Task.Section.DONT)
            sectionToUse =
                Task.Section.MY_DAY;
        Task newTask =
            addTaskModal.show(sectionToUse);
        if (newTask != null) {
            taskDAO.save(newTask);
            Task.Section addedTo =
                newTask.getSection();
            switch (addedTo) {
                case IMPORTANT -> {
                    if (activeNavButton != null)
                        activeNavButton
                            .getStyleClass()
                            .remove(
                            "nav-button-active");
                    navImportant.getStyleClass()
                        .add(
                        "nav-button-active");
                    activeNavButton =
                        navImportant;
                    loadSection(
                        Task.Section.IMPORTANT,
                        "Important");
                }
                case NOTIFY_LATER -> {
                    if (activeNavButton != null)
                        activeNavButton
                            .getStyleClass()
                            .remove(
                            "nav-button-active");
                    navNotifyLater
                        .getStyleClass()
                        .add(
                        "nav-button-active");
                    activeNavButton =
                        navNotifyLater;
                    loadSection(
                        Task.Section
                            .NOTIFY_LATER,
                        "Notify Later");
                }
                default -> {
                    if (activeNavButton != null)
                        activeNavButton
                            .getStyleClass()
                            .remove(
                            "nav-button-active");
                    navMyDay.getStyleClass()
                        .add(
                        "nav-button-active");
                    activeNavButton = navMyDay;
                    loadSection(
                        Task.Section.MY_DAY,
                        "My Day");
                }
            }
        }
    }

    @FXML
    private void onProfileButtonClicked() {
        showProfileCard();
    }

    // ═══════════════════════════════════════
    // PROFILE CARD POPUP
    // ═══════════════════════════════════════

    private void showProfileCard() {
        User user =
            UserSession.getCurrentUser();
        if (user == null) {
            Stage stage = (Stage)
                profileButton.getScene()
                    .getWindow();
            profileMenuCtrl.show(
                profileButton, stage);
            return;
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 14px;" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(" +
            "gaussian, rgba(0,0,0,0.12)," +
            " 16, 0, 0, 4);");
        card.setPrefWidth(260);

        HBox userRow = new HBox(12);
        userRow.setAlignment(
            Pos.CENTER_LEFT);
        userRow.setPadding(
            new Insets(16, 18, 14, 18));

        Circle bg = new Circle(22);
        bg.setFill(Color.web(
            user.getAvatarColor()));
        Label init = new Label(
            user.getInitials());
        init.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';");
        StackPane avatarPane =
            new StackPane(bg, init);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(
            user.getFullName());
        nameLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;" +
            "-fx-font-family: 'Segoe UI';");
        Label usernameLabel = new Label(
            "@" + user.getUsername());
        usernameLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-family: 'Segoe UI';");
        nameBox.getChildren().addAll(
            nameLabel, usernameLabel);
        userRow.getChildren().addAll(
            avatarPane, nameBox);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle(
            "-fx-background-color: #F0F0F5;");

        VBox menuItems = new VBox(0);
        menuItems.setPadding(new Insets(6));

        Button switchBtn = popupMenuItem(
            "🔄", "Switch User");
        switchBtn.setOnAction(e -> {
            popup.hide();
            Stage stage = (Stage)
                profileButton.getScene()
                    .getWindow();
            UserPickerController picker =
                new UserPickerController();
            boolean changed =
                picker.show(stage);
            if (changed) reloadApp(stage);
        });

        Button settingsBtn = popupMenuItem(
            "⚙️", "Settings & Theme");
        settingsBtn.setOnAction(e -> {
            popup.hide();
            Stage stage = (Stage)
                profileButton.getScene()
                    .getWindow();
            profileMenuCtrl.show(
                profileButton, stage);
        });

        menuItems.getChildren().addAll(
            switchBtn, settingsBtn);

        Region divider2 = new Region();
        divider2.setPrefHeight(1);
        divider2.setStyle(
            "-fx-background-color: #F0F0F5;");
        VBox.setMargin(divider2,
            new Insets(2, 6, 2, 6));

        Label version = new Label(
            "Facere v1.0");
        version.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #BCBCCC;" +
            "-fx-font-family: 'Segoe UI';");
        version.setPadding(
            new Insets(6, 18, 12, 18));

        card.getChildren().addAll(
            userRow, divider, menuItems,
            divider2, version);

        popup.getContent().add(card);

        card.setOpacity(0);
        card.setScaleX(0.95);
        card.setScaleY(0.95);

        javafx.geometry.Bounds bounds =
            profileButton.localToScreen(
                profileButton
                    .getBoundsInLocal());

        popup.show(
            profileButton.getScene()
                .getWindow(),
            bounds.getMaxX() - 260,
            bounds.getMaxY() + 6);

        FadeTransition fade =
            new FadeTransition(
                Duration.millis(150), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale =
            new ScaleTransition(
                Duration.millis(150), card);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(
            Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale)
            .play();
    }

    private Button popupMenuItem(
            String icon, String text) {
        Button btn = new Button(
            icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-padding: 10px 14px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;");
        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-background-color: " +
                "#F3F4F6;" +
                "-fx-text-fill: #1A1A2E;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-padding: 10px 14px;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"));
        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-background-color: " +
                "transparent;" +
                "-fx-text-fill: #374151;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-padding: 10px 14px;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"));
        return btn;
    }

    // ═══════════════════════════════════════
    // RELOAD APP
    // ═══════════════════════════════════════

    private void reloadApp(Stage owner) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(
                    getClass().getResource(
                        "/com/habitflow/fxml/" +
                        "MainView.fxml"));
            BorderPane root = loader.load();
            owner.getScene().setRoot(root);
            MainController newCtrl =
                loader.getController();
            newCtrl.refreshAvatar();
            com.habitflow.service
                .ThemeManager.getInstance()
                .initialize(owner.getScene());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════
    // CLEAR ALL
    // ═══════════════════════════════════════

    @FXML
    private void onClearAll() {
        String sectionName =
            sectionTitle.getText();
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All");
        confirm.setHeaderText(
            "Clear all items in " +
            sectionName + "?");
        confirm.setContentText(
            "This will permanently delete " +
            "all " +
            taskListView.getItems().size() +
            " item(s). This cannot be undone.");
        confirm.showAndWait()
            .ifPresent(response -> {
                if (response ==
                        ButtonType.OK) {
                    switch (currentSection) {
                        case DELETED ->
                            taskDAO
                                .clearDeleted();
                        case COMPLETED ->
                            taskDAO
                                .clearCompleted();
                        case DELAYED ->
                            taskDAO
                                .clearDelayed();
                        default -> {}
                    }
                    loadSection(currentSection,
                        sectionName);
                }
            });
    }

    // ═══════════════════════════════════════
    // ADD HABIT
    // ═══════════════════════════════════════

    @FXML
    private void onAddHabitClicked() {
        Habit newHabit =
            habitsCtrl.showAddHabitDialog();
        if (newHabit != null) {
            habitDAO.save(newHabit);
            loadSection(Task.Section.HABITS,
                "Habits");
        }
    }

    // ═══════════════════════════════════════
    // TASK CARD — optimized animations,
    // full description, direct removal
    // ═══════════════════════════════════════

    private class TaskCardCell
            extends ListCell<Task> {

        private final HBox   card;
        private final Region priorityStrip;
        private final Label  statusDot;
        private final VBox   infoBox;
        private final Label  titleLabel;
        private final Label  descriptionLabel;
        private final Label  dueDateLabel;
        private final Region spacer;
        private final Button btnDone;
        private final Button btnDelayed;
        private final Button btnDelete;

        TaskCardCell() {
            priorityStrip = new Region();
            priorityStrip.setMinHeight(40);
            priorityStrip.getStyleClass()
                .add("priority-normal");

            statusDot = new Label("●");
            statusDot.getStyleClass()
                .add("status-dot-pending");

            titleLabel = new Label();
            titleLabel.getStyleClass()
                .add("task-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(
                Double.MAX_VALUE);

            descriptionLabel = new Label();
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(
                Double.MAX_VALUE);
            descriptionLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #6B7280;" +
                "-fx-padding: 2 0 0 0;");

            dueDateLabel = new Label();
            dueDateLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #9CA3AF;");

            infoBox = new VBox(2,
                titleLabel, descriptionLabel,
                dueDateLabel);
            infoBox.setAlignment(
                Pos.CENTER_LEFT);

            spacer = new Region();
            HBox.setHgrow(spacer,
                Priority.ALWAYS);
            HBox.setHgrow(infoBox,
                Priority.SOMETIMES);

            btnDone = new Button("✓ Done");
            btnDone.getStyleClass()
                .add("btn-done");
            btnDone.setOnAction(
                e -> onMarkDone());

            btnDelayed = new Button(
                "⏳ Later");
            btnDelayed.getStyleClass()
                .add("btn-delayed");
            btnDelayed.setOnAction(
                e -> onMarkDelayed());

            btnDelete = new Button("✕");
            btnDelete.getStyleClass()
                .add("btn-delete");
            btnDelete.setOnAction(
                e -> onDelete());

            card = new HBox(10,
                priorityStrip, statusDot,
                infoBox, spacer,
                btnDone, btnDelayed,
                btnDelete);
            card.setAlignment(
                Pos.CENTER_LEFT);
            card.getStyleClass()
                .add("task-card");
        }

        @Override
        protected void updateItem(Task task,
                boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setGraphic(null);
                setStyle(
                    "-fx-background-color: " +
                    "transparent;");
                return;
            }

            titleLabel.setText(
                task.getTitle());

            String desc =
                task.getDescription();
            if (desc != null &&
                    !desc.isBlank()) {
                descriptionLabel.setText(desc);
                descriptionLabel
                    .setVisible(true);
                descriptionLabel
                    .setManaged(true);
            } else {
                descriptionLabel
                    .setVisible(false);
                descriptionLabel
                    .setManaged(false);
            }

            if (task.getDueDate() != null) {
                dueDateLabel.setText(
                    "📅 " + task.getDueDate()
                        .format(
                            DateTimeFormatter
                            .ofPattern(
                            "MMM d, yyyy")));
                dueDateLabel.setVisible(true);
            } else {
                dueDateLabel.setVisible(false);
            }

            priorityStrip.getStyleClass()
                .removeAll(
                    "priority-low",
                    "priority-normal",
                    "priority-high",
                    "priority-critical");
            switch (task.getPriority()) {
                case LOW ->
                    priorityStrip
                        .getStyleClass()
                        .add("priority-low");
                case HIGH ->
                    priorityStrip
                        .getStyleClass()
                        .add("priority-high");
                case CRITICAL ->
                    priorityStrip
                        .getStyleClass()
                        .add(
                        "priority-critical");
                default ->
                    priorityStrip
                        .getStyleClass()
                        .add(
                        "priority-normal");
            }

            statusDot.getStyleClass()
                .removeAll(
                "status-dot-pending",
                "status-dot-done",
                "status-dot-delayed");
            titleLabel.getStyleClass()
                .removeAll(
                "task-title",
                "task-title-done",
                "task-title-delayed");

            switch (task.getTaskStatus()) {
                case DONE -> {
                    statusDot.getStyleClass()
                        .add(
                        "status-dot-done");
                    titleLabel.getStyleClass()
                        .add(
                        "task-title-done");
                    btnDone.setDisable(true);
                    btnDelayed.setDisable(
                        false);
                }
                case DELAYED -> {
                    statusDot.getStyleClass()
                        .add(
                        "status-dot-delayed");
                    titleLabel.getStyleClass()
                        .add(
                        "task-title-delayed");
                    btnDone.setDisable(false);
                    btnDelayed.setDisable(
                        true);
                }
                default -> {
                    statusDot.getStyleClass()
                        .add(
                        "status-dot-pending");
                    titleLabel.getStyleClass()
                        .add("task-title");
                    btnDone.setDisable(false);
                    btnDelayed.setDisable(
                        false);
                }
            }

            setGraphic(card);
            setStyle(
                "-fx-background-color: " +
                "transparent;" +
                "-fx-padding: 4px 0;");

            // Animate only on section load,
            // not on scroll reuse
            if (animateCards) {
                int idx = getIndex();
                AnimationHelper
                    .slideInFromRight(
                        card, 250,
                        idx * 40.0);
                if (idx >= taskListView
                        .getItems()
                        .size() - 1) {
                    animateCards = false;
                }
            }
        }

        // Direct item removal — no full
        // section reload needed
        private void onMarkDone() {
            Task task = getItem();
            if (task == null) return;
            taskDAO.updateStatus(
                task.getId(),
                Task.TaskStatus.DONE);
            taskListView.getItems()
                .remove(task);
            updateSubtitle();
        }

        private void onMarkDelayed() {
            Task task = getItem();
            if (task == null) return;
            taskDAO.updateStatus(
                task.getId(),
                Task.TaskStatus.DELAYED);
            taskListView.getItems()
                .remove(task);
            updateSubtitle();
        }

        private void onDelete() {
            Task task = getItem();
            if (task == null) return;
            taskDAO.softDelete(task.getId());
            taskListView.getItems()
                .remove(task);
            updateSubtitle();
        }
    }
}