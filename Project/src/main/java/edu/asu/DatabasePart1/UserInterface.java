package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.Scanner;

/****
 * <p> UserInterface Class </p>
 * 
 * <p> Description: Handles the user interface and interaction flow for the application. </p>
 * 
 * @author Dhruv Bansal
 * 
 * @version 1.00 2024-10-09 Implementation for user interface
 */

public class UserInterface {
    private static final Scanner scanner = new Scanner(System.in);
    private final UserManager userManager;
    private final AdminManager adminManager;

    /**
     * Constructs a UserInterface with the given DatabaseHelper.
     * 
     * @param databaseHelper The DatabaseHelper instance to use
     */
    public UserInterface(DatabaseHelper databaseHelper) {
        this.userManager = new UserManager(databaseHelper);
        this.adminManager = new AdminManager(databaseHelper);
    }

    /**
     * Starts the main application loop, presenting options to the user.
     * 
     * @throws SQLException if a database access error occurs
     */
    public void start() throws SQLException {
        String choice;
        do {
            System.out.println("\n+----------------------------------+");
            System.out.println("|        EDU ACCESS @ ASU®         |");
            System.out.println("+----------------------------------+");
            System.out.println("| Select an option:                |");
            System.out.println("| U - User Login/Registration      |");
            System.out.println("| A - Admin Access                 |");
            System.out.println("| Q - Quit System                  |");
            System.out.println("+----------------------------------+");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "U":
                    System.out.println("\n--- Entering User Flow ---");
                    userManager.userFlow();
                    break;
                case "A":
                    System.out.println("\n--- Entering Admin Flow ---");
                    adminManager.adminFlow();
                    break;
                case "Q":
                    System.out.println("\n+----------------------------------+");
                    System.out.println("|     Exiting EDU ACCESS SYSTEM    |");
                    System.out.println("+----------------------------------+");
                    break;
                default:
                    System.out.println("\n! Invalid choice. Please try again.");
            }
        } while (!choice.equals("Q"));
    }

    /**
     * Gets input from the user with a given prompt.
     * 
     * @param prompt The prompt to display to the user
     * @return The user's input as a String
     */
    public static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Gets a password input from the user, masking the input.
     * 
     * @param prompt The prompt to display to the user
     * @return The user's password as a char array
     */
    public static char[] getPassword(String prompt) {
        System.out.print(prompt);
        return System.console().readPassword();
    }

    /**
     * Gets required input from the user, ensuring it's not empty.
     * 
     * @param prompt The prompt to display to the user
     * @return The user's non-empty input as a String
     */
    public static String getRequiredInput(String prompt) {
        String input;
        do {
            input = getInput(prompt);
            if (input.trim().isEmpty()) {
                System.out.println("This field is required. Please enter a value.");
            }
        } while (input.trim().isEmpty());
        return input;
    }
}