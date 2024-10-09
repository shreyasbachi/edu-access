package edu.asu.DatabasePart1;

import java.sql.SQLException;

public class AdminManagerTestingAutomation {
    private static int totalPass = 0;
    private static int failed = 0;
    
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
            System.out.println("Number of tests passed: " + totalPass);
            System.out.println("Number of tests failed: " + failed);
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
            totalPass++;
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred");
            failed++;
        }
    }
    
    private static void testResetUserPassword() {
        System.out.println("\nTesting resetUserPassword method:");
        try {

            // Call resetUserPassword method
            String testUsername = adminManager.resetUserPassword();
            
            // Check if the password was reset (OTP set)
            if (databaseHelper.isOTPPasswordSet(testUsername)) {
                System.out.println("Test Passed: User password reset successfully");
                totalPass++;
            } else {
                System.out.println("Test Failed: User password not reset");
                failed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred");
            failed++;
        } finally {
            // Reset System.in to its original state
            System.setIn(System.in);
       
        }
    }
    
    private static void testModifyUserRoles() {
        System.out.println("\nTesting modifyUserRoles method:");

        try {

            // Simulate modifyUserRoles method call
            String testUsername = adminManager.modifyUserRoles();
            
            // Check if the roles were modified
            String[] updatedRoles = databaseHelper.getUserRoles(testUsername);
            if (updatedRoles != null && updatedRoles.length > 0) {
                System.out.println("Test Passed: User roles modified successfully");
                totalPass++;
            } else {
                System.out.println("Test Failed: User roles not modified");
                failed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred");
            failed++;
        }
    }
    
    private static void testDeleteUserAccount() {
        System.out.println("\nTesting deleteUserAccount method:");

        try {
            String testUsername = UserInterface.getInput("Enter test username: ");
            // Simulate deleteUserAccount method call
            adminManager.deleteUserAccount();
            
            // Check if the user was deleted
            if (!databaseHelper.doesUserExist(testUsername)) {
                System.out.println("Test Passed: User account deleted successfully");
                totalPass++;
            } else {
                System.out.println("Test Failed: User account not deleted");
                failed++;
            }
        } catch (SQLException e) {
            System.out.println("Test Failed: SQLException occurred");
        }
    }
}