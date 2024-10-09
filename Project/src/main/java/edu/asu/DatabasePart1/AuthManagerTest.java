package edu.asu.DatabasePart1;

import java.sql.SQLException;

/****
 * <p> AuthManagerTest Class </p>
 * 
 * <p> Description: Handles testing for authentication manager </p>
 * 
 * @author Dhruv Bansal, Shreyas Bachiraju, Nirek Shah, Dhruv Shetty, Sonit Penchala
 * 
 * @version 1.00 2024-10-09 Implementation for database helper
 */

public class AuthManagerTest {

    private static DatabaseHelper databaseHelper;
    private static AuthenticationManager authManager;
    private static final String TEST_USERNAME = "testuser";
    private static final char[] TEST_PASSWORD = "testpassword".toCharArray();

    /**
     * Main method to run all tests.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        setup();
        createTestUser();
        testLogin();
        testGenerateOTP();
        testForgotPassword();
        // cleanup();
        System.out.println("All tests completed.");
    }

    /**
     * Sets up the test environment by initializing DatabaseHelper and AuthenticationManager.
     */
    private static void setup() {
        databaseHelper = new DatabaseHelper();
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
            return;
        }
        authManager = new AuthenticationManager(databaseHelper);
    }

    /**
     * Creates a test user for authentication tests.
     */
    private static void createTestUser() {
        try {
            databaseHelper.register(TEST_USERNAME, TEST_PASSWORD, "student");
            System.out.println("Test user created successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to create test user: " + e.getMessage());
        }
    }

    /**
     * Tests the login functionality of AuthenticationManager.
     */
    private static void testLogin() {
        try {
            boolean result = authManager.login(TEST_USERNAME, TEST_PASSWORD);
            if (result) {
                System.out.println("testLogin passed");
            } else {
                System.out.println("testLogin failed: Login returned false");
            }
        } catch (SQLException e) {
            System.out.println("testLogin failed: " + e.getMessage());
        }
    }

    /**
     * Tests the OTP generation functionality of AuthenticationManager.
     */
    private static void testGenerateOTP() {
        String otp = authManager.generateOTP();
        if (otp != null && otp.length() == 6 ) {
            System.out.println("testGenerateOTP passed");
        } else {
            System.out.println("testGenerateOTP failed: Invalid OTP generated");
        }
    }

    /**
     * Tests the forgot password functionality of AuthenticationManager.
     */
    private static void testForgotPassword() {
        try {
            // Simulate user input for forgot password flow
            authManager.forgotPassword();
            
         
        } catch (Exception e) {
            System.out.println("testForgotPassword failed: " + e.getMessage());
        } finally {
            // Reset System.in
            System.setIn(System.in);
        }
    }

   
}