package retailiq.service;

import retailiq.*;

import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsEngine {

    private final RetailIQService service;

    public AnalyticsEngine(RetailIQService service) {
        this.service = service;
    }

    public int getTotalProducts() {
        return service.getAllProducts().size();
    }

    public int getTotalCustomers() {
        return service.getAllCustomers().size();
    }

    public int getTotalTransactions() {
        return service.getAllTransactions().size();
    }

    public double getTotalRevenue() {
        return service.getAllTransactions().stream()
                .mapToDouble(Transaction::getTotalAmount)
                .sum();
    }

    public double getAverageOrderValue() {
        int count = getTotalTransactions();
        return count == 0 ? 0.0 : getTotalRevenue() / count;
    }

    public double getAverageReviewRating() {
        List<Review> reviews = service.getAllReviews();
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public int getTotalInventoryUnits() {
        return service.getAllProducts().stream()
                .mapToInt(Product::getStock)
                .sum();
    }

    public int getTotalWarehouseCapacity() {
        return service.getAllWarehouses().stream()
                .mapToInt(Warehouse::getCapacity)
                .sum();
    }

    public double getWarehouseUtilizationRate() {
        int cap = getTotalWarehouseCapacity();
        if (cap == 0) return 0.0;
        int inventory = getTotalInventoryUnits();
        return Math.min(100.0, (double) inventory / cap * 100.0);
    }

    public Map<String, Integer> getProductCountByCategory() {
        Map<String, Integer> map = new TreeMap<>();
        for (Product p : service.getAllProducts()) {
            map.put(p.getCategory(), map.getOrDefault(p.getCategory(), 0) + 1);
        }
        return map;
    }

    public Map<String, Double> getRevenueByCategory() {
        Map<Integer, Product> prodMap = service.getAllProducts().stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
        Map<String, Double> map = new TreeMap<>();
        for (Transaction t : service.getAllTransactions()) {
            Product p = prodMap.get(t.getProductId());
            String cat = (p != null) ? p.getCategory() : "Other";
            map.put(cat, map.getOrDefault(cat, 0.0) + t.getTotalAmount());
        }
        return map;
    }

    public Map<String, Double> getSalesByDate() {
        Map<String, Double> map = new TreeMap<>();
        for (Transaction t : service.getAllTransactions()) {
            String date = t.getTransactionDate();
            map.put(date, map.getOrDefault(date, 0.0) + t.getTotalAmount());
        }
        return map;
    }

    public static class ProductSalesStats {
        private final Product product;
        private final int quantitySold;
        private final double totalRevenue;

        public ProductSalesStats(Product product, int quantitySold, double totalRevenue) {
            this.product = product;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }

        public Product getProduct() { return product; }
        public int getQuantitySold() { return quantitySold; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public List<ProductSalesStats> getTopSellingProducts() {
        Map<Integer, Product> prodMap = service.getAllProducts().stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        Map<Integer, Integer> qtyMap = new HashMap<>();
        Map<Integer, Double> revMap = new HashMap<>();

        for (Transaction t : service.getAllTransactions()) {
            qtyMap.put(t.getProductId(), qtyMap.getOrDefault(t.getProductId(), 0) + t.getQuantity());
            revMap.put(t.getProductId(), revMap.getOrDefault(t.getProductId(), 0.0) + t.getTotalAmount());
        }

        List<ProductSalesStats> stats = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : qtyMap.entrySet()) {
            Product p = prodMap.get(entry.getKey());
            if (p != null) {
                stats.add(new ProductSalesStats(p, entry.getValue(), revMap.getOrDefault(entry.getKey(), 0.0)));
            }
        }

        stats.sort((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
        return stats;
    }

    public List<Product> getLowStockProducts(int threshold) {
        return service.getAllProducts().stream()
                .filter(p -> p.getStock() <= threshold)
                .sorted(Comparator.comparingInt(Product::getStock))
                .collect(Collectors.toList());
    }

    public Map<Integer, Integer> getRatingDistribution() {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            map.put(i, 0);
        }
        for (Review r : service.getAllReviews()) {
            int rating = Math.max(1, Math.min(5, r.getRating()));
            map.put(rating, map.getOrDefault(rating, 0) + 1);
        }
        return map;
    }

    public List<Transaction> getCustomerTransactions(int customerId) {
        return service.getAllTransactions().stream()
                .filter(t -> t.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public double getCustomerTotalSpend(int customerId) {
        return getCustomerTransactions(customerId).stream()
                .mapToDouble(Transaction::getTotalAmount)
                .sum();
    }

    public List<Review> getProductReviews(int productId) {
        return service.getAllReviews().stream()
                .filter(r -> r.getProductId() == productId)
                .collect(Collectors.toList());
    }

    public List<Transaction> getProductTransactions(int productId) {
        return service.getAllTransactions().stream()
                .filter(t -> t.getProductId() == productId)
                .collect(Collectors.toList());
    }

    /**
     * Generates real dynamic business insights calculated strictly from live project data.
     */
    public List<String> generateDynamicInsights() {
        List<String> insights = new ArrayList<>();

        List<Product> lowStock = getLowStockProducts(15);
        if (!lowStock.isEmpty()) {
            insights.add(String.format("Critical Stock Alert: %d product%s below reorder threshold (<=15 units). Lowest: %s (%d left).",
                    lowStock.size(), lowStock.size() > 1 ? "s are" : " is", lowStock.get(0).getName(), lowStock.get(0).getStock()));
        } else {
            insights.add("Inventory Health: All product inventory levels are currently above healthy reorder safety margins.");
        }

        Map<String, Double> revByCat = getRevenueByCategory();
        if (!revByCat.isEmpty()) {
            Map.Entry<String, Double> topCat = revByCat.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            double totalRev = getTotalRevenue();
            if (topCat != null && totalRev > 0) {
                double pct = (topCat.getValue() / totalRev) * 100.0;
                insights.add(String.format("Top Revenue Driver: Category '%s' dominates sales at Rs. %,.0f (%.1f%% of overall revenue).",
                        topCat.getKey(), topCat.getValue(), pct));
            }
        }

        List<ProductSalesStats> topProducts = getTopSellingProducts();
        if (!topProducts.isEmpty()) {
            ProductSalesStats best = topProducts.get(0);
            insights.add(String.format("Best Seller: %s leads order volume with %d units sold generating Rs. %,.0f in sales.",
                    best.getProduct().getName(), best.getQuantitySold(), best.getTotalRevenue()));
        }

        List<Review> reviews = service.getAllReviews();
        if (!reviews.isEmpty()) {
            double avgRating = getAverageReviewRating();
            long fiveStars = reviews.stream().filter(r -> r.getRating() == 5).count();
            double pctFive = (double) fiveStars / reviews.size() * 100.0;
            insights.add(String.format("Customer Satisfaction: Catalog maintains %.2f/5.0 average score; %.0f%% of reviews award top 5-star rating.",
                    avgRating, pctFive));
        }

        int totalCap = getTotalWarehouseCapacity();
        if (totalCap > 0) {
            double util = getWarehouseUtilizationRate();
            insights.add(String.format("Supply Chain Capacity: Network utilization stands at %.1f%% across %d regional distribution hubs.",
                    util, service.getAllWarehouses().size()));
        }

        return insights;
    }
}
