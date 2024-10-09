package edu.asu.DatabasePart1;
import java.sql.*;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

/****
 * <p> DatabaseHelper Class </p>
 * 
 * <p> Description: Handles all database operations and interactions. </p>
 * 
 * @author Dhruv Bansal, Shreyas Bachiraju, Nirek Shah, Dhruv Shetty, Sonit Penchala
 * 
 * @version 1.00 2024-10-09 Implementation for database helper
 */

class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/cse360_projectdb";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

	/**
	 * Establishes a connection to the database and creates necessary tables.
	 * 
	 * @throws SQLException if a database access error occurs
	 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	/**
	 * Creates necessary tables in the database if they don't exist.
	 * 
	 * @throws SQLException if a database access error occurs
	 */
	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "username VARCHAR(100) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "roles VARCHAR(255), "
				+ "email VARCHAR(255) UNIQUE, "
				+ "first_name VARCHAR(100), "
				+ "middle_name VARCHAR(100), "
				+ "last_name VARCHAR(100), "
				+ "preferred_name VARCHAR(100), "
				+ "otp VARCHAR(50), "
				+ "otp_expiration TIMESTAMP, "
				+ "is_otp_password BOOLEAN DEFAULT FALSE, "
				+ "profile_complete BOOLEAN DEFAULT FALSE)";
		statement.execute(userTable);

        String invitationTable = "CREATE TABLE IF NOT EXISTS invitations ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "invitation_code VARCHAR(50) UNIQUE, "
                + "roles VARCHAR(100), "
                + "is_used BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationTable);
    }

	/**
	 * Checks if the database is empty.
	 * 
	 * @return true if the database is empty, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	/**
	 * Registers a new user in the database.
	 * 
	 * @param username the username of the new user
	 * @param password the password of the new user
	 * @param roles the roles assigned to the new user
	 * @throws SQLException if a database access error occurs
	 */
	public void register(String username, char[] password, String roles) throws SQLException {
		String insertUser = "INSERT INTO cse360users (username, password, roles, profile_complete) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, username);
			pstmt.setString(2, hashPassword(password));
			pstmt.setString(3, roles);
			pstmt.setBoolean(4, false);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Creates a new invitation in the database.
	 * 
	 * @param invitationCode the unique invitation code
	 * @param roles the roles associated with the invitation
	 * @throws SQLException if a database access error occurs
	 */
	public void createInvitation(String invitationCode, String roles) throws SQLException {
		String insertInvitation = "INSERT INTO invitations (invitation_code, roles) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertInvitation)) {
			pstmt.setString(1, invitationCode);
			pstmt.setString(2, roles);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves the roles associated with an invitation code.
	 * 
	 * @param invitationCode the invitation code to check
	 * @return the roles associated with the invitation code, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
	public String getInvitationRoles(String invitationCode) throws SQLException {
		String query = "SELECT roles FROM invitations WHERE invitation_code = ? AND is_used = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, invitationCode);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("roles");
				}
			}
		}
		return null;
	}

	/**
	 * Marks an invitation as used in the database.
	 * 
	 * @param invitationCode the invitation code to mark as used
	 * @throws SQLException if a database access error occurs
	 */
	public void markInvitationAsUsed(String invitationCode) throws SQLException {
		String updateInvitation = "UPDATE invitations SET is_used = TRUE WHERE invitation_code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateInvitation)) {
			pstmt.setString(1, invitationCode);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Resets a user's password and sets up OTP authentication.
	 * 
	 * @param username the username of the user
	 * @param otp the one-time password
	 * @param expirationTime the expiration time for the OTP
	 * @throws SQLException if a database access error occurs
	 */
	public void resetUserPassword(String username, String otp, Timestamp expirationTime) throws SQLException {
		String query = "UPDATE cse360users SET otp = ?, otp_expiration = ?, is_otp_password = TRUE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.setTimestamp(2, expirationTime);
			pstmt.setString(3, username);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a user from the database.
	 * 
	 * @param username the username of the user to delete
	 * @throws SQLException if a database access error occurs
	 */
	public void deleteUser(String username) throws SQLException {
		String query = "DELETE FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		}
	}
	
	/**
	 * Updates the roles of a user in the database.
	 * 
	 * @param username the username of the user
	 * @param newRoles the new roles to assign to the user
	 * @throws SQLException if a database access error occurs
	 */
	public void updateUserRoles(String username, String newRoles) throws SQLException {
		String query = "UPDATE cse360users SET roles = ? WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRoles);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Authenticates a user's login credentials.
	 * 
	 * @param username the username of the user
	 * @param password the password of the user
	 * @return true if the login is successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean login(String username, char[] password) throws SQLException {
		String query = "SELECT password FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String storedHash = rs.getString("password");
					return checkPassword(password, storedHash);
				}
			}
		} finally {
			Arrays.fill(password, '\0');
		}
		return false;
	}

	/**
	 * Checks if a user's password is set to OTP.
	 * 
	 * @param username the username of the user
	 * @return true if the user's password is set to OTP, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isOTPPasswordSet(String username) throws SQLException {
		String query = "SELECT is_otp_password FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean("is_otp_password");
				}
			}
		}
		return false;
	}

	/**
	 * Retrieves the roles of a user from the database.
	 * 
	 * @param username the username of the user
	 * @return an array of roles assigned to the user
	 * @throws SQLException if a database access error occurs
	 */
	public String[] getUserRoles(String username) throws SQLException {
		String query = "SELECT roles FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String rolesString = rs.getString("roles");
					return rolesString.split(",");
				}
			}
		}
		return new String[]{}; // Return an empty array if no roles are found
	}

	/**
	 * Checks if a user's profile is complete.
	 * 
	 * @param username the username of the user
	 * @return true if the user's profile is complete, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isProfileComplete(String username) throws SQLException {
		String query = "SELECT profile_complete FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean("profile_complete");
				}
			}
		}
		return false;
	}

	/**
	 * Updates a user's profile information in the database.
	 * 
	 * @param username the username of the user
	 * @param email the user's email address
	 * @param firstName the user's first name
	 * @param middleName the user's middle name (optional)
	 * @param lastName the user's last name
	 * @param preferredName the user's preferred name (optional)
	 * @throws SQLException if a database access error occurs
	 */
	public void updateProfile(String username, String email, String firstName, String middleName, String lastName, String preferredName) throws SQLException {
		String query = "UPDATE cse360users SET email = ?, first_name = ?, middle_name = ?, last_name = ?, preferred_name = ?, profile_complete = TRUE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, email);
			pstmt.setString(2, firstName);
			pstmt.setString(3, middleName != null && !middleName.trim().isEmpty() ? middleName : null);
			pstmt.setString(4, lastName);
			pstmt.setString(5, preferredName != null && !preferredName.trim().isEmpty() ? preferredName : null);
			pstmt.setString(6, username);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if a user exists in the database.
	 * 
	 * @param username the username to check
	 * @return true if the user exists, false otherwise
	 */
	public boolean doesUserExist(String username) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Displays a list of all users for admin view.
	 * 
	 * @throws SQLException if a database access error occurs
	 */
	public void displayUsersByAdmin() throws SQLException {
		String sql = "SELECT username, first_name, last_name, roles FROM cse360users";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			System.out.println("User List:");
			System.out.printf("%-20s %-20s %-20s %-20s%n", "Username", "First Name", "Last Name", "Roles");
			System.out.println("-".repeat(80));
			while (rs.next()) {
				String username = rs.getString("username");
				String firstName = rs.getString("first_name");
				String lastName = rs.getString("last_name");
				String roles = rs.getString("roles");
				System.out.printf("%-20s %-20s %-20s %-20s%n", username, firstName, lastName, roles);
			}
		}
	}

	/**
	 * Displays user details for a specific user.
	 * 
	 * @param userEmail the email of the user to display
	 * @throws SQLException if a database access error occurs
	 */
	public void displayUsersByUser(String userEmail) throws SQLException {
		String sql = "SELECT * FROM cse360users WHERE email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, userEmail);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Retrieve by column name
					int id = rs.getInt("id");
					String email = rs.getString("email");
					String firstName = rs.getString("first_name");
					String lastName = rs.getString("last_name");
					String preferredName = rs.getString("preferred_name");
					String username = rs.getString("user_name");
					String role = rs.getString("role");

					// Display values
					System.out.println("User Details:");
					System.out.println("ID: " + id);
					System.out.println("Email: " + email);
					System.out.println("First Name: " + firstName);
					System.out.println("Last Name: " + lastName);
					System.out.println("Preferred Name: " + preferredName);
					System.out.println("Username: " + username);
					System.out.println("Role: " + role);
				} else {
					System.out.println("No user found with the email: " + userEmail);
				}
			}
		}
	}

	/**
	 * Sets a one-time password (OTP) for a user.
	 *
	 * @param email The email of the user
	 * @param otp The one-time password to set
	 * @throws SQLException if a database access error occurs
	 */
	public void setOTP(String email, String otp) throws SQLException {
        String query = "UPDATE cse360users SET otp = ?, is_otp_password = TRUE WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        }
    }

	/**
	 * Verifies the one-time password (OTP) for a user.
	 *
	 * @param username The username of the user
	 * @param otp The one-time password to verify
	 * @return true if the OTP is valid, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean verifyOTP(String username, String otp) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE username = ? AND otp = ? AND is_otp_password = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Resets a user's password by setting a one-time password (OTP). Used by admin.
	 *
	 * @param username The username of the user
	 * @param otp The one-time password to set
	 * @throws SQLException if a database access error occurs
	 */
	public void resetUserPasswordByAdmin(String username, String otp) throws SQLException {
		String query = "UPDATE cse360users SET otp = ?, is_otp_password = TRUE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		}
	}
	
	/**
	 * Resets a user's password with a new password.
	 *
	 * @param username The username of the user
	 * @param newPassword The new password to set
	 * @throws SQLException if a database access error occurs
	 */
	public void resetPassword(String username, char[] newPassword) throws SQLException {
		String query = "UPDATE cse360users SET password = ?, otp = NULL, is_otp_password = FALSE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, hashPassword(newPassword));
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		} finally {
			Arrays.fill(newPassword, '\0'); // Clear the password array
		}
	}

	/**
	 * Hashes a password using BCrypt.
	 *
	 * @param password The password to hash
	 * @return The hashed password
	 */
	private String hashPassword(char[] password) {
		return BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
	}

	/**
	 * Checks if an input password matches a stored hash.
	 *
	 * @param inputPassword The input password to check
	 * @param storedHash The stored hash to compare against
	 * @return true if the password matches, false otherwise
	 */
	public boolean checkPassword(char[] inputPassword, String storedHash) {
		try {
			return BCrypt.checkpw(new String(inputPassword), storedHash);
		} catch (IllegalArgumentException e) {
			System.err.println("Error checking password: " + e.getMessage());
			return false;
		} finally {
			Arrays.fill(inputPassword, '\0'); // Clear the password array
		}
	}
	
	/**
	 * Closes the database connection and statement.
	 */
	public void closeConnection() {
		// Close statement, if it exists
		try {
			if (statement != null) {
				statement.close();
				System.out.println("Statement closed successfully.");
			}
		} catch (SQLException se2) {
			System.err.println("Error closing statement: " + se2.getMessage());
			se2.printStackTrace();
		}

		// Close connection, if it exists
		try {
			if (connection != null) {
				connection.close();
				System.out.println("Connection closed successfully.");
			}
		} catch (SQLException se) {
			System.err.println("Error closing connection: " + se.getMessage());
			se.printStackTrace();
		}
	}

	/**
	 * Generates a salt for password hashing.
	 *
	 * @return The generated salt
	 * @throws UnsupportedOperationException if the method is not implemented
	 */
	public String generateSalt() {
		throw new UnsupportedOperationException("Unimplemented method 'generateSalt'");
	}

}
