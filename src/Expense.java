import java.util.*;
import java.sql.*;
import java.io.*;

interface IExpense {
    void addExpense(int userId);
}

class Expense implements IExpense {
    Scanner sc = new Scanner(System.in);

    public void addExpense(int userId) {
        double amount = 0.0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("Enter Expense amount: ");
                amount = sc.nextDouble();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
                sc.next();
            }
        }
        sc.nextLine();

        try (Connection con = DatabaseConnection.connect()) {
            CallableStatement stmt = con.prepareCall("{call add_transaction(?, ?, ?)}");
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.setString(3, "EXPENSE");
            stmt.execute();
            System.out.println("Expense added successfully!");

            String username = getUsernameById(userId, con);

            if (username != null) {
                String filename = "src/userfiles/" + username + ".txt";
                try (FileWriter fw = new FileWriter(filename, true)) {
                    fw.write("Expense added: " + amount + "\n");
                    System.out.println("Expense details appended to " + filename);
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                }
            } else {
                System.out.println("User not found. Expense details not written to file.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
