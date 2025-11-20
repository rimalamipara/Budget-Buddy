import java.sql.*;
import java.util.*;
import java.io.*;

interface IIncome {
    void addIncome(int userId);
}

public class Income implements IIncome {
    Scanner sc = new Scanner(System.in);

    public void addIncome(int userId) {

        double amount = 0.0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("Enter income amount: ");
                amount = sc.nextDouble();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
                sc.next(); // Clear the invalid input
            }
        }
        sc.nextLine();

        String sql = "{CALL add_transaction(?, ?, 'INCOME')}";

        try (Connection con = DatabaseConnection.connect()) {
            CallableStatement stmt = con.prepareCall(sql);
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.execute();
            System.out.println("Income added successfully!");

            String username = getUsernameById(userId, con);

            if (username != null) {
                String filename = "src/userfiles/" + username + ".txt";
                try (FileWriter fw = new FileWriter(filename, true)) {
                    fw.write("Income added: " + amount + "\n");
                    System.out.println("Income details appended to " + filename);
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                }
            } else {
                System.out.println("User not found. Income details not written to file.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding income: " + e.getMessage());
        }
    }

    private String getUsernameById(int userId, Connection con) {
        String sql = "SELECT username FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving username: " + e.getMessage());
        }
        return null;
    }
}
