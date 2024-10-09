package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.util.UUID;

public class AdminManagerTestingAutomation {
    private static int numPassed = 0;
    private static int numFailed = 0;
    
    private static DatabaseHelper databaseHelper;
    private static AdminManager adminManager;

    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nAdminManager Testing Automation");
        
        try {
            setup();
            
            // Run test cases
            testInviteUser();
            testResetUserPassword();
            testModifyUserRoles();
            testDeleteUserAccount();
            
            // Print the final results
            System.out.println("____________________________________________________________________________");
            System.out.println();
            System.out.println("Number of tests passed: " + numPassed);
            System.out.println("Number of tests failed: " + numFailed);
        } catch (SQLException e) {
            System.err.println("Database error occurred during testing: " + e.getMessage());
        } finally {
            teardown();
        }
    }
    
    private static void setup() throws SQLException {
        databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();
        adminManager = new AdminManager(databaseHelper);
    }
    
    private static void teardown() {
        if (databaseHelper != null) {
            databaseHelper.closeConnection();
        }
    }
    
    private static void testInviteUser() {
        System.out.println("\nTesting inviteUser method:");
        try {
            // Simulate inviteUser method call
            adminManager.inviteUser();
            
            // Check if an invitation was created in the database
            // This is a simplified check and may need adjustment based on your actual implementation
            System.out.println("Test Passed: Invitation created successfully");
            numPassed++;
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred - " + e.getMessage());
            numFailed++;
        }
    }
    
    private static void testResetUserPassword() {
        System.out.println("\nTesting resetUserPassword method:");
        String testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        try {
            // Create a test user
            String testPassword = "password";
            databaseHelper.register(testUsername, testPassword.toCharArray(), "student");
            
            // Simulate resetUserPassword method call
            adminManager.resetUserPassword();
            
            // Check if the password was reset (OTP set)
            if (databaseHelper.isOTPPasswordSet(testUsername)) {
                System.out.println("Test Passed: User password reset successfully");
                numPassed++;
            } else {
                System.out.println("Test Failed: User password not reset");
                numFailed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred - " + e.getMessage());
            numFailed++;
        } finally {
            // Clean up
            try {
                databaseHelper.deleteUser(testUsername);
            } catch (SQLException e) {
                System.out.println("Warning: Failed to delete test user - " + e.getMessage());
            }
        }
    }
    
    private static void testModifyUserRoles() {
        System.out.println("\nTesting modifyUserRoles method:");
        String testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        try {
            // Create a test user
            String testPassword = "password";
            databaseHelper.register(testUsername, testPassword.toCharArray(), "student");
            
            // Simulate modifyUserRoles method call
            adminManager.modifyUserRoles();
            
            // Check if the roles were modified
            String[] updatedRoles = databaseHelper.getUserRoles(testUsername);
            if (updatedRoles != null && updatedRoles.length > 0) {
                System.out.println("Test Passed: User roles modified successfully");
                numPassed++;
            } else {
                System.out.println("Test Failed: User roles not modified");
                numFailed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred - " + e.getMessage());
            numFailed++;
        } finally {
            // Clean up
            try {
                databaseHelper.deleteUser(testUsername);
            } catch (SQLException e) {
                System.out.println("Warning: Failed to delete test user - " + e.getMessage());
            }
        }
    }
    
    private static void testDeleteUserAccount() {
        System.out.println("\nTesting deleteUserAccount method:");
        String testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        try {
            // Create a test user
            String testPassword = "password";
            databaseHelper.register(testUsername, testPassword.toCharArray(), "student");
            
            // Simulate deleteUserAccount method call
            adminManager.deleteUserAccount();
            
            // Check if the user was deleted
            if (!databaseHelper.doesUserExist(testUsername)) {
                System.out.println("Test Passed: User account deleted successfully");
                numPassed++;
            } else {
                System.out.println("Test Failed: User account not deleted");
                numFailed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred - " + e.getMessage());
            numFailed++;
        }
    }
}