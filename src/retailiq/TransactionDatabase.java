package retailiq;

import java.util.ArrayList;

public class TransactionDatabase {

    private ArrayList<Transaction> transactions;

    public TransactionDatabase() {

        transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TXN1001",
                201,
                101,
                1,
                69999,
                "2026-09-01"
        ));

        transactions.add(new Transaction(
                "TXN1002",
                202,
                102,
                1,
                119999,
                "2026-09-02"
        ));

        transactions.add(new Transaction(
                "TXN1003",
                203,
                103,
                2,
                159998,
                "2026-09-03"
        ));

        transactions.add(new Transaction(
                "TXN1004",
                204,
                104,
                1,
                64999,
                "2026-09-04"
        ));

        transactions.add(new Transaction(
                "TXN1005",
                205,
                105,
                1,
                99999,
                "2026-09-05"
        ));

        transactions.add(new Transaction(
                "TXN1006",
                206,
                106,
                2,
                17998,
                "2026-09-06"
        ));

        transactions.add(new Transaction(
                "TXN1007",
                207,
                107,
                3,
                4497,
                "2026-09-07"
        ));

        transactions.add(new Transaction(
                "TXN1008",
                208,
                108,
                1,
                3499,
                "2026-09-08"
        ));

        transactions.add(new Transaction(
                "TXN1009",
                201,
                109,
                2,
                25998,
                "2026-09-09"
        ));

        transactions.add(new Transaction(
                "TXN1010",
                202,
                110,
                1,
                15999,
                "2026-09-10"
        ));
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void displayAllTransactions() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("              RETAILIQ TRANSACTIONS");
        System.out.println("=================================================");

        for (Transaction transaction : transactions) {

            System.out.println();
            transaction.displayTransaction();
            System.out.println("-----------------------------------------------");
        }
    }

    public Transaction findTransactionById(String transactionId) {

        for (Transaction transaction : transactions) {

            if (transaction.getTransactionId().equals(transactionId)) {
                return transaction;
            }
        }

        return null;
    }
}