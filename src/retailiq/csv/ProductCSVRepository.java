package retailiq.csv;

import retailiq.Product;
import retailiq.ProductDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductCSVRepository {

    private final String filePath;
    private final List<Product> products = new ArrayList<>();

    public ProductCSVRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    public synchronized void load() {
        products.clear();
        try {
            List<String> lines = CSVUtils.readLines(filePath);
            if (lines.isEmpty()) {
                seedDefaults();
                return;
            }

            // Skip header if present
            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("productid")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = CSVUtils.parseLine(line);
                if (cols.size() >= 8) {
                    int id = CSVUtils.parseInt(cols.get(0), 0);
                    String name = cols.get(1);
                    String category = cols.get(2);
                    String description = cols.get(3);
                    double price = CSVUtils.parseDouble(cols.get(4), 0.0);
                    int stock = CSVUtils.parseInt(cols.get(5), 0);
                    double rating = CSVUtils.parseDouble(cols.get(6), 0.0);
                    String code = cols.get(7);

                    Product product = new Product(id, name, category, description, price, stock, rating, code);
                    DataValidator.ValidationResult val = DataValidator.validateProduct(product);
                    if (val.isValid()) {
                        products.add(product);
                    } else {
                        System.err.println("Skipping invalid product row: " + val.getErrorMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load products CSV: " + e.getMessage());
            seedDefaults();
        }
    }

    private void seedDefaults() {
        ProductDatabase db = new ProductDatabase();
        products.addAll(db.getProducts());
        persist();
    }

    public synchronized void persist() {
        List<String> lines = new ArrayList<>();
        lines.add("productId,name,category,description,price,stock,rating,productCode");
        for (Product p : products) {
            lines.add(CSVUtils.formatLine(
                    p.getProductId(),
                    p.getName(),
                    p.getCategory(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getStock(),
                    p.getRating(),
                    p.getProductCode()
            ));
        }
        try {
            CSVUtils.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println("Could not write products CSV: " + e.getMessage());
        }
    }

    public synchronized List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public synchronized Optional<Product> findById(int id) {
        return products.stream().filter(p -> p.getProductId() == id).findFirst();
    }

    public synchronized void save(Product product) {
        int index = -1;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductId() == product.getProductId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            products.set(index, product);
        } else {
            products.add(product);
        }
        persist();
    }

    public synchronized boolean deleteById(int id) {
        boolean removed = products.removeIf(p -> p.getProductId() == id);
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized void replaceAll(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        persist();
    }
}
