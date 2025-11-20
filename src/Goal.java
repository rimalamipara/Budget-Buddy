import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.*;

interface IGoal {
    void setGoal(int userId);

    void addMoneyToGoal();

    void listOngoingGoals(int userId);
}

public class Goal implements IGoal {
    Scanner sc = new Scanner(System.in);

    public void setGoal(int userId) {
        String goalName = "";
        double targetAmount = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("Enter goal name: ");
                goalName = sc.nextLine().toLowerCase();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a String value.");
                sc.next();
            }
        }
        while (!validInput) {
            try {
                System.out.print("Enter target amount for goal: ");
                targetAmount = sc.nextDouble();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
                sc.next();
            }
        }

        sc.nextLine();
        String sql = "INSERT INTO Goals(user_id, goal_name, target_amount, current_amount) VALUES(?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, goalName);
            pstmt.setDouble(3, targetAmount);
            pstmt.setDouble(4, 0);
            pstmt.executeUpdate();
            System.out.println("Goal set successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addMoneyToGoal() {
        String goalName = "";
        double targetAmount = 0;
        double currentAmount = 0;
        double amount = 0;
        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print("Enter goal name: ");
                goalName = sc.nextLine().toLowerCase();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a String value.");
                sc.next();
            }
        }
        String findGoalIdSql = "SELECT goal_id, target_amount, current_amount FROM Goals WHERE goal_name = ?";
        int goalId = -1;

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement pstmt = con.prepareStatement(findGoalIdSql)) {
            pstmt.setString(1, goalName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                goalId = rs.getInt("goal_id");
                targetAmount = rs.getDouble("target_amount");
                currentAmount = rs.getDouble("current_amount");
            } else {
                System.out.println("Goal with the given name does not exist.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error finding goal: " + e.getMessage());
            return;
        }

        if (goalId != -1) {
            while (!validInput) {
                try {
                    System.out.print("Enter amount to add to the goal: ");
                    amount = sc.nextDouble();
                    validInput = true;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a numeric value.");
                    sc.next();
                }
            }

            sc.nextLine();
            String updateGoalSql = "UPDATE Goals SET current_amount = ? WHERE goal_id = ?";

            try (Connection con = DatabaseConnection.connect();
                    PreparedStatement pstmt = con.prepareStatement(updateGoalSql)) {
                pstmt.setDouble(1, currentAmount + amount);
                pstmt.setInt(2, goalId);
                pstmt.executeUpdate();

                System.out.println("Money added to goal successfully!");
                System.out.println("Goal: " + goalName);
                System.out.println("Target Amount: " + targetAmount);
                System.out.println("Current Amount: " + (currentAmount + amount));
                double filledPercentage = (currentAmount + amount) / targetAmount * 100;
                System.out.println("Filled: " + String.format("%.2f", filledPercentage) + "%");

                String username = getUsernameById(goalId, con);
                if (username != null) {
                    String filename = "src/userfiles/" + username + ".txt";
                    try (FileWriter writer = new FileWriter(filename, true)) {
                        writer.write("Added " + amount + " to goal: " + goalName + "\n");
                        writer.write("Current Amount: " + (currentAmount + amount) + " / " + targetAmount + "\n");
                        writer.write("Goal Completion: " + String.format("%.2f", filledPercentage) + "%\n\n");
                        System.out.println("Goal update written to " + filename);
                    } catch (IOException e) {
                        System.out.println("Error writing to file: " + e.getMessage());
                    }
                } else {
                    System.out.println("User not found. Goal details not written to file.");
                }
            } catch (SQLException e) {
                System.out.println("Error updating goal: " + e.getMessage());
            }
        }
    }

    public void listOngoingGoals(int userId) {
        String sql = "SELECT goal_name, target_amount, current_amount FROM Goals WHERE user_id = ? AND current_amount < target_amount";

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String goalName = rs.getString("goal_name");
                double targetAmount = rs.getDouble("target_amount");
                double currentAmount = rs.getDouble("current_amount");
                System.out.println("Goal: " + goalName + ", Target Amount: " + targetAmount + ", Current Amount: "
                        + currentAmount);
            }

            if (!hasResults) {
                System.out.println("No ongoing goals for user ID: " + userId);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching ongoing goals: " + e.getMessage());
        }
    }

    private String getUsernameById(int goalId, Connection con) {
        String sql = "SELECT u.username FROM Users u JOIN Goals g ON u.user_id = g.user_id WHERE g.goal_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, goalId);
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
