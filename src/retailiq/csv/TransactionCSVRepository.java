package retailiq.csv;

import retailiq.Transaction;
import retailiq.TransactionDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionCSVRepository {

    private final String filePath;
    private final List<Transaction> transactions = new ArrayList<>();

    public TransactionCSVRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    public synchronized void load() {
        transactions.clear();
        try {
            List<String> lines = CSVUtils.readLines(filePath);
            if (lines.isEmpty()) {
                seedDefaults();
                return;
            }

            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("transactionid")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = CSVUtils.parseLine(line);
                if (cols.size() >= 6) {
                    String id = cols.get(0);
                    int custId = CSVUtils.parseInt(cols.get(1), 0);
                    int prodId = CSVUtils.parseInt(cols.get(2), 0);
                    int qty = CSVUtils.parseInt(cols.get(3), 1);
                    double total = CSVUtils.parseDouble(cols.get(4), 0.0);
                    String date = cols.get(5);

                    Transaction transaction = new Transaction(id, custId, prodId, qty, total, date);
                    DataValidator.ValidationResult val = DataValidator.validateTransaction(transaction);
                    if (val.isValid()) {
                        transactions.add(transaction);
                    } else {
                        System.err.println("Skipping invalid transaction row: " + val.getErrorMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load transactions CSV: " + e.getMessage());
            seedDefaults();
        }
    }

    private void seedDefaults() {
        TransactionDatabase db = new TransactionDatabase();
        transactions.addAll(db.getTransactions());
        persist();
    }

    public synchronized void persist() {
        List<String> lines = new ArrayList<>();
        lines.add("transactionId,customerId,productId,quantity,totalAmount,transactionDate");
        for (Transaction t : transactions) {
            lines.add(CSVUtils.formatLine(
                    t.getTransactionId(),
                    t.getCustomerId(),
                    t.getProductId(),
                    t.getQuantity(),
                    t.getTotalAmount(),
                    t.getTransactionDate()
            ));
        }
        try {
            CSVUtils.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println("Could not write transactions CSV: " + e.getMessage());
        }
    }

    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(transactions);
    }

    public synchronized Optional<Transaction> findById(String id) {
        return transactions.stream().filter(t -> t.getTransactionId().equalsIgnoreCase(id)).findFirst();
    }

    public synchronized void save(Transaction transaction) {
        int index = -1;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId().equalsIgnoreCase(transaction.getTransactionId())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            transactions.set(index, transaction);
        } else {
            transactions.add(transaction);
        }
        persist();
    }

    public synchronized boolean deleteById(String id) {
        boolean removed = transactions.removeIf(t -> t.getTransactionId().equalsIgnoreCase(id));
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized void replaceAll(List<Transaction> newTransactions) {
        transactions.clear();
        transactions.addAll(newTransactions);
        persist();
    }
}
