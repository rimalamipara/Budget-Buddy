import java.util.InputMismatchException;
import java.util.Scanner;

class Main {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {

        Login_Register lr = new Login_Register();
        Income im = new Income();
        Expense e = new Expense();
        Goal g = new Goal();
        Transactions t = new Transactions();
        boolean validInput = false;
        int choice1 = 12;
        int choice2 = 12;
        int userId = -1;

        while (true) {
            if (userId == -1) {
                System.out.println("\n--- Budget Buddy ---\n");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Reset Password");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                while (!validInput) {
                    try {
                        choice1 = sc.nextInt();
                        validInput = true;
                    } catch (InputMismatchException q) {
                        System.out.println("Invalid input. Please enter a numeric value.");
                        sc.next();
                    }
                }
                sc.nextLine();

                switch (choice1) {
                    case 1:
                        lr.register();
                        break;
                    case 2:
                        userId = lr.login();
                        break;
                    case 3:
                        lr.resetPassword();
                        break;
                    case 0:
                        System.out.println("Exiting...........");
                        sc.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Enter a valid choice.");
                        break;
                }
            } else {
                System.out.println("1. Add Income");
                System.out.println("2. Add Expense");
                System.out.println("3. Set Goal");
                System.out.println("4. Add Money to Goal");
                System.out.println("5. total running goals");
                System.out.println("6. View Transactions");
                System.out.println("0. Logout");
                System.out.print("Choose an option: ");

                while (!validInput) {
                    try {
                        choice2 = sc.nextInt();
                        validInput = true;
                    } catch (InputMismatchException q) {
                        System.out.println("Invalid input. Please enter a numeric value.");
                        sc.next();
                    }
                }
                sc.nextLine();

                switch (choice2) {
                    case 1:
                        im.addIncome(userId);
                        break;
                    case 2:
                        e.addExpense(userId);
                        break;
                    case 3:
                        g.setGoal(userId);
                        break;
                    case 4:
                        g.addMoneyToGoal();
                        break;
                    case 5:
                        g.listOngoingGoals(userId);
                        break;
                    case 6:
                        t.viewTransactions(userId);
                        break;
                    case 0:
                        System.out.println("Logging out...");
                        userId = -1;
                        break;
                    default:
                        System.out.println("Enter a valid choice.");
                        break;
                }
            }
        }
    }
}