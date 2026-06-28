package com.habitflow.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * AppIcon — sets the Facere icon on
 * any Stage or Dialog window.
 */
public class AppIcon {

    private static Image cachedIcon;

    /**
     * Sets the Facere icon on a Stage.
     */
    public static void set(Stage stage) {
        if (stage == null) return;
        try {
            if (cachedIcon == null) {
                var stream =
                    AppIcon.class
                        .getResourceAsStream(
                        "/com/habitflow/" +
                        "images/" +
                        "facere_icon_512.png");
                if (stream != null) {
                    cachedIcon =
                        new Image(stream);
                }
            }
            if (cachedIcon != null) {
                stage.getIcons().clear();
                stage.getIcons()
                    .add(cachedIcon);
            }
        } catch (Exception e) {
            // icon not found — skip
        }
    }

    /**
     * Sets the Facere icon on a Dialog
     * by accessing its underlying Stage.
     * Call AFTER dialog.getDialogPane()
     * is configured.
     */
    public static void set(
            javafx.scene.control.Dialog<?>
                dialog) {
        try {
            dialog.setOnShown(e -> {
                Stage stage = (Stage)
                    dialog.getDialogPane()
                        .getScene()
                        .getWindow();
                set(stage);
            });
        } catch (Exception e) {
            // skip
        }
    }
}