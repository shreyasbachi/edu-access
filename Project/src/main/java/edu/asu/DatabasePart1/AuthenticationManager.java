package edu.asu.DatabasePart1;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;

/****
 * <p> AuthenticationManager Class </p>
 * 
 * <p> Description: Handles all authentication-related operations for users. </p>
 * 
 * @author Dhruv Bansal
 * 
 * @version 1.00 2024-10-09 Implementation for authentication manager
 */

public class AuthenticationManager {
    private final DatabaseHelper databaseHelper;

    /**
     * Constructs an AuthenticationManager with the given DatabaseHelper.
     * 
     * @param databaseHelper The DatabaseHelper instance to use
     */
    public AuthenticationManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * 
     * @param username The username of the user
     * @param password The password of the user
     * @return true if login is successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean login(String username, char[] password) throws SQLException {
        return databaseHelper.login(username, password);
    }

    /**
     * Sets up the user's profile with additional information.
     * 
     * @param username The username of the user
     * @throws SQLException if a database error occurs
     */
    
    public void setupProfile(String username) throws SQLException {
        System.out.println("-------------------------------------");
        System.out.println("Let's set up your profile now.");
        System.out.println("-------------------------------------");
        String email = UserInterface.getRequiredInput("Enter Email: ");
        String firstName = UserInterface.getRequiredInput("Enter First Name: ");
        String middleName = UserInterface.getInput("Enter Middle Name (optional): ");
        String lastName = UserInterface.getRequiredInput("Enter Last Name: ");
        String preferredName = UserInterface.getInput("Enter Preferred Name (optional): ");

        databaseHelper.updateProfile(username, email, firstName, middleName, lastName, preferredName);
        System.out.println("-------------------------------------");
        System.out.println("Profile setup done.");
        System.out.println("-------------------------------------");
    }

    /**
     * Handles the OTP login process for users with reset passwords.
     * 
     * @param username The username of the user
     * @throws SQLException if a database error occurs
     */
    public void handleOTPLogin(String username) throws SQLException {
        System.out.println("-------------------------------------");
        System.out.println("Your password has been reset by an admin. Please enter the OTP provided to you.");
        System.out.println("-------------------------------------");
        String enteredOTP = UserInterface.getInput("Enter OTP: ");
        
        if (databaseHelper.verifyOTP(username, enteredOTP)) {
            System.out.println("-------------------------------------");
            System.out.println("OTP verified. Please set a new password.");
            System.out.println("-------------------------------------");
            char[] newPassword;
            char[] confirmPassword;
            do {
                newPassword = UserInterface.getPassword("Enter new password: ");
                confirmPassword = UserInterface.getPassword("Confirm new password: ");
                
                if (!Arrays.equals(newPassword, confirmPassword)) {
                    System.out.println("Passwords do not match. Please try again.");
                }
            } while (!Arrays.equals(newPassword, confirmPassword));
            
            databaseHelper.resetPassword(username, newPassword);
            System.out.println("-------------------------------------");
            System.out.println("Password reset successful. You can now log in with your new password.");
            System.out.println("-------------------------------------");
        } else {
            System.out.println("-------------------------------------");
            System.out.println("Invalid OTP. Please contact an admin.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Handles the forgot password process for users.
     * 
     * @throws SQLException if a database error occurs
     */
    public void forgotPassword() throws SQLException {
        System.out.println("-------------------------------------");
        System.out.println("Forgot Password");
        System.out.println("-------------------------------------");
        String username = UserInterface.getInput("Enter your username: ");

        if (databaseHelper.doesUserExist(username)) {
            String otp = generateOTP();
            Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes from now
            databaseHelper.resetUserPassword(username, otp, expirationTime);
            System.out.println("-------------------------------------");
            System.out.println("An OTP has been generated and sent to your email: " + otp);
            System.out.println("-------------------------------------");
            
            String enteredOTP = UserInterface.getInput("Enter the OTP: ");
            
            if (databaseHelper.verifyOTP(username, enteredOTP)) {
                resetPassword(username);
            } else {
                System.out.println("-------------------------------------");
                System.out.println("Invalid OTP. Please try again.");
                System.out.println("-------------------------------------");
            }
        } else {
            System.out.println("-------------------------------------");
            System.out.println("No user found with this username.");
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Generates a random 6-digit OTP.
     * 
     * @return A string representing the generated OTP
     */
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Resets the password for a given user.
     * 
     * @param username The username of the user
     * @throws SQLException if a database error occurs
     */
    private void resetPassword(String username) throws SQLException {
        System.out.println("-------------------------------------");
        System.out.println("Reset Password");
        System.out.println("-------------------------------------");
        char[] newPassword;
        char[] confirmPassword;
        do {
            newPassword = UserInterface.getPassword("Enter new password: ");
            confirmPassword = UserInterface.getPassword("Confirm new password: ");

            if (!Arrays.equals(newPassword, confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
            }
        } while (!Arrays.equals(newPassword, confirmPassword));

        databaseHelper.resetPassword(username, newPassword);
        System.out.println("-------------------------------------");
        System.out.println("Password reset successful. You can now log in with your new password.");
        System.out.println("-------------------------------------");
    }
}