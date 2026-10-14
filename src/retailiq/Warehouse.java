package retailiq;

public class Warehouse {

    private int warehouseId;
    private String warehouseName;
    private String location;
    private int capacity;

    public Warehouse(int warehouseId, String warehouseName,
                     String location, int capacity) {

        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.location = location;
        this.capacity = capacity;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void displayWarehouse() {

        System.out.println("Warehouse ID   : " + warehouseId);
        System.out.println("Warehouse Name : " + warehouseName);
        System.out.println("Location       : " + location);
        System.out.println("Capacity       : " + capacity);
    }
}