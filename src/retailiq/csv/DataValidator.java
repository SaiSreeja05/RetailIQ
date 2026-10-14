package retailiq.csv;

import retailiq.*;

import java.util.ArrayList;
import java.util.List;

public class DataValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    public static ValidationResult validateProduct(Product product) {
        List<String> errors = new ArrayList<>();
        if (product == null) {
            errors.add("Product cannot be null");
            return new ValidationResult(false, errors);
        }

        if (product.getProductId() <= 0) {
            errors.add("Product ID must be a positive integer");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            errors.add("Product name cannot be empty");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            errors.add("Category cannot be empty");
        }
        if (product.getPrice() < 0) {
            errors.add("Price cannot be negative");
        }
        if (product.getStock() < 0) {
            errors.add("Stock cannot be negative");
        }
        if (product.getRating() < 0.0 || product.getRating() > 5.0) {
            errors.add("Rating must be between 0.0 and 5.0");
        }
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            errors.add("Product code cannot be empty");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult validateCustomer(Customer customer) {
        List<String> errors = new ArrayList<>();
        if (customer == null) {
            errors.add("Customer cannot be null");
            return new ValidationResult(false, errors);
        }

        if (customer.getCustomerId() <= 0) {
            errors.add("Customer ID must be a positive integer");
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            errors.add("Customer name cannot be empty");
        }
        if (customer.getEmail() == null || !customer.getEmail().contains("@") || !customer.getEmail().contains(".")) {
            errors.add("Valid email address is required");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            errors.add("Phone number cannot be empty");
        }
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            errors.add("Address cannot be empty");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult validateReview(Review review) {
        List<String> errors = new ArrayList<>();
        if (review == null) {
            errors.add("Review cannot be null");
            return new ValidationResult(false, errors);
        }

        if (review.getReviewId() <= 0) {
            errors.add("Review ID must be a positive integer");
        }
        if (review.getProductId() <= 0) {
            errors.add("Associated Product ID must be positive");
        }
        if (review.getCustomerId() <= 0) {
            errors.add("Associated Customer ID must be positive");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            errors.add("Rating must be an integer between 1 and 5");
        }
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            errors.add("Review commentary cannot be empty");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult validateTransaction(Transaction transaction) {
        List<String> errors = new ArrayList<>();
        if (transaction == null) {
            errors.add("Transaction cannot be null");
            return new ValidationResult(false, errors);
        }

        if (transaction.getTransactionId() == null || transaction.getTransactionId().trim().isEmpty()) {
            errors.add("Transaction ID cannot be empty");
        }
        if (transaction.getCustomerId() <= 0) {
            errors.add("Customer ID must be positive");
        }
        if (transaction.getProductId() <= 0) {
            errors.add("Product ID must be positive");
        }
        if (transaction.getQuantity() <= 0) {
            errors.add("Quantity must be greater than zero");
        }
        if (transaction.getTotalAmount() < 0) {
            errors.add("Total amount cannot be negative");
        }
        if (transaction.getTransactionDate() == null || transaction.getTransactionDate().trim().isEmpty()) {
            errors.add("Transaction date cannot be empty");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult validateWarehouse(Warehouse warehouse) {
        List<String> errors = new ArrayList<>();
        if (warehouse == null) {
            errors.add("Warehouse cannot be null");
            return new ValidationResult(false, errors);
        }

        if (warehouse.getWarehouseId() <= 0) {
            errors.add("Warehouse ID must be a positive integer");
        }
        if (warehouse.getWarehouseName() == null || warehouse.getWarehouseName().trim().isEmpty()) {
            errors.add("Warehouse name cannot be empty");
        }
        if (warehouse.getLocation() == null || warehouse.getLocation().trim().isEmpty()) {
            errors.add("Location cannot be empty");
        }
        if (warehouse.getCapacity() <= 0) {
            errors.add("Capacity must be greater than zero");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
