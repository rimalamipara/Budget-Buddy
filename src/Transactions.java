import java.sql.*;

interface ITransactions {
    void viewTransactions(int userId);
}

class Transaction {
    int transactionId;
    double amount;
    String type;
    String date;

    public Transaction(int transactionId, double amount, String type, String date) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }
}

class TransactionNode {
    Transaction data;
    TransactionNode next;
    TransactionNode prev;

    TransactionNode(Transaction data) {
        this.data = data;
    }
}

class TransactionDLL {
    TransactionNode head;
    TransactionNode tail;

    void insertAtFirst(Transaction data) {
        TransactionNode newNode = new TransactionNode(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }

    void insertAtLast(Transaction data) {
        TransactionNode newNode = new TransactionNode(data);
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
            System.out.println("No transactions to display.");
        } else {
            TransactionNode temp = head;
            while (temp != null) {
                System.out.println("ID: " + temp.data.transactionId +
                        ", Amount: " + temp.data.amount +
                        ", Type: " + temp.data.type +
                        ", Date: " + temp.data.date);
                temp = temp.next;
            }
        }
    }
}

public class Transactions implements ITransactions {
    private TransactionDLL transactionList = new TransactionDLL();

    public void viewTransactions(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ?";

        try (Connection con = DatabaseConnection.connect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int transactionId = rs.getInt("transaction_id");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                String date = rs.getString("date");
                Transaction transaction = new Transaction(transactionId, amount, type, date);
                transactionList.insertAtLast(transaction);
            }

            if (hasResults) {
                transactionList.display();
            } else {
                System.out.println("No transactions found for user ID: " + userId);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }
}
