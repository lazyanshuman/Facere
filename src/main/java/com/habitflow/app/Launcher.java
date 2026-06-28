package com.habitflow.app;

import javafx.application.Application;

/**
 * Launcher — this is the STARTING POINT of the entire app.
 * It is a plain class (not extending Application) so the
 * fat JAR works correctly without any extra configuration.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HabitFlowApp.class, args);
    }
}