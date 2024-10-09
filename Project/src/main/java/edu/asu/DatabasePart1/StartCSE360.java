package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.Arrays;

/****
 * <p> StartCSE360 Class </p>
 * 
 * <p> Description: Main class to initialize and start the CSE360 application. </p>
 * 
 * @author Dhruv Bansal, Shreyas Bachiraju, Nirek Shah, Dhruv Shetty, Sonit Penchala
 * 
 * @version 1.00 2024-10-09 Implementation for starting the application
 */

public class StartCSE360 {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    /**
     * Main method to start the application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            printWelcomeBanner();
            databaseHelper.connectToDatabase();  // Connect to the database

            // Check if the database is empty (no users registered)
            if (databaseHelper.isDatabaseEmpty()) {
                System.out.println("-----------------------------------");
                System.out.println("    The H2 Database is empty    ");
                System.out.println("-----------------------------------");
                setupAdministrator();
            }

            UserInterface userInterface = new UserInterface(databaseHelper);
            userInterface.start();

        } catch (SQLException e) {
            System.err.println("-----------------------------------");
            System.err.println("    Database error occurred       ");
            System.err.println("-----------------------------------");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            printGoodbyeBanner();
            databaseHelper.closeConnection();
        }
    }

    /**
     * Sets up the admin account if the database is empty.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void setupAdministrator() throws SQLException {
        System.out.println("-----------------------------------");
        System.out.println("  Setting up Admin Access  ");
        System.out.println("-----------------------------------");
        String username = UserInterface.getInput("Enter Admin Username: ");
        char[] password = UserInterface.getPassword("Enter Admin Password: ");
        char[] confirmPassword = UserInterface.getPassword("Confirm Admin Password: ");

        while (!Arrays.equals(password, confirmPassword)) {
            System.out.println("! Passwords do not match. Please try again.");
            password = UserInterface.getPassword("Enter Admin Password: ");
            confirmPassword = UserInterface.getPassword("Confirm Admin Password: ");
        }

        String role = "admin";

        databaseHelper.register(username, password, role);
        System.out.println("*Admin setup completed.");

        System.out.println("---------------------------------------------------");
        System.out.println(" Admin account created successfully. Please log in ");
        System.out.println("                 to continue.                      ");
        System.out.println("---------------------------------------------------");
    }

    /**
     * Prints a welcome banner.
     */
    private static void printWelcomeBanner() {
        System.out.println("-----------------------------------");
        System.out.println("                                   ");
        System.out.println("    Welcome to EDU ACCESS @ ASU®   ");
        System.out.println("                                   ");
        System.out.println("-----------------------------------");
    }

    /**
     * Prints a goodbye banner.
     */
    private static void printGoodbyeBanner() {
        System.out.println("-----------------------------------");
        System.out.println("                                   ");
        System.out.println("  Thank you for using EDU ACCESS @ ASU®  ");
        System.out.println("           See you soon!           ");
        System.out.println("                                   ");
        System.out.println("-----------------------------------");
    }
}