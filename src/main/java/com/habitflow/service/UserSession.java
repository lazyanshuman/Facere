package com.habitflow.service;

import com.habitflow.model.User;

/**
 * UserSession — holds the currently
 * logged-in user for the whole app.
 *
 * Every DAO reads UserSession.getCurrentUserId()
 * so each user only sees their own data.
 */
public class UserSession {

    private static User currentUser;

    public static void setCurrentUser(User u) {
        currentUser = u;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the current user's id, or -1
     * if nobody is logged in yet.
     */
    public static int getCurrentUserId() {
        return currentUser == null
            ? -1
            : currentUser.getId();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}