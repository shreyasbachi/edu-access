package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.Arrays;

public class UserManager {
    private final DatabaseHelper databaseHelper;
    private final AuthenticationManager authManager;

    public UserManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.authManager = new AuthenticationManager(databaseHelper);
    }

    public void userFlow() throws SQLException {
        System.out.println("User flow");
        System.out.print("What would you like to do? 1.Register 2.Login 3.Forgot Password: ");
        String choice = UserInterface.getInput("");
        switch(choice) {
            case "1": 
                registerUser();
                break;
            case "2":
                loginUser();
                break;
            case "3":
                authManager.forgotPassword();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void registerUser() throws SQLException {
        String invitationCode = UserInterface.getInput("Enter invitation code: ");
        String roles = databaseHelper.getInvitationRoles(invitationCode);
        
        if (roles == null) {
            System.out.println("Invalid or used invitation code. Registration failed.");
            return;
        }

        String username = UserInterface.getInput("Enter Username: ");
        char[] password = UserInterface.getPassword("Enter Password: ");
        char[] confirmPassword = UserInterface.getPassword("Confirm Password: ");

        if (Arrays.equals(password, confirmPassword)) {
            databaseHelper.register(username, password, roles);
            databaseHelper.markInvitationAsUsed(invitationCode);
            System.out.println("Registration successful. Please log in to set up your profile.");
            System.out.println("Returning to main screen...");
        } else {
            System.out.println("Passwords do not match. Registration failed.");
        }
    }

    private void loginUser() throws SQLException {
        String username = UserInterface.getInput("Enter Username: ");
        
        if (databaseHelper.isOTPPasswordSet(username)) {
            authManager.handleOTPLogin(username);
            return;
        } else {
            char[] password = UserInterface.getPassword("Enter Password: ");
            if (authManager.login(username, password)) {
                System.out.println("Login successful.");
                if (!databaseHelper.isProfileComplete(username)) {
                    authManager.setupProfile(username);
                }
                String[] roles = databaseHelper.getUserRoles(username);
                if (roles.length > 1) {
                    chooseRoleFlow(username, roles);
                } else if (roles.length == 1) {
                    userSession(username, roles[0]);
                } else {
                    System.out.println("No roles assigned to this user. Please contact an administrator.");
                }
            } else {
                System.out.println("Invalid credentials. Try again!!");
            }
        }
    }

    private void chooseRoleFlow(String username, String[] roles) throws SQLException {
        while (true) {
            System.out.println("You have multiple roles. Please choose a role:");
            for (int i = 0; i < roles.length; i++) {
                System.out.println((i + 1) + ". " + roles[i]);
            }
            System.out.print("Enter your choice (or 0 to go back): ");
            int choice = Integer.parseInt(UserInterface.getInput(""));
            
            if (choice == 0) {
                System.out.println("Returning to main menu...");
                return;
            } else if (choice > 0 && choice <= roles.length) {
                userSession(username, roles[choice - 1]);
                System.out.print("Do you want to switch to another role? (Y/N): ");
                String switchRole = UserInterface.getInput("").trim().toUpperCase();
                if (!switchRole.equals("Y")) {
                    return;
                }
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void userSession(String username, String role) throws SQLException {
        String choice;
        do {
            System.out.println("\n" + role.substring(0, 1).toUpperCase() + role.substring(1) + " Menu:");
            if (role.equalsIgnoreCase("student")) {
                System.out.println("1 - View Courses");
                System.out.println("2 - Submit Assignment");
            } else if (role.equalsIgnoreCase("instructor")) {
                System.out.println("1 - Manage Courses");
                System.out.println("2 - Grade Assignments");
            } else if (role.equalsIgnoreCase("admin")) {
                new AdminManager(databaseHelper).adminFlow();
                return;
            }
            System.out.println("Q - Logout");
            System.out.print("Enter your choice: ");
            choice = UserInterface.getInput("").toUpperCase();

            switch (choice) {
                case "1":
                case "2":
                    System.out.println("This feature is not implemented yet.");
                    break;
                case "Q":
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("Q"));
    }
}