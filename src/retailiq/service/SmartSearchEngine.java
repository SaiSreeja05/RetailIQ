package retailiq.service;

import DynamicProgramming.Levenshtein;
import StringAlgorithms.KMP;
import StringAlgorithms.RabinKarp;
import retailiq.*;

import java.util.*;

public class SmartSearchEngine {

    public enum EntityType {
        PRODUCT, CUSTOMER, TRANSACTION, REVIEW, WAREHOUSE
    }

    public static class SearchResult {
        private final EntityType type;
        private final String title;
        private final String subtitle;
        private final String detail;
        private final Object entity;
        private final int score; // Higher is better match

        public SearchResult(EntityType type, String title, String subtitle, String detail, Object entity, int score) {
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.detail = detail;
            this.entity = entity;
            this.score = score;
        }

        public EntityType getType() { return type; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getDetail() { return detail; }
        public Object getEntity() { return entity; }
        public int getScore() { return score; }
    }

    private final RetailIQService service;

    public SmartSearchEngine(RetailIQService service) {
        this.service = service;
    }

    public List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String q = query.trim().toLowerCase();

        // 1. Products
        for (Product p : service.getAllProducts()) {
            int score = calculateMatchScore(q, p.getName(), p.getCategory(), p.getProductCode(), String.valueOf(p.getProductId()), p.getDescription());
            if (score > 0) {
                results.add(new SearchResult(
                        EntityType.PRODUCT,
                        p.getName(),
                        "Product #" + p.getProductId() + " • " + p.getCategory() + " • " + p.getProductCode(),
                        String.format("Price: Rs. %,.0f | Stock: %d | Rating: %.1f★", p.getPrice(), p.getStock(), p.getRating()),
                        p,
                        score
                ));
            }
        }

        // 2. Customers
        for (Customer c : service.getAllCustomers()) {
            int score = calculateMatchScore(q, c.getName(), c.getEmail(), c.getPhone(), c.getAddress(), String.valueOf(c.getCustomerId()));
            if (score > 0) {
                results.add(new SearchResult(
                        EntityType.CUSTOMER,
                        c.getName(),
                        "Customer #" + c.getCustomerId() + " • " + c.getEmail(),
                        "Phone: " + c.getPhone() + " | Address: " + c.getAddress(),
                        c,
                        score
                ));
            }
        }

        // 3. Transactions
        for (Transaction t : service.getAllTransactions()) {
            int score = calculateMatchScore(q, t.getTransactionId(), t.getTransactionDate(), String.valueOf(t.getCustomerId()), String.valueOf(t.getProductId()));
            if (score > 0) {
                results.add(new SearchResult(
                        EntityType.TRANSACTION,
                        "Transaction " + t.getTransactionId(),
                        "Date: " + t.getTransactionDate() + " • Customer #" + t.getCustomerId() + " • Product #" + t.getProductId(),
                        String.format("Qty: %d | Total: Rs. %,.0f", t.getQuantity(), t.getTotalAmount()),
                        t,
                        score
                ));
            }
        }

        // 4. Reviews
        for (Review r : service.getAllReviews()) {
            int score = calculateMatchScore(q, r.getReviewText(), String.valueOf(r.getReviewId()), String.valueOf(r.getProductId()), String.valueOf(r.getCustomerId()));
            if (score > 0) {
                results.add(new SearchResult(
                        EntityType.REVIEW,
                        "Review #" + r.getReviewId() + " (" + r.getRating() + "★)",
                        "Product #" + r.getProductId() + " • Customer #" + r.getCustomerId(),
                        r.getReviewText(),
                        r,
                        score
                ));
            }
        }

        // 5. Warehouses
        for (Warehouse w : service.getAllWarehouses()) {
            int score = calculateMatchScore(q, w.getWarehouseName(), w.getLocation(), String.valueOf(w.getWarehouseId()));
            if (score > 0) {
                results.add(new SearchResult(
                        EntityType.WAREHOUSE,
                        w.getWarehouseName(),
                        "Warehouse #" + w.getWarehouseId() + " • " + w.getLocation(),
                        "Storage Capacity: " + w.getCapacity() + " units",
                        w,
                        score
                ));
            }
        }

        // Sort descending by match score
        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return results;
    }

    private int calculateMatchScore(String query, String... fields) {
        int bestScore = 0;

        for (String field : fields) {
            if (field == null || field.isEmpty()) continue;
            String f = field.toLowerCase();

            // Exact match
            if (f.equals(query)) {
                return 100;
            }

            // Starts with query
            if (f.startsWith(query)) {
                bestScore = Math.max(bestScore, 85);
            }

            // Substring search via KMP algorithm
            if (KMP.search(f, query) != -1) {
                bestScore = Math.max(bestScore, 70);
            }

            // Substring search via Rabin-Karp algorithm
            if (RabinKarp.contains(f, query)) {
                bestScore = Math.max(bestScore, 70);
            }

            // Fuzzy typo-tolerant search via Levenshtein distance
            // Check individual words in field
            String[] tokens = f.split("\\s+");
            for (String token : tokens) {
                if (token.length() >= 3 && query.length() >= 3) {
                    int dist = Levenshtein.distance(query, token);
                    if (dist == 1) {
                        bestScore = Math.max(bestScore, 60);
                    } else if (dist == 2 && query.length() >= 4) {
                        bestScore = Math.max(bestScore, 45);
                    }
                }
            }
        }

        return bestScore;
    }
}
