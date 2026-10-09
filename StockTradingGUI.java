package StockTradingPlatform;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class StockTradingGUI extends JFrame {

    private User user;
    private Portfolio portfolio;
    private java.util.List<Stock> stocks;
    private java.util.List<Transaction> history;

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Symbol", "Company", "Price (INR)"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField symbolField = new JTextField(9);
    private final JTextField quantityField = new JTextField(5);
    private final JLabel balanceLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();
    private final JTextArea output = new JTextArea(10, 55);

    private static final String DIR = "stock_data";

    @SuppressWarnings("unchecked")
    private <T> T load(String file, T fallback) {
        File f = new File(DIR, file);
        if (!f.exists()) return fallback;

        try (ObjectInputStream in =
                 new ObjectInputStream(new FileInputStream(f))) {
            return (T) in.readObject();
        } catch (Exception e) {
            return fallback;
        }
    }

    private void save(String file, Object data) {
        try {
            File dir = new File(DIR);
            if (!dir.exists()) dir.mkdirs();

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(new File(dir, file)))) {
                out.writeObject(data);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Could not save data: " + e.getMessage());
        }
    }

    private void saveAll() {
        save("user.dat", user);
        save("portfolio.dat", portfolio);
        save("stocks.dat", stocks);
        save("history.dat", history);
    }

    public StockTradingGUI() {
        setTitle("Stock Trading Platform");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        stocks = load("stocks.dat", new ArrayList<Stock>());
        if (stocks.isEmpty()) {
            stocks.add(new Stock("TCS", "Tata Consultancy", 3500));
            stocks.add(new Stock("INFY", "Infosys", 1800));
            stocks.add(new Stock("RELIANCE", "Reliance Industries", 2900));
            stocks.add(new Stock("WIPRO", "Wipro", 550));
        }

        user = load("user.dat", new User("Student", 100000));
        portfolio = load("portfolio.dat", new Portfolio());
        history = load("history.dat", new ArrayList<Transaction>());

        JPanel header = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("STOCK TRADING PLATFORM",
                                 SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        header.add(title);

        JPanel summary = new JPanel();
        summary.add(balanceLabel);
        summary.add(valueLabel);
        header.add(summary);

        JPanel tradePanel = new JPanel(new FlowLayout());
        tradePanel.add(new JLabel("Symbol:"));
        tradePanel.add(symbolField);
        tradePanel.add(new JLabel("Quantity:"));
        tradePanel.add(quantityField);

        JButton buy = new JButton("Buy Stock");
        JButton sell = new JButton("Sell Stock");
        JButton portfolioButton = new JButton("View Portfolio");
        JButton historyButton = new JButton("Transaction History");
        JButton update = new JButton("Update Market");
        JButton saveExit = new JButton("Save & Exit");

        tradePanel.add(buy);
        tradePanel.add(sell);
        tradePanel.add(portfolioButton);
        tradePanel.add(historyButton);
        tradePanel.add(update);
        tradePanel.add(saveExit);

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(tradePanel, BorderLayout.NORTH);
        bottom.add(new JScrollPane(output), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        buy.addActionListener(e -> trade(true));
        sell.addActionListener(e -> trade(false));
        portfolioButton.addActionListener(e -> showPortfolio());
        historyButton.addActionListener(e -> showHistory());
        update.addActionListener(e -> updatePrices());

        saveExit.addActionListener(e -> {
            saveAll();
            dispose();
        });

        refreshMarket();
        updateSummary();
        saveAll();
    }

    private Stock findStock(String symbol) {
        for (Stock s : stocks) {
            if (s.getSymbol().equalsIgnoreCase(symbol)) return s;
        }
        return null;
    }

    private void refreshMarket() {
        model.setRowCount(0);
        for (Stock s : stocks) {
            model.addRow(new Object[]{
                s.getSymbol(), s.getCompanyName(),
                String.format("%.2f", s.getPrice())
            });
        }
    }

    private void updateSummary() {
        double value = 0;
        for (Stock s : stocks) {
            value += portfolio.getQuantity(s.getSymbol()) * s.getPrice();
        }

        balanceLabel.setText(String.format(
            "Available Balance: Rs. %.2f", user.getBalance()));
        valueLabel.setText(String.format(
            "Portfolio Market Value: Rs. %.2f", value));
    }

    private void trade(boolean buying) {
        String symbol = symbolField.getText().trim().toUpperCase();

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid positive quantity.");
            return;
        }

        Stock stock = findStock(symbol);
        if (stock == null) {
            JOptionPane.showMessageDialog(this,
                "Stock not found. Select TCS, INFY, RELIANCE or WIPRO.");
            return;
        }

        double price = stock.getPrice();
        double total = price * quantity;

        if (buying) {
            if (!user.deductMoney(total)) {
                JOptionPane.showMessageDialog(this,
                    "Insufficient balance!");
                return;
            }

            portfolio.buy(symbol, quantity, price);
        } else {
            if (!portfolio.sell(symbol, quantity)) {
                JOptionPane.showMessageDialog(this,
                    "You do not own enough shares!");
                return;
            }

            user.addMoney(total);
        }

        history.add(new Transaction(
            buying ? "BUY" : "SELL", symbol, quantity, price));

        output.append((buying ? "BUY" : "SELL") + " successful: "
            + quantity + " shares of " + symbol
            + " | Total Rs. " + String.format("%.2f", total) + "\n");

        refreshMarket();
        updateSummary();
        saveAll();
    }

    private void showPortfolio() {
        output.setText("========== YOUR PORTFOLIO ==========\n");

        double invested = 0;
        double currentValue = 0;
        boolean found = false;

        for (Stock s : stocks) {
            int qty = portfolio.getQuantity(s.getSymbol());

            if (qty > 0) {
                double cost = qty *
                    portfolio.getAverageBuyPrice(s.getSymbol());
                double value = qty * s.getPrice();

                invested += cost;
                currentValue += value;
                found = true;

                output.append(String.format(
                    "%s | Shares: %d | Value: Rs. %.2f | P/L: Rs. %+.2f%n",
                    s.getSymbol(), qty, value, value - cost));
            }
        }

        if (!found) output.append("No stocks owned yet.\n");

        output.append(String.format(
            "Total invested: Rs. %.2f%nCurrent value: Rs. %.2f%n"
            + "Unrealized Profit/Loss: Rs. %+.2f%n",
            invested, currentValue, currentValue - invested));

        updateSummary();
    }

    private void showHistory() {
        output.setText("========== TRANSACTION HISTORY ==========\n");

        if (history.isEmpty()) {
            output.append("No transactions yet.\n");
            return;
        }

        for (Transaction t : history) {
            output.append(t.toString() + "\n");
        }
    }

    private void updatePrices() {
        Random random = new Random();

        for (Stock s : stocks) {
            double change = random.nextDouble() * 0.10 - 0.05;
            s.setPrice(Math.max(0.01, s.getPrice() * (1 + change)));
        }

        refreshMarket();
        updateSummary();
        saveAll();
        output.append("Simulated market prices updated.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new StockTradingGUI().setVisible(true));
    }
}