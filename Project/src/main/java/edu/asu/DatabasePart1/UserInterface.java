package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.Scanner;

public class UserInterface {
    private static final Scanner scanner = new Scanner(System.in);
    private final UserManager userManager;
    private final AdminManager adminManager;
    private final AuthenticationManager authManager;

    public UserInterface(DatabaseHelper databaseHelper) {
        this.userManager = new UserManager(databaseHelper);
        this.adminManager = new AdminManager(databaseHelper);
        this.authManager = new AuthenticationManager(databaseHelper);
    }

    public void start() throws SQLException {
        String choice;
        do {
            System.out.println("\nSelect an option:");
            System.out.println("U - User");
            System.out.println("A - Administrator");
            System.out.println("Q - Quit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "U":
                    userManager.userFlow();
                    break;
                case "A":
                    adminManager.adminFlow();
                    break;
                case "Q":
                    System.out.println("Exiting the program...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("Q"));
    }

    public static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static char[] getPassword(String prompt) {
        System.out.print(prompt);
        return System.console().readPassword();
    }

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