package retailiq;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ProductDatabase productDatabase = new ProductDatabase();
        CustomerDatabase customerDatabase = new CustomerDatabase();
        ReviewDatabase reviewDatabase = new ReviewDatabase();
        TransactionDatabase transactionDatabase = new TransactionDatabase();
        WarehouseDatabase warehouseDatabase = new WarehouseDatabase();

        int choice;

        do {

            System.out.println();
            System.out.println("=================================================");
            System.out.println("                    RETAILIQ");
            System.out.println("            Retail Analytics System");
            System.out.println("=================================================");
            System.out.println("1. View Products");
            System.out.println("2. View Customers");
            System.out.println("3. View Reviews");
            System.out.println("4. View Transactions");
            System.out.println("5. View Warehouses");
            System.out.println("6. Search Product");
            System.out.println("7. Search Customer");
            System.out.println("8. Search Review");
            System.out.println("9. Search Transaction");
            System.out.println("10. Exit");
            System.out.println("=================================================");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    productDatabase.displayAllProducts();
                    break;

                case 2:
                    customerDatabase.displayAllCustomers();
                    break;

                case 3:
                    reviewDatabase.displayAllReviews();
                    break;

                case 4:
                    transactionDatabase.displayAllTransactions();
                    break;

                case 5:
                    warehouseDatabase.displayAllWarehouses();
                    break;

                case 6:

                    System.out.print("Enter Product ID: ");
                    int productId = sc.nextInt();

                    Product product =
                            productDatabase.findProductById(productId);

                    if (product != null) {
                        System.out.println();
                        product.displayProduct();
                    } else {
                        System.out.println("Product not found.");
                    }

                    break;

                case 7:

                    System.out.print("Enter Customer ID: ");
                    int customerId = sc.nextInt();

                    Customer customer =
                            customerDatabase.findCustomerById(customerId);

                    if (customer != null) {
                        System.out.println();
                        customer.displayCustomer();
                    } else {
                        System.out.println("Customer not found.");
                    }

                    break;

                case 8:

                    System.out.print("Enter Review ID: ");
                    int reviewId = sc.nextInt();

                    Review review =
                            reviewDatabase.findReviewById(reviewId);

                    if (review != null) {
                        System.out.println();
                        review.displayReview();
                    } else {
                        System.out.println("Review not found.");
                    }

                    break;

                case 9:

                    System.out.print("Enter Transaction ID: ");
                    String transactionId = sc.next();

                    Transaction transaction =
                            transactionDatabase
                            .findTransactionById(transactionId);

                    if (transaction != null) {
                        System.out.println();
                        transaction.displayTransaction();
                    } else {
                        System.out.println("Transaction not found.");
                    }

                    break;

                case 10:

                    System.out.println();
                    System.out.println("Thank you for using RetailIQ.");
                    break;

                default:

                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 10);

        sc.close();
    }
}