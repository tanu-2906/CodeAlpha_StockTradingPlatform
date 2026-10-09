package StockTradingPlatform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Integer> holdings = new HashMap<>();
    private Map<String, Double> averageBuyPrice = new HashMap<>();

    public void buy(String symbol, int quantity, double price) {
        int oldQuantity = holdings.getOrDefault(symbol, 0);
        double oldAverage = averageBuyPrice.getOrDefault(symbol, 0.0);

        int newQuantity = oldQuantity + quantity;

        double newAverage =
                ((oldQuantity * oldAverage) + (quantity * price))
                / newQuantity;

        holdings.put(symbol, newQuantity);
        averageBuyPrice.put(symbol, newAverage);
    }

    public boolean sell(String symbol, int quantity) {
        int owned = holdings.getOrDefault(symbol, 0);

        if (quantity <= 0 || quantity > owned) {
            return false;
        }

        int remaining = owned - quantity;

        if (remaining == 0) {
            holdings.remove(symbol);
            averageBuyPrice.remove(symbol);
        } else {
            holdings.put(symbol, remaining);
        }

        return true;
    }

    public int getQuantity(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public double getAverageBuyPrice(String symbol) {
    return averageBuyPrice.getOrDefault(symbol, 0.0);
}

    public Map<String, Integer> getHoldings() {
    return new HashMap<>(holdings);
    }

    public void display(List<Stock> stocks) {
        System.out.println("\n========== PORTFOLIO ==========");

        double invested = 0;
        double currentValue = 0;
        boolean found = false;

        System.out.printf("%-12s %-10s %-14s %-14s%n",
                "Symbol", "Quantity", "Current Value", "Profit/Loss");

        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            int quantity = entry.getValue();
            double avgPrice = averageBuyPrice.get(symbol);

            for (Stock stock : stocks) {
                if (stock.getSymbol().equals(symbol)) {
                    double cost = quantity * avgPrice;
                    double value = quantity * stock.getPrice();
                    double profitLoss = value - cost;

                    invested += cost;
                    currentValue += value;
                    found = true;

                    System.out.printf("%-12s %-10d ₹%-13.2f ₹%+.2f%n",
                            symbol, quantity, value, profitLoss);
                    break;
                }
            }
        }

        if (!found) {
            System.out.println("No stocks in your portfolio.");
        }

        System.out.printf("%nTotal invested: ₹%.2f%n", invested);
        System.out.printf("Current value:  ₹%.2f%n", currentValue);
        System.out.printf("Unrealized P/L: ₹%+.2f%n",
                currentValue - invested);
    }
}
