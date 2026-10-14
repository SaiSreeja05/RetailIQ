package retailiq.service;

import retailiq.*;
import retailiq.csv.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RetailIQService {

    private static RetailIQService instance;

    private final ProductCSVRepository productRepo;
    private final CustomerCSVRepository customerRepo;
    private final ReviewCSVRepository reviewRepo;
    private final TransactionCSVRepository transactionRepo;
    private final WarehouseCSVRepository warehouseRepo;

    private final List<Runnable> changeListeners = new ArrayList<>();

    public RetailIQService() {
        this("data");
    }

    public RetailIQService(String dataDir) {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String productsPath = dataDir + File.separator + "products.csv";
        String customersPath = dataDir + File.separator + "customers.csv";
        String reviewsPath = dataDir + File.separator + "reviews.csv";
        String transactionsPath = dataDir + File.separator + "transactions.csv";
        String warehousesPath = dataDir + File.separator + "warehouses.csv";

        this.productRepo = new ProductCSVRepository(productsPath);
        this.customerRepo = new CustomerCSVRepository(customersPath);
        this.reviewRepo = new ReviewCSVRepository(reviewsPath);
        this.transactionRepo = new TransactionCSVRepository(transactionsPath);
        this.warehouseRepo = new WarehouseCSVRepository(warehousesPath);
    }

    public static synchronized RetailIQService getInstance() {
        if (instance == null) {
            instance = new RetailIQService();
        }
        return instance;
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void notifyDataChanged() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadAll() {
        productRepo.load();
        customerRepo.load();
        reviewRepo.load();
        transactionRepo.load();
        warehouseRepo.load();
        notifyDataChanged();
    }

    public void persistAll() {
        productRepo.persist();
        customerRepo.persist();
        reviewRepo.persist();
        transactionRepo.persist();
        warehouseRepo.persist();
    }

    // Repositories
    public ProductCSVRepository getProductRepo() {
        return productRepo;
    }

    public CustomerCSVRepository getCustomerRepo() {
        return customerRepo;
    }

    public ReviewCSVRepository getReviewRepo() {
        return reviewRepo;
    }

    public TransactionCSVRepository getTransactionRepo() {
        return transactionRepo;
    }

    public WarehouseCSVRepository getWarehouseRepo() {
        return warehouseRepo;
    }

    // Convenience Getters
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public List<Review> getAllReviews() {
        return reviewRepo.findAll();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepo.findAll();
    }

    // Products
    public DataValidator.ValidationResult saveProduct(Product product) {
        DataValidator.ValidationResult val = DataValidator.validateProduct(product);
        if (val.isValid()) {
            productRepo.save(product);
            notifyDataChanged();
        }
        return val;
    }

    public boolean deleteProduct(int productId) {
        boolean res = productRepo.deleteById(productId);
        if (res) notifyDataChanged();
        return res;
    }

    // Customers
    public DataValidator.ValidationResult saveCustomer(Customer customer) {
        DataValidator.ValidationResult val = DataValidator.validateCustomer(customer);
        if (val.isValid()) {
            customerRepo.save(customer);
            notifyDataChanged();
        }
        return val;
    }

    public boolean deleteCustomer(int customerId) {
        boolean res = customerRepo.deleteById(customerId);
        if (res) notifyDataChanged();
        return res;
    }

    // Reviews
    public DataValidator.ValidationResult saveReview(Review review) {
        DataValidator.ValidationResult val = DataValidator.validateReview(review);
        if (val.isValid()) {
            reviewRepo.save(review);
            notifyDataChanged();
        }
        return val;
    }

    public boolean deleteReview(int reviewId) {
        boolean res = reviewRepo.deleteById(reviewId);
        if (res) notifyDataChanged();
        return res;
    }

    // Transactions
    public DataValidator.ValidationResult saveTransaction(Transaction transaction) {
        DataValidator.ValidationResult val = DataValidator.validateTransaction(transaction);
        if (val.isValid()) {
            transactionRepo.save(transaction);
            // Optionally update product stock
            productRepo.findById(transaction.getProductId()).ifPresent(prod -> {
                int newStock = Math.max(0, prod.getStock() - transaction.getQuantity());
                Product updated = new Product(
                        prod.getProductId(), prod.getName(), prod.getCategory(), prod.getDescription(),
                        prod.getPrice(), newStock, prod.getRating(), prod.getProductCode()
                );
                productRepo.save(updated);
            });
            notifyDataChanged();
        }
        return val;
    }

    public boolean deleteTransaction(String transactionId) {
        boolean res = transactionRepo.deleteById(transactionId);
        if (res) notifyDataChanged();
        return res;
    }

    // Warehouses
    public DataValidator.ValidationResult saveWarehouse(Warehouse warehouse) {
        DataValidator.ValidationResult val = DataValidator.validateWarehouse(warehouse);
        if (val.isValid()) {
            warehouseRepo.save(warehouse);
            notifyDataChanged();
        }
        return val;
    }

    public boolean deleteWarehouse(int warehouseId) {
        boolean res = warehouseRepo.deleteById(warehouseId);
        if (res) notifyDataChanged();
        return res;
    }
}
