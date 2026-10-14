package retailiq;

public class Transaction {

    private String transactionId;
    private int customerId;
    private int productId;
    private int quantity;
    private double totalAmount;
    private String transactionDate;

    public Transaction(String transactionId, int customerId,
                       int productId, int quantity,
                       double totalAmount, String transactionDate) {

        this.transactionId = transactionId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void displayTransaction() {

        System.out.println("Transaction ID : " + transactionId);
        System.out.println("Customer ID    : " + customerId);
        System.out.println("Product ID     : " + productId);
        System.out.println("Quantity       : " + quantity);
        System.out.println("Total Amount   : Rs. " + totalAmount);
        System.out.println("Date           : " + transactionDate);
    }
}