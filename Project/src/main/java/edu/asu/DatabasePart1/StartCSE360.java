package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.Arrays;

public class StartCSE360 {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    public static void main(String[] args) {
        try {
            databaseHelper.connectToDatabase();  // Connect to the database

            // Check if the database is empty (no users registered)
            if (databaseHelper.isDatabaseEmpty()) {
                System.out.println("In-Memory Database is empty");
                setupAdministrator();
            }

            UserInterface userInterface = new UserInterface(databaseHelper);
            userInterface.start();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Good Bye!!");
            databaseHelper.closeConnection();
        }
    }

    private static void setupAdministrator() throws SQLException {
        System.out.println("Setting up the Administrator access.");
        String username = UserInterface.getInput("Enter Admin Username: ");
        char[] password = UserInterface.getPassword("Enter Admin Password: ");
        char[] confirmPassword = UserInterface.getPassword("Confirm Admin Password: ");

        while (!Arrays.equals(password, confirmPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            password = UserInterface.getPassword("Enter Admin Password: ");
            confirmPassword = UserInterface.getPassword("Confirm Admin Password: ");
        }

        String role = "admin";

        databaseHelper.register(username, password, role);
        System.out.println("Administrator setup completed.");

        System.out.println("Admin account created successfully. Please log in to continue.");
    }
}