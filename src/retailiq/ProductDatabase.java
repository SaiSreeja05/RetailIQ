package retailiq;

import java.util.ArrayList;

public class ProductDatabase {

    private ArrayList<Product> products;

    public ProductDatabase() {

        products = new ArrayList<>();

        products.add(new Product(
                101,
                "Apple iPhone 15",
                "Electronics",
                "Apple iPhone 15 with advanced camera and powerful performance",
                69999,
                25,
                4.5,
                "IPH15-001"
        ));

        products.add(new Product(
                102,
                "Apple iPhone 15 Pro",
                "Electronics",
                "Apple iPhone 15 Pro with titanium design and advanced camera",
                119999,
                15,
                4.7,
                "IPH15P-002"
        ));

        products.add(new Product(
                103,
                "Samsung Galaxy S25",
                "Electronics",
                "Samsung Galaxy smartphone with powerful processor and camera",
                79999,
                20,
                4.6,
                "SAM25-003"
        ));

        products.add(new Product(
                104,
                "HP Pavilion Laptop",
                "Computers",
                "HP Pavilion laptop with high performance processor and large storage",
                64999,
                12,
                4.4,
                "HPPAV-004"
        ));

        products.add(new Product(
                105,
                "Apple MacBook Air",
                "Computers",
                "Apple MacBook Air with powerful performance and long battery life",
                99999,
                10,
                4.8,
                "MBAIR-005"
        ));

        products.add(new Product(
                106,
                "Sony Wireless Headphones",
                "Audio",
                "Sony wireless headphones with noise cancellation and long battery life",
                8999,
                35,
                4.5,
                "SONYHP-006"
        ));

        products.add(new Product(
                107,
                "Logitech Wireless Mouse",
                "Accessories",
                "Logitech wireless mouse with comfortable design and accurate tracking",
                1499,
                50,
                4.3,
                "LOGM-007"
        ));

        products.add(new Product(
                108,
                "Mechanical Gaming Keyboard",
                "Accessories",
                "Mechanical gaming keyboard with RGB lighting and fast response",
                3499,
                30,
                4.4,
                "GAMEKB-008"
        ));

        products.add(new Product(
                109,
                "Dell 24 Inch Monitor",
                "Computers",
                "Dell monitor with full HD display and comfortable viewing",
                12999,
                18,
                4.5,
                "DELL24-009"
        ));

        products.add(new Product(
                110,
                "Samsung Smart Watch",
                "Wearables",
                "Samsung smart watch with health tracking and activity monitoring",
                15999,
                22,
                4.2,
                "SAMWATCH-010"
        ));
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void displayAllProducts() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("                 RETAILIQ PRODUCTS");
        System.out.println("=================================================");

        for (Product product : products) {

            System.out.println();
            product.displayProduct();
            System.out.println("-----------------------------------------------");
        }
    }

    public Product findProductById(int productId) {

        for (Product product : products) {

            if (product.getProductId() == productId) {
                return product;
            }
        }

        return null;
    }
}