package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.UUID;

public class AdminManager {
    private final DatabaseHelper databaseHelper;
    private final AuthenticationManager authManager;

    public AdminManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.authManager = new AuthenticationManager(databaseHelper);
    }

    public void adminFlow() throws SQLException {
        System.out.println("Admin flow");
        String username = UserInterface.getInput("Enter Admin Username: ");
        char[] password = UserInterface.getPassword("Enter Admin Password: ");
        if (authManager.login(username, password)) {
            System.out.println("Admin login successful.");
            if (!databaseHelper.isProfileComplete(username)) {
                System.out.println("Your profile is not complete. Let's set it up now.");
                authManager.setupProfile(username);
            }
            adminSession();
        } else {
            System.out.println("Invalid admin credentials. Try again!!");
        }
    }

    private void adminSession() throws SQLException {
        String choice;
        do {
            System.out.println("\nAdmin Menu:");
            System.out.println("I - Invite user");
            System.out.println("R - Reset user password");
            System.out.println("D - Delete user account");
            System.out.println("L - List user accounts");
            System.out.println("M - Modify user roles");
            System.out.println("Q - Logout");
            System.out.print("Enter your choice: ");
            choice = UserInterface.getInput("").toUpperCase();

            switch (choice) {
                case "I":
                    inviteUser();
                    break;
                case "R":
                    resetUserPassword();
                    break;
                case "D":
                    deleteUserAccount();
                    break;
                case "L":
                    databaseHelper.displayUsersByAdmin();
                    break;
                case "M":
                    modifyUserRoles();
                    break;
                case "Q":
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("Q"));
    }

    private void inviteUser() throws SQLException {
        String invitationCode = generateInvitationCode();
        System.out.println("Generated invitation code: " + invitationCode);
        String roles = UserInterface.getInput("Enter roles for the invited user (comma-separated): ");
        databaseHelper.createInvitation(invitationCode, roles);
        System.out.println("Invitation created successfully.");
    }

    private void resetUserPassword() throws SQLException {
        String username = UserInterface.getInput("Enter user username to reset password: ");
        if (databaseHelper.doesUserExist(username)) {
            String otp = authManager.generateOTP();
            databaseHelper.resetUserPasswordByAdmin(username, otp);
            System.out.println("Password reset. One-time password: " + otp);
            System.out.println("User will be prompted to change password on next login.");
        } else {
            System.out.println("User not found.");
        }
    }

    private void modifyUserRoles() throws SQLException {
        String username = UserInterface.getInput("Enter user username to modify roles: ");
        if (databaseHelper.doesUserExist(username)) {
            String newRoles = UserInterface.getInput("Enter new roles (comma-separated): ");
            databaseHelper.updateUserRoles(username, newRoles);
            System.out.println("User roles updated.");
        } else {
            System.out.println("User not found.");
        }
    }

    private void deleteUserAccount() throws SQLException {
        String username = UserInterface.getInput("Enter user username to delete: ");
        if (databaseHelper.doesUserExist(username)) {
            String confirm = UserInterface.getInput("Are you sure you want to delete this user? (Yes/No): ");
            if (confirm.equalsIgnoreCase("Yes")) {
                databaseHelper.deleteUser(username);
                System.out.println("User account deleted.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("User not found.");
        }
    }

    private String generateInvitationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}