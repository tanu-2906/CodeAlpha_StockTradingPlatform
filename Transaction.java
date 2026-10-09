package StockTradingPlatform;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String symbol;
    private int quantity;
    private double price;
    private LocalDateTime date;

    public Transaction(String type, String symbol,
                       int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.date = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return date.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                + " | " + type
                + " | " + symbol
                + " | Quantity: " + quantity
                + " | Price: ₹" + String.format("%.2f", price)
                + " | Total: ₹"
                + String.format("%.2f", price * quantity);
    }
}
