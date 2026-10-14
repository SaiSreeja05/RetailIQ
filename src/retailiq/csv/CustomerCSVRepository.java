package retailiq.csv;

import retailiq.Customer;
import retailiq.CustomerDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerCSVRepository {

    private final String filePath;
    private final List<Customer> customers = new ArrayList<>();

    public CustomerCSVRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    public synchronized void load() {
        customers.clear();
        try {
            List<String> lines = CSVUtils.readLines(filePath);
            if (lines.isEmpty()) {
                seedDefaults();
                return;
            }

            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("customerid")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = CSVUtils.parseLine(line);
                if (cols.size() >= 5) {
                    int id = CSVUtils.parseInt(cols.get(0), 0);
                    String name = cols.get(1);
                    String email = cols.get(2);
                    String phone = cols.get(3);
                    String address = cols.get(4);

                    Customer customer = new Customer(id, name, email, phone, address);
                    DataValidator.ValidationResult val = DataValidator.validateCustomer(customer);
                    if (val.isValid()) {
                        customers.add(customer);
                    } else {
                        System.err.println("Skipping invalid customer row: " + val.getErrorMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load customers CSV: " + e.getMessage());
            seedDefaults();
        }
    }

    private void seedDefaults() {
        CustomerDatabase db = new CustomerDatabase();
        customers.addAll(db.getCustomers());
        persist();
    }

    public synchronized void persist() {
        List<String> lines = new ArrayList<>();
        lines.add("customerId,name,email,phone,address");
        for (Customer c : customers) {
            lines.add(CSVUtils.formatLine(
                    c.getCustomerId(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress()
            ));
        }
        try {
            CSVUtils.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println("Could not write customers CSV: " + e.getMessage());
        }
    }

    public synchronized List<Customer> findAll() {
        return new ArrayList<>(customers);
    }

    public synchronized Optional<Customer> findById(int id) {
        return customers.stream().filter(c -> c.getCustomerId() == id).findFirst();
    }

    public synchronized void save(Customer customer) {
        int index = -1;
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustomerId() == customer.getCustomerId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            customers.set(index, customer);
        } else {
            customers.add(customer);
        }
        persist();
    }

    public synchronized boolean deleteById(int id) {
        boolean removed = customers.removeIf(c -> c.getCustomerId() == id);
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized void replaceAll(List<Customer> newCustomers) {
        customers.clear();
        customers.addAll(newCustomers);
        persist();
    }
}
