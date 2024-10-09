package edu.asu.DatabasePart1;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;

public class AuthenticationManager {
    private final DatabaseHelper databaseHelper;

    public AuthenticationManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public boolean login(String username, char[] password) throws SQLException {
        return databaseHelper.login(username, password);
    }

    public void setupProfile(String username) throws SQLException {
        System.out.println("Setting up your profile.");
        String email = UserInterface.getRequiredInput("Enter Email: ");
        String firstName = UserInterface.getRequiredInput("Enter First Name: ");
        String middleName = UserInterface.getInput("Enter Middle Name (optional): ");
        String lastName = UserInterface.getRequiredInput("Enter Last Name: ");
        String preferredName = UserInterface.getInput("Enter Preferred Name (optional): ");

        databaseHelper.updateProfile(username, email, firstName, middleName, lastName, preferredName);
        System.out.println("Profile setup completed.");
    }

    public void handleOTPLogin(String username) throws SQLException {
        System.out.println("Your password has been reset by an administrator. Please enter the OTP provided to you.");
        String enteredOTP = UserInterface.getInput("Enter OTP: ");
        
        if (databaseHelper.verifyOTP(username, enteredOTP)) {
            System.out.println("OTP verified. Please set a new password.");
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
            System.out.println("Password reset successful. You can now log in with your new password.");
        } else {
            System.out.println("Invalid OTP. Please contact an administrator.");
        }
    }

    public void forgotPassword() throws SQLException {
        String username = UserInterface.getInput("Enter your username: ");

        if (databaseHelper.doesUserExist(username)) {
            String otp = generateOTP();
            Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes from now
            databaseHelper.resetUserPassword(username, otp, expirationTime);
            System.out.println("An OTP has been generated and sent to your email: " + otp);
            
            String enteredOTP = UserInterface.getInput("Enter the OTP: ");
            
            if (databaseHelper.verifyOTP(username, enteredOTP)) {
                resetPassword(username);
            } else {
                System.out.println("Invalid OTP. Please try again.");
            }
        } else {
            System.out.println("No user found with this username.");
        }
    }

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void resetPassword(String username) throws SQLException {
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
        System.out.println("Password reset successful. You can now log in with your new password.");
    }
}