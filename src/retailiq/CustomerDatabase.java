package retailiq;

import java.util.ArrayList;

public class CustomerDatabase {

    private ArrayList<Customer> customers;

    public CustomerDatabase() {

        customers = new ArrayList<>();

        customers.add(new Customer(
                201,
                "Rahul",
                "rahul@example.com",
                "9000000001",
                "Hyderabad"
        ));

        customers.add(new Customer(
                202,
                "Ananya",
                "ananya@example.com",
                "9000000002",
                "Vijayawada"
        ));

        customers.add(new Customer(
                203,
                "Kiran",
                "kiran@example.com",
                "9000000003",
                "Bengaluru"
        ));

        customers.add(new Customer(
                204,
                "Priya",
                "priya@example.com",
                "9000000004",
                "Chennai"
        ));

        customers.add(new Customer(
                205,
                "Arjun",
                "arjun@example.com",
                "9000000005",
                "Kochi"
        ));

        customers.add(new Customer(
                206,
                "Sneha",
                "sneha@example.com",
                "9000000006",
                "Mumbai"
        ));

        customers.add(new Customer(
                207,
                "Vikram",
                "vikram@example.com",
                "9000000007",
                "Delhi"
        ));

        customers.add(new Customer(
                208,
                "Meera",
                "meera@example.com",
                "9000000008",
                "Pune"
        ));
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void displayAllCustomers() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("                 RETAILIQ CUSTOMERS");
        System.out.println("=================================================");

        for (Customer customer : customers) {

            System.out.println();
            customer.displayCustomer();
            System.out.println("-----------------------------------------------");
        }
    }

    public Customer findCustomerById(int customerId) {

        for (Customer customer : customers) {

            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }

        return null;
    }
}