package retailiq.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import retailiq.*;
import retailiq.service.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RetailIQWebServer {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;
    private final SmartSearchEngine searchEngine;
    private final AlgorithmLabService lab;
    private final int port;
    private HttpServer server;

    public RetailIQWebServer(int port) {
        this.port = port;
        this.service = RetailIQService.getInstance();
        this.analytics = new AnalyticsEngine(service);
        this.searchEngine = new SmartSearchEngine(service);
        this.lab = new AlgorithmLabService(service);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API contexts
        server.createContext("/api/analytics", this::handleAnalytics);
        server.createContext("/api/products", this::handleProducts);
        server.createContext("/api/customers", this::handleCustomers);
        server.createContext("/api/transactions", this::handleTransactions);
        server.createContext("/api/reviews", this::handleReviews);
        server.createContext("/api/warehouses", this::handleWarehouses);
        server.createContext("/api/search", this::handleSearch);
        server.createContext("/api/algorithms", this::handleAlgorithms);
        server.createContext("/api/data", this::handleDataManagement);

        // Static files handler
        server.createContext("/", this::handleStatic);

        server.setExecutor(null);
        server.start();
        System.out.println("==================================================");
        System.out.println("  RetailIQ Web Server running at: http://localhost:" + port);
        System.out.println("==================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // Handlers
    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        File file = new File("web" + path);
        if (!file.exists() || file.isDirectory()) {
            // fallback to index.html for SPA routing
            file = new File("web/index.html");
        }

        if (!file.exists()) {
            sendResponse(exchange, 404, "text/plain", "File not found: " + path);
            return;
        }

        String mime = "text/plain";
        if (path.endsWith(".html")) mime = "text/html; charset=utf-8";
        else if (path.endsWith(".css")) mime = "text/css; charset=utf-8";
        else if (path.endsWith(".js")) mime = "application/javascript; charset=utf-8";
        else if (path.endsWith(".json")) mime = "application/json; charset=utf-8";
        else if (path.endsWith(".png")) mime = "image/png";

        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleAnalytics(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }

        StringBuilder json = new StringBuilder("{");
        json.append("\"totalProducts\":").append(analytics.getTotalProducts()).append(",");
        json.append("\"totalCustomers\":").append(analytics.getTotalCustomers()).append(",");
        json.append("\"totalTransactions\":").append(analytics.getTotalTransactions()).append(",");
        json.append("\"totalRevenue\":").append(analytics.getTotalRevenue()).append(",");
        json.append("\"averageOrderValue\":").append(analytics.getAverageOrderValue()).append(",");
        json.append("\"averageRating\":").append(analytics.getAverageReviewRating()).append(",");
        json.append("\"totalInventory\":").append(analytics.getTotalInventoryUnits()).append(",");
        json.append("\"totalCapacity\":").append(analytics.getTotalWarehouseCapacity()).append(",");
        json.append("\"warehouseUtilization\":").append(analytics.getWarehouseUtilizationRate()).append(",");

        // Dynamic insights
        json.append("\"insights\":").append(JsonUtils.stringListToJson(analytics.generateDynamicInsights())).append(",");

        // Sales by date
        json.append("\"salesByDate\":").append(JsonUtils.stringDoubleMapToJson(analytics.getSalesByDate())).append(",");

        // Revenue by category
        json.append("\"revenueByCategory\":").append(JsonUtils.stringDoubleMapToJson(analytics.getRevenueByCategory())).append(",");

        // Product count by category
        json.append("\"productsByCategory\":").append(JsonUtils.stringIntMapToJson(analytics.getProductCountByCategory())).append(",");

        // Rating distribution
        json.append("\"ratingDistribution\":").append(JsonUtils.intIntMapToJson(analytics.getRatingDistribution())).append(",");

        // Top products
        json.append("\"topProducts\":[");
        List<AnalyticsEngine.ProductSalesStats> tops = analytics.getTopSellingProducts();
        for (int i = 0; i < tops.size(); i++) {
            if (i > 0) json.append(",");
            AnalyticsEngine.ProductSalesStats s = tops.get(i);
            json.append(String.format("{\"product\":%s,\"quantitySold\":%d,\"totalRevenue\":%.2f}",
                    JsonUtils.productToJson(s.getProduct()), s.getQuantitySold(), s.getTotalRevenue()));
        }
        json.append("],");

        // Low stock products
        json.append("\"lowStockProducts\":[");
        List<Product> low = analytics.getLowStockProducts(15);
        for (int i = 0; i < low.size(); i++) {
            if (i > 0) json.append(",");
            json.append(JsonUtils.productToJson(low.get(i)));
        }
        json.append("]}");

        sendResponse(exchange, 200, "application/json", json.toString());
    }

    private void handleProducts(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Product> list = service.getAllProducts();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.productToJson(list.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (method.equalsIgnoreCase("POST")) {
            String body = readBody(exchange);
            Map<String, String> params = parseFormOrQuery(body);
            try {
                int id = Integer.parseInt(params.getOrDefault("productId", "0"));
                String name = params.getOrDefault("name", "");
                String cat = params.getOrDefault("category", "");
                String desc = params.getOrDefault("description", "");
                double price = Double.parseDouble(params.getOrDefault("price", "0"));
                int stock = Integer.parseInt(params.getOrDefault("stock", "0"));
                double rating = Double.parseDouble(params.getOrDefault("rating", "5.0"));
                String code = params.getOrDefault("productCode", "");

                if (id == 0) {
                    id = service.getAllProducts().stream().mapToInt(Product::getProductId).max().orElse(100) + 1;
                }

                Product p = new Product(id, name, cat, desc, price, stock, rating, code);
                service.saveProduct(p);
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"product\":" + JsonUtils.productToJson(p) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"" + JsonUtils.escape(e.getMessage()) + "\"}");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));
            boolean ok = service.deleteProduct(id);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + ok + "}");
        }
    }

    private void handleCustomers(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Customer> list = service.getAllCustomers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.customerToJson(list.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (method.equalsIgnoreCase("POST")) {
            String body = readBody(exchange);
            Map<String, String> params = parseFormOrQuery(body);
            try {
                int id = Integer.parseInt(params.getOrDefault("customerId", "0"));
                String name = params.getOrDefault("name", "");
                String email = params.getOrDefault("email", "");
                String phone = params.getOrDefault("phone", "");
                String address = params.getOrDefault("address", "");

                if (id == 0) {
                    id = service.getAllCustomers().stream().mapToInt(Customer::getCustomerId).max().orElse(200) + 1;
                }

                Customer c = new Customer(id, name, email, phone, address);
                service.saveCustomer(c);
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"customer\":" + JsonUtils.customerToJson(c) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"" + JsonUtils.escape(e.getMessage()) + "\"}");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));
            boolean ok = service.deleteCustomer(id);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + ok + "}");
        }
    }

    private void handleTransactions(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Transaction> list = service.getAllTransactions();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.transactionToJson(list.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (method.equalsIgnoreCase("POST")) {
            String body = readBody(exchange);
            Map<String, String> params = parseFormOrQuery(body);
            try {
                String id = params.getOrDefault("transactionId", "TXN" + (1000 + service.getAllTransactions().size() + 1));
                int custId = Integer.parseInt(params.getOrDefault("customerId", "0"));
                int prodId = Integer.parseInt(params.getOrDefault("productId", "0"));
                int qty = Integer.parseInt(params.getOrDefault("quantity", "1"));
                double total = Double.parseDouble(params.getOrDefault("totalAmount", "0"));
                String date = params.getOrDefault("transactionDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                if (total == 0) {
                    Optional<Product> opt = service.getProductRepo().findById(prodId);
                    if (opt.isPresent()) total = opt.get().getPrice() * qty;
                }

                Transaction t = new Transaction(id, custId, prodId, qty, total, date);
                service.saveTransaction(t);
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"transaction\":" + JsonUtils.transactionToJson(t) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"" + JsonUtils.escape(e.getMessage()) + "\"}");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String id = q.getOrDefault("id", "");
            boolean ok = service.deleteTransaction(id);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + ok + "}");
        }
    }

    private void handleReviews(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Review> list = service.getAllReviews();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.reviewToJson(list.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (method.equalsIgnoreCase("POST")) {
            String body = readBody(exchange);
            Map<String, String> params = parseFormOrQuery(body);
            try {
                int id = Integer.parseInt(params.getOrDefault("reviewId", "0"));
                int prodId = Integer.parseInt(params.getOrDefault("productId", "0"));
                int custId = Integer.parseInt(params.getOrDefault("customerId", "0"));
                String text = params.getOrDefault("reviewText", "");
                int rating = Integer.parseInt(params.getOrDefault("rating", "5"));

                if (id == 0) {
                    id = service.getAllReviews().stream().mapToInt(Review::getReviewId).max().orElse(300) + 1;
                }

                Review r = new Review(id, prodId, custId, text, rating);
                service.saveReview(r);
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"review\":" + JsonUtils.reviewToJson(r) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"" + JsonUtils.escape(e.getMessage()) + "\"}");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));
            boolean ok = service.deleteReview(id);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + ok + "}");
        }
    }

    private void handleWarehouses(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Warehouse> list = service.getAllWarehouses();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.warehouseToJson(list.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (method.equalsIgnoreCase("POST")) {
            String body = readBody(exchange);
            Map<String, String> params = parseFormOrQuery(body);
            try {
                int id = Integer.parseInt(params.getOrDefault("warehouseId", "0"));
                String name = params.getOrDefault("warehouseName", "");
                String loc = params.getOrDefault("location", "");
                int cap = Integer.parseInt(params.getOrDefault("capacity", "5000"));

                if (id == 0) {
                    id = service.getAllWarehouses().stream().mapToInt(Warehouse::getWarehouseId).max().orElse(400) + 1;
                }

                Warehouse w = new Warehouse(id, name, loc, cap);
                service.saveWarehouse(w);
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"warehouse\":" + JsonUtils.warehouseToJson(w) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"" + JsonUtils.escape(e.getMessage()) + "\"}");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));
            boolean ok = service.deleteWarehouse(id);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + ok + "}");
        }
    }

    private void handleSearch(HttpExchange exchange) throws IOException {
        Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
        String query = q.getOrDefault("q", "");
        List<SmartSearchEngine.SearchResult> list = searchEngine.search(query);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            SmartSearchEngine.SearchResult r = list.get(i);
            sb.append(String.format("{\"type\":\"%s\",\"title\":\"%s\",\"subtitle\":\"%s\",\"detail\":\"%s\",\"score\":%d}",
                    r.getType(), JsonUtils.escape(r.getTitle()), JsonUtils.escape(r.getSubtitle()),
                    JsonUtils.escape(r.getDetail()), r.getScore()));
        }
        sb.append("]");
        sendResponse(exchange, 200, "application/json", sb.toString());
    }

    private void handleAlgorithms(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());

        if (path.endsWith("/bitmask")) {
            int budget = Integer.parseInt(q.getOrDefault("budget", "100000"));
            AlgorithmLabService.BundleResult r = lab.runBitmaskBundleOptimization(budget);
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"maxScore\":").append(r.maxScore).append(",");
            sb.append("\"totalPrice\":").append(r.totalPrice).append(",");
            sb.append("\"explanation\":\"").append(JsonUtils.escape(r.explanation)).append("\",");
            sb.append("\"selectedProducts\":[");
            for (int i = 0; i < r.selectedProducts.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtils.productToJson(r.selectedProducts.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (path.endsWith("/levenshtein")) {
            String s1 = q.getOrDefault("s1", "ipohne");
            String s2 = q.getOrDefault("s2", "iPhone 15");
            AlgorithmLabService.LevenshteinResult r = lab.runLevenshtein(s1, s2);
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"distance\":").append(r.distance).append(",");
            sb.append("\"isClose\":").append(r.isClose).append(",");
            sb.append("\"explanation\":\"").append(JsonUtils.escape(r.explanation)).append("\",");
            sb.append("\"s1\":\"").append(JsonUtils.escape(r.s1)).append("\",");
            sb.append("\"s2\":\"").append(JsonUtils.escape(r.s2)).append("\"}");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (path.endsWith("/matrixchain")) {
            String dimsStr = q.getOrDefault("dims", "10,20,30,40,30");
            String[] parts = dimsStr.split(",");
            int[] dims = new int[parts.length];
            for (int i = 0; i < parts.length; i++) dims[i] = Integer.parseInt(parts[i].trim());
            AlgorithmLabService.MatrixChainResult r = lab.runMatrixChain(dims);
            String json = String.format("{\"minCost\":%d,\"explanation\":\"%s\"}", r.minCost, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/ahocorasick")) {
            String body = readBody(exchange);
            Map<String, String> p = parseFormOrQuery(body.isEmpty() ? exchange.getRequestURI().getQuery() : body);
            String text = p.getOrDefault("text", "The iPhone Pro has an excellent camera and smooth performance.");
            String kwsStr = p.getOrDefault("keywords", "camera,performance,battery,design,smooth");
            List<String> kws = Arrays.stream(kwsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            AlgorithmLabService.KeywordMatchResult r = lab.runAhoCorasick(text, kws);
            String json = String.format("{\"matchedKeywords\":%s,\"explanation\":\"%s\"}",
                    JsonUtils.stringListToJson(r.matchedKeywords), JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/kmp")) {
            String text = q.getOrDefault("text", "Apple iPhone 15 Pro with titanium design");
            String pattern = q.getOrDefault("pattern", "titanium");
            AlgorithmLabService.StringSearchResult r = lab.runKMP(text, pattern);
            String json = String.format("{\"found\":%b,\"index\":%d,\"explanation\":\"%s\"}", r.found, r.index, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/rabinkarp")) {
            String text = q.getOrDefault("text", "Apple iPhone 15 Pro with titanium design");
            String pattern = q.getOrDefault("pattern", "titanium");
            AlgorithmLabService.StringSearchResult r = lab.runRabinKarp(text, pattern);
            String json = String.format("{\"found\":%b,\"index\":%d,\"explanation\":\"%s\"}", r.found, r.index, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/zfunction")) {
            String text = q.getOrDefault("text", "Apple iPhone 15 Pro with titanium design");
            String pattern = q.getOrDefault("pattern", "titanium");
            AlgorithmLabService.ZFunctionResult r = lab.runZFunction(text, pattern);
            String json = String.format("{\"contains\":%b,\"explanation\":\"%s\"}", r.contains, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/suffixarray")) {
            String text = q.getOrDefault("text", "retailiq");
            AlgorithmLabService.SuffixArrayKasaiResult r = lab.runSuffixArrayAndKasai(text);
            StringBuilder sb = new StringBuilder("{\"explanation\":\"").append(JsonUtils.escape(r.explanation)).append("\",\"entries\":[");
            for (int i = 0; i < r.suffixArray.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("{\"rank\":%d,\"pos\":%d,\"lcp\":%d,\"suffix\":\"%s\"}",
                        i, r.suffixArray[i], r.lcpArray[i], JsonUtils.escape(r.suffixStrings.get(i))));
            }
            sb.append("]}");
            sendResponse(exchange, 200, "application/json", sb.toString());
        } else if (path.endsWith("/suffixautomaton")) {
            String text = q.getOrDefault("text", "electronics_catalog_inventory");
            String pattern = q.getOrDefault("pattern", "catalog");
            AlgorithmLabService.SuffixQueryResult r = lab.runSuffixAutomaton(text, pattern);
            String json = String.format("{\"found\":%b,\"explanation\":\"%s\"}", r.found, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else if (path.endsWith("/suffixtree")) {
            String text = q.getOrDefault("text", "electronics_catalog_inventory");
            String pattern = q.getOrDefault("pattern", "catalog");
            AlgorithmLabService.SuffixQueryResult r = lab.runSuffixTree(text, pattern);
            String json = String.format("{\"found\":%b,\"explanation\":\"%s\"}", r.found, JsonUtils.escape(r.explanation));
            sendResponse(exchange, 200, "application/json", json);
        } else {
            sendResponse(exchange, 404, "text/plain", "Unknown algorithm endpoint");
        }
    }

    private void handleDataManagement(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/reload")) {
            service.reloadAll();
            sendResponse(exchange, 200, "application/json", "{\"success\":true,\"message\":\"All CSV data reloaded successfully\"}");
        } else if (path.endsWith("/save")) {
            service.persistAll();
            sendResponse(exchange, 200, "application/json", "{\"success\":true,\"message\":\"All data flushed to CSV successfully\"}");
        } else if (path.endsWith("/backup")) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupDir = new File("data/backups/backup_" + timestamp);
            if (!backupDir.exists()) backupDir.mkdirs();
            String[] files = {"products.csv", "customers.csv", "reviews.csv", "transactions.csv", "warehouses.csv"};
            for (String fName : files) {
                File src = new File("data/" + fName);
                if (src.exists()) {
                    Files.copy(src.toPath(), new File(backupDir, fName).toPath());
                }
            }
            sendResponse(exchange, 200, "application/json", "{\"success\":true,\"backupPath\":\"" + JsonUtils.escape(backupDir.getAbsolutePath()) + "\"}");
        } else {
            // Status
            String[] files = {"products.csv", "customers.csv", "reviews.csv", "transactions.csv", "warehouses.csv"};
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < files.length; i++) {
                if (i > 0) sb.append(",");
                File f = new File("data/" + files[i]);
                sb.append(String.format("{\"file\":\"%s\",\"path\":\"%s\",\"size\":%d,\"exists\":%b}",
                        files[i], JsonUtils.escape(f.getAbsolutePath()), f.exists() ? f.length() : 0, f.exists()));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // Helpers
    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2048];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseFormOrQuery(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        // Try JSON if starts with {
        if (body.trim().startsWith("{")) {
            // Simple key-value JSON parsing
            String trimmed = body.trim();
            trimmed = trimmed.substring(1, trimmed.length() - 1);
            String[] pairs = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
            return map;
        }

        return parseQuery(body);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    map.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
                } else if (idx == -1) {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }
        try {
            RetailIQWebServer webServer = new RetailIQWebServer(port);
            webServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
