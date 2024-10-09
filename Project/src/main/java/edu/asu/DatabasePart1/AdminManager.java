package edu.asu.DatabasePart1;
import java.sql.SQLException;
import java.util.UUID;

/****
 * <p> AdminManager Class </p>
 * 
 * <p> Description: Handles all the use cases for a user with the Admin role. </p>
 * 
 * @author Dhruv Bansal, Shreyas Bachiraju, Nirek Shah, Dhruv Shetty, Sonit Penchala
 * 
 * @version 1.00 2024-10-09 Implementation for admin manager
 */

public class AdminManager {
    private final DatabaseHelper databaseHelper;
    private final AuthenticationManager authManager;

    /**
     * Constructs an AdminManager with the given DatabaseHelper.
     * 
     * @param databaseHelper The DatabaseHelper instance to use
     */
    public AdminManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.authManager = new AuthenticationManager(databaseHelper);
    }

    /**
     * Admin login flow and initiates the admin session if successful.
     */
    
    public void adminFlow() throws SQLException {
        System.out.println("-------------------------------------");
        System.out.println("Welcome to the Admin Flow!");
        System.out.println("-------------------------------------");
        String username = UserInterface.getInput("Enter Admin Username: ");
        char[] password = UserInterface.getPassword("Enter Admin Password: ");
        if (authManager.login(username, password)) {
            System.out.println("-------------------------------------");
            System.out.println("Admin login successful.");
            System.out.println("-------------------------------------");
            if (!databaseHelper.isProfileComplete(username)) {
                authManager.setupProfile(username);
            }
            adminSession();
        } else {
            System.out.println("-------------------------------------");
            System.out.println("Invalid admin credentials. Try again.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Admin session, presenting a menu of admin actions.
     */
    private void adminSession() throws SQLException {
        String choice;
        do {
            System.out.println("\n-------------------------------------");
            System.out.println("Admin Menu:");
            System.out.println("-------------------------------------");
            System.out.println("I - Invite user");
            System.out.println("R - Reset user password");
            System.out.println("D - Delete user account");
            System.out.println("L - List user accounts");
            System.out.println("M - Modify user roles");
            System.out.println("Q - Logout");
            System.out.println("-------------------------------------");
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
                    System.out.println("-------------------------------------");
                    System.out.println("Securely logging you out.");
                    System.out.println("-------------------------------------");
                    break;
                default:
                    System.out.println("-------------------------------------");
                    System.out.println("Invalid choice. Please try again.");
                    System.out.println("-------------------------------------");
            }
        } 
        while (!choice.equals("Q"));
    }

    /**
     * Handles the process of inviting a new user by generating a random code.
     */
    public void inviteUser() throws SQLException {
        String invitationCode = generateInvitationCode();
        System.out.println("-------------------------------------");
        System.out.println("Send this invitation code to the user: " + invitationCode);
        System.out.println("-------------------------------------");
        String roles = UserInterface.getInput("Enter roles for the invited user (comma-separated): ");
        databaseHelper.createInvitation(invitationCode, roles);
        System.out.println("-------------------------------------");
        System.out.println("Code generated successfully.");
        System.out.println("-------------------------------------");
    }

    /**
     * Resets a user's password by generating an OTP.
     */
    public void resetUserPassword() throws SQLException {
        String username = UserInterface.getInput("Enter user username to reset password: ");
        if (databaseHelper.doesUserExist(username)) {
            String otp = authManager.generateOTP();
            databaseHelper.resetUserPasswordByAdmin(username, otp);
            System.out.println("-------------------------------------");
            System.out.println("Password reset. OTP: " + otp);
            System.out.println("-------------------------------------");
        } else {
            System.out.println("-------------------------------------");
            System.out.println("User not found.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Modifies the roles.
     */
    public void modifyUserRoles() throws SQLException {
        String username = UserInterface.getInput("Enter user username to modify roles: ");
        if (databaseHelper.doesUserExist(username)) {
            System.out.println("Possible roles are: Admin, Instructor or Student.");
            String newRoles = UserInterface.getInput("Enter new roles (comma-separated): ");
            databaseHelper.updateUserRoles(username, newRoles);
            System.out.println("-------------------------------------");
            System.out.println("User roles updated.");
            System.out.println("-------------------------------------");
        } else {
            System.out.println("-------------------------------------");
            System.out.println("User not found.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Deletes a user account.
     */
    public void deleteUserAccount() throws SQLException {
        String username = UserInterface.getInput("Enter a user's username to delete: ");
        if (databaseHelper.doesUserExist(username)) {
            String confirm = UserInterface.getInput("Are you sure you want to delete this user? (Yes/No): ");
            if (confirm.equalsIgnoreCase("Yes")) {
                databaseHelper.deleteUser(username);
                System.out.println("-------------------------------------");
                System.out.println("User account deleted successfully.");
                System.out.println("-------------------------------------");
            } else {
                System.out.println("-------------------------------------");
                System.out.println("Deletion stopped.");
                System.out.println("-------------------------------------");
            }
        } else {
            System.out.println("-------------------------------------");
            System.out.println("User not found in the database.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Generates a unique invitation code.
     * 
     * @return A string representing the generated invitation code
     */
    private String generateInvitationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}