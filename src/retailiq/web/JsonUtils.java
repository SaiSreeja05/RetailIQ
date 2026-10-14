package retailiq.web;

import retailiq.*;

import java.util.List;
import java.util.Map;

public class JsonUtils {

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static String productToJson(Product p) {
        if (p == null) return "null";
        return String.format(
                "{\"productId\":%d,\"name\":\"%s\",\"category\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"stock\":%d,\"rating\":%.2f,\"productCode\":\"%s\"}",
                p.getProductId(), escape(p.getName()), escape(p.getCategory()), escape(p.getDescription()),
                p.getPrice(), p.getStock(), p.getRating(), escape(p.getProductCode())
        );
    }

    public static String customerToJson(Customer c) {
        if (c == null) return "null";
        return String.format(
                "{\"customerId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\"}",
                c.getCustomerId(), escape(c.getName()), escape(c.getEmail()), escape(c.getPhone()), escape(c.getAddress())
        );
    }

    public static String reviewToJson(Review r) {
        if (r == null) return "null";
        return String.format(
                "{\"reviewId\":%d,\"productId\":%d,\"customerId\":%d,\"reviewText\":\"%s\",\"rating\":%d}",
                r.getReviewId(), r.getProductId(), r.getCustomerId(), escape(r.getReviewText()), r.getRating()
        );
    }

    public static String transactionToJson(Transaction t) {
        if (t == null) return "null";
        return String.format(
                "{\"transactionId\":\"%s\",\"customerId\":%d,\"productId\":%d,\"quantity\":%d,\"totalAmount\":%.2f,\"transactionDate\":\"%s\"}",
                escape(t.getTransactionId()), t.getCustomerId(), t.getProductId(), t.getQuantity(), t.getTotalAmount(), escape(t.getTransactionDate())
        );
    }

    public static String warehouseToJson(Warehouse w) {
        if (w == null) return "null";
        return String.format(
                "{\"warehouseId\":%d,\"warehouseName\":\"%s\",\"location\":\"%s\",\"capacity\":%d}",
                w.getWarehouseId(), escape(w.getWarehouseName()), escape(w.getLocation()), w.getCapacity()
        );
    }

    public static String stringListToJson(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String stringDoubleMapToJson(Map<String, Double> map) {
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Double> e : map.entrySet()) {
            if (count++ > 0) sb.append(",");
            sb.append("\"").append(escape(e.getKey())).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    public static String stringIntMapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (count++ > 0) sb.append(",");
            sb.append("\"").append(escape(e.getKey())).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    public static String intIntMapToJson(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (count++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }
}
