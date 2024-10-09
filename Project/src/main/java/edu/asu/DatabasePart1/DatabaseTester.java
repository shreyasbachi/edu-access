package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * <p> DatabaseTester Class </p>
 * 
 * <p> Description: Provides comprehensive testing for the DatabaseHelper class functionalities. </p>
 * 
 * @author Dhruv Bansal, Shreyas Bachiraju, Nirek Shah, Dhruv Shetty, Sonit Penchala
 * 
 * @version 1.00 2024-10-09 Implementation for database testing
 */
public class DatabaseTester {

    private static DatabaseHelper dbHelper;
    private static int passedTests = 0;
    private static int failedTests = 0;

    /**
     * Main method to run all database tests.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        dbHelper = new DatabaseHelper();
        
        try {
            dbHelper.connectToDatabase();
            
            testRegisterAndLogin();
            testCreateAndVerifyInvitation();
            testResetPassword();
            testUpdateProfile();
            testUserExistence();
            testAdminFunctions();
            
            System.out.println("Test Summary:");
            System.out.println("Passed tests: " + passedTests);
            System.out.println("Failed tests: " + failedTests);
            System.out.println("Total tests: " + (passedTests + failedTests));
        } catch (SQLException e) {
            System.err.println("Test failed due to SQL exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbHelper.closeConnection();
        }
    }

    /**
     * Tests user registration and login functionality.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testRegisterAndLogin() throws SQLException {
        String username = "Sonit";
        char[] password = "sonit123".toCharArray();
        String roles = "user";

        try {
            dbHelper.register(username, password, roles);
            System.out.println("User registered successfully.");
            passedTests++;

            boolean loginSuccess = dbHelper.login(username, password);
            assert loginSuccess : "Login failed for registered user";
            System.out.println("Login successful.");
            passedTests++;

            char[] wrongPassword = "wrongpassword".toCharArray();
            boolean loginFail = dbHelper.login(username, wrongPassword);
            assert !loginFail : "Login succeeded with incorrect password";
            System.out.println("Incorrect login test passed.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        } finally {
            dbHelper.deleteUser(username);
            System.out.println("User deleted successfully.");
        }
    }

    /**
     * Tests invitation creation and verification.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testCreateAndVerifyInvitation() throws SQLException {
        String invitationCode = "SHREYAS123";
        String roles = "user,editor";

        try {
            dbHelper.createInvitation(invitationCode, roles);
            System.out.println("Invitation created successfully.");
            passedTests++;

            String retrievedRoles = dbHelper.getInvitationRoles(invitationCode);
            assert roles.equals(retrievedRoles) : "Retrieved roles do not match";
            System.out.println("Invitation verification successful.");
            passedTests++;

            dbHelper.markInvitationAsUsed(invitationCode);
            String usedInvitationRoles = dbHelper.getInvitationRoles(invitationCode);
            assert usedInvitationRoles == null : "Used invitation should return null roles";
            System.out.println("Marking invitation as used successful.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        }
    }

    /**
     * Tests password reset functionality.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testResetPassword() throws SQLException {
        String username = "Dhruv";
        char[] password = "dhruv123".toCharArray();
        String roles = "user";

        try {
            dbHelper.register(username, password, roles);

            String otp = "654321";
            Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + 3600000); // 1 hour from now
            dbHelper.resetUserPassword(username, otp, expirationTime);

            boolean isOtpSet = dbHelper.isOTPPasswordSet(username);
            assert isOtpSet : "OTP password should be set";
            System.out.println("Password reset and OTP set successfully.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        } finally {
            dbHelper.deleteUser(username);
        }
    }

    /**
     * Tests profile update functionality.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testUpdateProfile() throws SQLException {
        String username = "Shreyas";
        char[] password = "shreyas123".toCharArray();
        String roles = "user";

        try {
            dbHelper.register(username, password, roles);

            String email = "shreyas@test.com";
            String firstName = "Shreyas";
            String middleName = "Middle";
            String lastName = "Bachiraju";
            String preferredName = "Shre";
            dbHelper.updateProfile(username, email, firstName, middleName, lastName, preferredName);

            boolean isComplete = dbHelper.isProfileComplete(username);
            assert isComplete : "Profile should be marked as complete";
            System.out.println("Profile update successful.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        } finally {
            dbHelper.deleteUser(username);
        }
    }

    /**
     * Tests user existence checking functionality.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testUserExistence() throws SQLException {
        String username = "Sonit";
        char[] password = "sonit123".toCharArray();
        String roles = "user";

        try {
            dbHelper.register(username, password, roles);

            boolean exists = dbHelper.doesUserExist(username);
            assert exists : "User should exist";
            System.out.println("User existence check successful.");
            passedTests++;

            boolean notExists = dbHelper.doesUserExist("nonexistentuser");
            assert !notExists : "Non-existent user should not exist";
            System.out.println("Non-existent user check successful.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        } finally {
            dbHelper.deleteUser(username);
        }
    }

    /**
     * Tests admin-specific functions.
     * 
     * @throws SQLException if a database access error occurs
     */
    private static void testAdminFunctions() throws SQLException {
        String adminUsername = "DhruvAdmin";
        char[] adminPassword = "dhruv123".toCharArray();
        String adminRoles = "admin";

        try {
            dbHelper.register(adminUsername, adminPassword, adminRoles);

            boolean isAdmin = dbHelper.isUserAdmin(adminUsername);
            assert isAdmin : "User should be admin";
            System.out.println("Admin check successful.");
            passedTests++;

            String newRoles = "admin,superuser";
            dbHelper.updateUserRoles(adminUsername, newRoles);
            String[] updatedRoles = dbHelper.getUserRoles(adminUsername);
            assert Arrays.equals(updatedRoles, newRoles.split(",")) : "Roles update failed";
            System.out.println("User roles update successful.");
            passedTests++;
        } catch (AssertionError e) {
            System.out.println("Test failed: " + e.getMessage());
            failedTests++;
        } finally {
            dbHelper.deleteUser(adminUsername);
        }
    }
}