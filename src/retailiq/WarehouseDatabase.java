package retailiq;

import java.util.ArrayList;

public class WarehouseDatabase {

    private ArrayList<Warehouse> warehouses;

    public WarehouseDatabase() {

        warehouses = new ArrayList<>();

        warehouses.add(new Warehouse(
                401,
                "Hyderabad Central Warehouse",
                "Hyderabad",
                5000
        ));

        warehouses.add(new Warehouse(
                402,
                "Vijayawada Distribution Center",
                "Vijayawada",
                3500
        ));

        warehouses.add(new Warehouse(
                403,
                "Bengaluru Tech Warehouse",
                "Bengaluru",
                4500
        ));

        warehouses.add(new Warehouse(
                404,
                "Chennai Distribution Center",
                "Chennai",
                4000
        ));

        warehouses.add(new Warehouse(
                405,
                "Mumbai Central Warehouse",
                "Mumbai",
                5500
        ));
    }

    public ArrayList<Warehouse> getWarehouses() {
        return warehouses;
    }

    public void displayAllWarehouses() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("               RETAILIQ WAREHOUSES");
        System.out.println("=================================================");

        for (Warehouse warehouse : warehouses) {

            System.out.println();
            warehouse.displayWarehouse();
            System.out.println("-----------------------------------------------");
        }
    }

    public Warehouse findWarehouseById(int warehouseId) {

        for (Warehouse warehouse : warehouses) {

            if (warehouse.getWarehouseId() == warehouseId) {
                return warehouse;
            }
        }

        return null;
    }
}