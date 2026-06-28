package com.habitflow.controller;

import com.habitflow.service.SecurityManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * SecuritySetupController
 *
 * Shows the PIN setup dialog.
 * Opened from the Me menu.
 */
public class SecuritySetupController {

    private final SecurityManager security =
        SecurityManager.getInstance();

    /**
     * Shows the Set Up PIN dialog.
     */
    public void showSetupDialog() {

        Dialog<Boolean> dialog =
            new Dialog<>();
        dialog.setTitle("Set Up PIN Lock");
        com.habitflow.util.AppIcon.set(dialog);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        ButtonType saveBtn = new ButtonType(
            "🔐  Save PIN",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(saveBtn,
                ButtonType.CANCEL);

        // ── Form ─────────────────────────────
        VBox form = new VBox(16);
        form.setPadding(
            new Insets(24, 28, 8, 28));
        form.setStyle(
            "-fx-background-color: white;");
        form.setPrefWidth(380);

        Label title = new Label(
            "🔐  Set Up PIN Lock");
        title.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;");

        Label desc = new Label(
            "Your PIN will be required every " +
            "time you open Facere.\n" +
            "Choose exactly 6 digits for" +
            " your PIN.");

        desc.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #6B7280;");
        desc.setWrapText(true);

        // PIN field
        Label pinLabel = new Label("New PIN");
        pinLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;");
        PasswordField pinField =
            new PasswordField();
        pinField.setPromptText(
            "Enter exactly 6 digits");
        pinField.setStyle(inputStyle());

        // Confirm PIN field
        Label confirmLabel =
            new Label("Confirm PIN");
        confirmLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;");
        PasswordField confirmField =
            new PasswordField();
        confirmField.setPromptText(
            "Re-enter your 6-digit PIN");
        confirmField.setStyle(inputStyle());


        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle(
            "-fx-text-fill: #EF4444;" +
            "-fx-font-size: 12px;");
        errorLabel.setVisible(false);

        form.getChildren().addAll(
            title, desc,
            new VBox(6, pinLabel, pinField),
            new VBox(6, confirmLabel,
                confirmField),
            errorLabel);

        dialog.getDialogPane()
            .setContent(form);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white;");

        // Style save button
        Button saveBtnNode = (Button) dialog
            .getDialogPane()
            .lookupButton(saveBtn);
        saveBtnNode.setStyle(
            "-fx-background-color: #6C63FF;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 24px;");

        pinField.requestFocus();

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;

            String pin =
                pinField.getText().trim();
            String confirm =
                confirmField.getText().trim();

            // Validate
            if (pin.length() != 6) {
                errorLabel.setText(
                    "PIN must be exactly " +
                    "6 digits.");
                errorLabel.setVisible(true);
                return null;
            }

            if (!pin.equals(confirm)) {
                errorLabel.setText(
                    "PINs do not match. " +
                    "Please try again.");
                errorLabel.setVisible(true);
                return null;
            }

            boolean saved =
                security.setupPin(pin);

            if (saved) {

                return true;
            }

            return null;
        });

        dialog.showAndWait()
            .ifPresent(success -> {
                if (success) {
                    showSuccessAlert();
                }
            });
    }

    /**
     * Shows the Change PIN dialog.
     */
    public void showChangePinDialog() {

        Dialog<Boolean> dialog =
            new Dialog<>();
        dialog.setTitle("Change PIN");
        com.habitflow.util.AppIcon.set(dialog);
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType(
            "🔐  Change PIN",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(saveBtn,
                ButtonType.CANCEL);

        VBox form = new VBox(16);
        form.setPadding(
            new Insets(24, 28, 8, 28));
        form.setStyle(
            "-fx-background-color: white;");
        form.setPrefWidth(380);

        Label title = new Label(
            "🔄  Change PIN");
        title.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;");

        PasswordField oldPinField =
            new PasswordField();
        oldPinField.setPromptText(
            "Current PIN");
        oldPinField.setStyle(inputStyle());

        PasswordField newPinField =
            new PasswordField();
        newPinField.setPromptText(
            "New PIN (4-6 digits)");
        newPinField.setStyle(inputStyle());

        PasswordField confirmField =
            new PasswordField();
        confirmField.setPromptText(
            "Confirm new PIN");
        confirmField.setStyle(inputStyle());

        Label errorLabel = new Label("");
        errorLabel.setStyle(
            "-fx-text-fill: #EF4444;" +
            "-fx-font-size: 12px;");
        errorLabel.setVisible(false);

        form.getChildren().addAll(
            title,
            new VBox(6,
                new Label("Current PIN"),
                oldPinField),
            new VBox(6,
                new Label("New PIN"),
                newPinField),
            new VBox(6,
                new Label("Confirm New PIN"),
                confirmField),
            errorLabel);

        dialog.getDialogPane()
            .setContent(form);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white;");

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;

            String oldPin =
                oldPinField.getText();
            String newPin =
                newPinField.getText();
            String confirm =
                confirmField.getText();

if (newPin.length() != 6) {
                errorLabel.setText(
                    "New PIN must be exactly " +
                    "6 digits.");
                errorLabel.setVisible(true);
                return null;
            }

            if (!newPin.equals(confirm)) {
                errorLabel.setText(
                    "New PINs do not match.");
                errorLabel.setVisible(true);
                return null;
            }

            boolean changed =
                security.changePin(
                    oldPin, newPin);
            if (!changed) {
                errorLabel.setText(
                    "Current PIN is incorrect.");
                errorLabel.setVisible(true);
                return null;
            }

            return true;
        });

        dialog.showAndWait()
            .ifPresent(success -> {
                if (success) {
                    showSuccessAlert();
                }
            });
    }

    /**
     * Shows remove PIN confirmation.
     */
    public void showRemovePinDialog() {
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove PIN Lock");
        confirm.setHeaderText(
            "Remove PIN Lock?");
        confirm.setContentText(
            "This will disable the lock " +
            "screen. Anyone with access to " +
           "your PC can open Facere.\n\n" +
            "Are you sure?");

        confirm.showAndWait()
            .ifPresent(r -> {
                if (r == ButtonType.OK) {
                    security.removePin();
                    Alert done = new Alert(
                        Alert.AlertType
                            .INFORMATION);
                    done.setTitle("Done");
                    done.setHeaderText(null);
                    done.setContentText(
                        "PIN lock removed.\n" +
                        "Facere will open " +
                        "without a PIN.");
                    done.showAndWait();
                }
            });
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(
            Alert.AlertType.INFORMATION);
        alert.setTitle("PIN Set Up");
        alert.setHeaderText(null);
        alert.setContentText(
            "✅ PIN set up successfully!\n\n" +
            "Facere will ask for your PIN " +
            "every time it opens.");
        alert.showAndWait();
    }

    private String inputStyle() {
        return
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #111827;";
    }
}