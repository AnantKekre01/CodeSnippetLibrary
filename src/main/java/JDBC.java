import java.sql.*;
import java.util.Scanner;

public class JDBC {
    private static final String URL = "jdbc:mysql://127.0.0.1:3307/sys";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. ALTER TABLE (snippets)");
            System.out.println("2. EXIT");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    alterTable(scanner);
                    break;
                case 2:
                    System.out.println("Exiting program.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ALTER TABLE Operation
    public static void alterTable(Scanner scanner) {
        System.out.print("Enter ALTER TABLE query: ");
        String alterQuery = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(alterQuery);
            System.out.println("ALTER TABLE executed successfully.");

        } catch (SQLException e) {
            System.out.println("Error executing ALTER TABLE query.");
            e.printStackTrace();
        }
    }
}
