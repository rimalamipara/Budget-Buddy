import java.sql.*;
import java.util.*;
import java.io.*;

class UserNode {
    User data;
    UserNode next;
    UserNode prev;

    UserNode(User data) {
        this.data = data;
    }
}

class DLL1 {
    UserNode head;
    UserNode tail;

    void insertAtFirst(User data) {
        UserNode newNode = new UserNode(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }

    void insertAtLast(User data) {
        UserNode newNode = new UserNode(data);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    void display() {
        if (head == null) {
            System.out.println("Empty");
        } else {
            UserNode temp = head;
            while (temp != null) {
                System.out.println("User ID: " + temp.data.userId + ", Username: " + temp.data.username);
                temp = temp.next;
            }
        }
    }

    User find(String username, String password) {
        UserNode temp = head;
        while (temp != null) {
            if (temp.data.username.equals(username) && temp.data.password.equals(password)) {
                return temp.data;
            }
            temp = temp.next;
        }
        return null;
    }

    UserNode findUserNodeByUsername(String username) {
        UserNode temp = head;
        while (temp != null) {
            if (temp.data.username.equals(username)) {
                return temp;
            }
            temp = temp.next;
        }
        return null;
    }
}

class User {
    int userId;
    String username;
    String password;

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }
}

public class Login_Register {
    Scanner sc = new Scanner(System.in);
    DLL1 userList = new DLL1();

    public Login_Register() {
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        String sql = "SELECT * FROM Users";

        try (Connection con = DatabaseConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                User user = new User(userId, username, password);
                userList.insertAtLast(user);
            }
        } catch (SQLException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public int login() {
        String password = "";
        String username = "";
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("Enter username: ");
                username = sc.nextLine();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a String value.");
                sc.next();
            }
        }
        sc.nextLine();
        while (!validInput) {
            try {
                System.out.print("Enter password: ");
                password = sc.nextLine();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
                sc.next();
            }
        }
        sc.nextLine();

        User user = userList.find(username, password);

        if (user != null) {
            System.out.println("Login successful!");
            return user.userId;
        } else {
            System.out.println("Invalid credentials.");
            return -1;
        }
    }

    void resetPassword() {
        System.out.print("Enter your username: ");
        String username = sc.nextLine();
        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        String sql = "SELECT email FROM Users WHERE username = ?";

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedEmail = rs.getString("email");
                if (storedEmail.equals(email)) {
                    System.out.print("Enter new password: ");
                    String newPassword = sc.nextLine();

                    String updateSql = "UPDATE Users SET password = ? WHERE username = ?";
                    try (PreparedStatement updatePstmt = con.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, newPassword);
                        updatePstmt.setString(2, username);
                        updatePstmt.executeUpdate();
                        System.out.println("Password reset successfully. You can now log in with your new password.");

                        // Update the password in the linked list
                        UserNode userNode = userList.findUserNodeByUsername(username);
                        if (userNode != null) {
                            userNode.data.password = newPassword;
                        }
                    }
                } else {
                    System.out.println("Email does not match our records.");
                }
            } else {
                System.out.println("Username does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error resetting password: " + e.getMessage());
        }
    }

    public void register() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();

        String checkUserSql = "SELECT * FROM Users WHERE username = ?";

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement checkPstmt = con.prepareStatement(checkUserSql)) {
            checkPstmt.setString(1, username);
            ResultSet rs = checkPstmt.executeQuery();

            if (rs.next()) {
                System.out.println("User already exists.");
            } else {
                String sql = "INSERT INTO Users(username, password, email) VALUES(?, ?, ?)";
                try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.setString(3, email);
                    pstmt.executeUpdate();
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        System.out.println("User registered successfully!");

                        User newUser = new User(userId, username, password);
                        userList.insertAtLast(newUser);

                        File userFile = new File("src/userfiles/" + username + ".txt");
                        if (userFile.createNewFile()) {
                            try (FileWriter writer = new FileWriter(userFile)) {
                                writer.write("Welcome " + username + "!\n");
                                writer.write("Your registration was successful.\n");
                                writer.write("Username: " + username + "\n");
                            }
                            System.out.println("User file created: " + userFile.getName());
                        } else {
                            System.out.println("User file already exists.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error creating user file: " + e.getMessage());
        }
    }
}
