package edu.asu.DatabasePart1;
import java.sql.*;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/cse360_projectdb";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

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

	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}
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
	public void createInvitation(String invitationCode, String roles) throws SQLException {
		String insertInvitation = "INSERT INTO invitations (invitation_code, roles) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertInvitation)) {
			pstmt.setString(1, invitationCode);
			pstmt.setString(2, roles);
			pstmt.executeUpdate();
		}
	}
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
	public void markInvitationAsUsed(String invitationCode) throws SQLException {
		String updateInvitation = "UPDATE invitations SET is_used = TRUE WHERE invitation_code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateInvitation)) {
			pstmt.setString(1, invitationCode);
			pstmt.executeUpdate();
		}
	}
	public void resetUserPassword(String username, String otp, Timestamp expirationTime) throws SQLException {
		String query = "UPDATE cse360users SET otp = ?, otp_expiration = ?, is_otp_password = TRUE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.setTimestamp(2, expirationTime);
			pstmt.setString(3, username);
			pstmt.executeUpdate();
		}
	}
	public void deleteUser(String username) throws SQLException {
		String query = "DELETE FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		}
	}
	
	public void updateUserRoles(String username, String newRoles) throws SQLException {
		String query = "UPDATE cse360users SET roles = ? WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRoles);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		}
	}
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

	public void setOTP(String email, String otp) throws SQLException {
        String query = "UPDATE cse360users SET otp = ?, is_otp_password = TRUE WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        }
    }
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

	public void resetUserPasswordByAdmin(String username, String otp) throws SQLException {
		String query = "UPDATE cse360users SET otp = ?, is_otp_password = TRUE WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		}
	}
	
	// Update the resetPassword method
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


    // New methods for password hashing and verification
	private String hashPassword(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
    }

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


    public String generateSalt() {
        throw new UnsupportedOperationException("Unimplemented method 'generateSalt'");
    }

}
