package StockTradingPlatform;

import java.io.*;
import java.util.*;

public class StockTradingPlatform {

    private static final String DATA_DIR = "stock_data";

    private static User user;
    private static Portfolio portfolio;
    private static List<Stock> stocks;
    private static List<Transaction> history;

    @SuppressWarnings("unchecked")
    private static <T> T loadData(String filename, T fallback) {
        File file = new File(DATA_DIR, filename);

        if (!file.exists()) {
            return fallback;
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException
                 | ClassCastException e) {
            System.out.println("Could not load " + filename
                    + ". Starting with default data.");
            return fallback;
        }
    }

    private static void saveObject(String filename, Object object) {
        File directory = new File(DATA_DIR);

        if (!directory.exists() && !directory.mkdirs()) {
            System.out.println("Could not create data directory.");
            return;
        }

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(new File(directory, filename)))) {
            out.writeObject(object);
        } catch (IOException e) {
            System.out.println("Error saving " + filename
                    + ": " + e.getMessage());
        }
    }

    private static void saveAll() {
        saveObject("user.dat", user);
        saveObject("portfolio.dat", portfolio);
        saveObject("stocks.dat", stocks);
        saveObject("history.dat", history);
    }

    private static Stock findStock(String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    }

    private static void displayMarket() {
        System.out.println("\n========== STOCK MARKET ==========");
        System.out.printf("%-12s %-24s %s%n",
                "Symbol", "Company", "Price");

        for (Stock stock : stocks) {
            stock.display();
        }
    }

    private static void buyStock(Scanner sc) {
        System.out.print("Enter stock symbol: ");
        Stock stock = findStock(sc.next().trim());

        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity: ");

        if (!sc.hasNextInt()) {
            System.out.println("Enter a valid whole number.");
            sc.next();
            return;
        }

        int quantity = sc.nextInt();

        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        double total = stock.getPrice() * quantity;

        if (user.deductMoney(total)) {
            portfolio.buy(stock.getSymbol(), quantity, stock.getPrice());

            history.add(new Transaction(
                    "BUY", stock.getSymbol(), quantity, stock.getPrice()));

            System.out.printf("Purchase successful! Total: ₹%.2f%n", total);
            saveAll();
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    private static void sellStock(Scanner sc) {
        System.out.print("Enter stock symbol: ");
        Stock stock = findStock(sc.next().trim());

        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity: ");

        if (!sc.hasNextInt()) {
            System.out.println("Enter a valid whole number.");
            sc.next();
            return;
        }

        int quantity = sc.nextInt();

        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        if (portfolio.sell(stock.getSymbol(), quantity)) {
            double total = stock.getPrice() * quantity;
            user.addMoney(total);

            history.add(new Transaction(
                    "SELL", stock.getSymbol(), quantity, stock.getPrice()));

            System.out.printf("Sale successful! Received: ₹%.2f%n", total);
            saveAll();
        } else {
            System.out.println("You do not own enough shares.");
        }
    }

    private static void updateMarketPrices() {
        Random random = new Random();

        for (Stock stock : stocks) {
            // Simulated price movement between -5% and +5%.
            double change = (random.nextDouble() * 0.10) - 0.05;
            double newPrice = stock.getPrice() * (1 + change);

            stock.setPrice(Math.max(0.01, newPrice));
        }

        System.out.println("Simulated market prices updated.");
        displayMarket();
        saveAll();
    }

    private static void displayHistory() {
        System.out.println("\n========== TRANSACTION HISTORY ==========");

        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        for (Transaction transaction : history) {
            System.out.println(transaction);
        }
    }

    public static void main(String[] args) {
        stocks = loadData("stocks.dat", new ArrayList<Stock>());

        if (stocks.isEmpty()) {
            stocks.add(new Stock("TCS", "Tata Consultancy", 3500));
            stocks.add(new Stock("INFY", "Infosys", 1800));
            stocks.add(new Stock("RELIANCE", "Reliance Industries", 2900));
            stocks.add(new Stock("WIPRO", "Wipro", 550));
        }

        user = loadData("user.dat", new User("Student", 100000));
        portfolio = loadData("portfolio.dat", new Portfolio());
        history = loadData("history.dat", new ArrayList<Transaction>());

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        System.out.println("Welcome, " + user.getName() + "!");
        System.out.println("Virtual trading account loaded.");

        do {
            System.out.println("\n===== STOCK TRADING PLATFORM =====");
            System.out.println("1. View Market Data");
            System.out.println("2. Buy Stocks");
            System.out.println("3. Sell Stocks");
            System.out.println("4. View Portfolio Performance");
            System.out.println("5. View Account Balance");
            System.out.println("6. View Transaction History");
            System.out.println("7. Update Simulated Market Prices");
            System.out.println("8. Save and Exit");
            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a number from 1 to 8.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    displayMarket();
                    break;

                case 2:
                    buyStock(sc);
                    break;

                case 3:
                    sellStock(sc);
                    break;

                case 4:
                    portfolio.display(stocks);
                    break;

                case 5:
                    System.out.printf("Available balance: ₹%.2f%n",
                            user.getBalance());
                    break;

                case 6:
                    displayHistory();
                    break;

                case 7:
                    updateMarketPrices();
                    break;

                case 8:
                    saveAll();
                    System.out.println("Data saved. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Select 1 to 8.");
            }

        } while (choice != 8);

        sc.close();
    }
}
