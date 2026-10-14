package retailiq.csv;

import retailiq.Warehouse;
import retailiq.WarehouseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WarehouseCSVRepository {

    private final String filePath;
    private final List<Warehouse> warehouses = new ArrayList<>();

    public WarehouseCSVRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    public synchronized void load() {
        warehouses.clear();
        try {
            List<String> lines = CSVUtils.readLines(filePath);
            if (lines.isEmpty()) {
                seedDefaults();
                return;
            }

            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("warehouseid")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = CSVUtils.parseLine(line);
                if (cols.size() >= 4) {
                    int id = CSVUtils.parseInt(cols.get(0), 0);
                    String name = cols.get(1);
                    String location = cols.get(2);
                    int cap = CSVUtils.parseInt(cols.get(3), 0);

                    Warehouse warehouse = new Warehouse(id, name, location, cap);
                    DataValidator.ValidationResult val = DataValidator.validateWarehouse(warehouse);
                    if (val.isValid()) {
                        warehouses.add(warehouse);
                    } else {
                        System.err.println("Skipping invalid warehouse row: " + val.getErrorMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load warehouses CSV: " + e.getMessage());
            seedDefaults();
        }
    }

    private void seedDefaults() {
        WarehouseDatabase db = new WarehouseDatabase();
        warehouses.addAll(db.getWarehouses());
        persist();
    }

    public synchronized void persist() {
        List<String> lines = new ArrayList<>();
        lines.add("warehouseId,warehouseName,location,capacity");
        for (Warehouse w : warehouses) {
            lines.add(CSVUtils.formatLine(
                    w.getWarehouseId(),
                    w.getWarehouseName(),
                    w.getLocation(),
                    w.getCapacity()
            ));
        }
        try {
            CSVUtils.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println("Could not write warehouses CSV: " + e.getMessage());
        }
    }

    public synchronized List<Warehouse> findAll() {
        return new ArrayList<>(warehouses);
    }

    public synchronized Optional<Warehouse> findById(int id) {
        return warehouses.stream().filter(w -> w.getWarehouseId() == id).findFirst();
    }

    public synchronized void save(Warehouse warehouse) {
        int index = -1;
        for (int i = 0; i < warehouses.size(); i++) {
            if (warehouses.get(i).getWarehouseId() == warehouse.getWarehouseId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            warehouses.set(index, warehouse);
        } else {
            warehouses.add(warehouse);
        }
        persist();
    }

    public synchronized boolean deleteById(int id) {
        boolean removed = warehouses.removeIf(w -> w.getWarehouseId() == id);
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized void replaceAll(List<Warehouse> newWarehouses) {
        warehouses.clear();
        warehouses.addAll(newWarehouses);
        persist();
    }
}
