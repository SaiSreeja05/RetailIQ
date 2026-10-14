package retailiq;

public class Product {

    private int productId;
    private String name;
    private String category;
    private String description;
    private double price;
    private int stock;
    private double rating;
    private String productCode;

    public Product(int productId, String name, String category,
                   String description, double price, int stock,
                   double rating, String productCode) {

        this.productId = productId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.productCode = productCode;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public double getRating() {
        return rating;
    }

    public String getProductCode() {
        return productCode;
    }

    public void displayProduct() {
        System.out.println("Product ID   : " + productId);
        System.out.println("Product Name : " + name);
        System.out.println("Category     : " + category);
        System.out.println("Description  : " + description);
        System.out.println("Price        : Rs. " + price);
        System.out.println("Stock        : " + stock);
        System.out.println("Rating       : " + rating + "/5");
        System.out.println("Product Code : " + productCode);
    }
}