import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all the operations related to the product.
 * This class follows the Singleton pattern.
 */
public class ProductOperation {

    // ---- Paths (constants, not class state) ----
    private static final String SOURCE_FOLDER = "data/product";      // raw CSV files
    private static final String PRODUCT_FILE = "data/products.txt";
    private static final String FIGURE_FOLDER = "data/figure";
    private static final int PAGE_SIZE = 10;

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    // ---- Singleton ----
    private static ProductOperation instance;

    private ProductOperation() {
    }

    /**
     * Returns the single instance of ProductOperation.
     * @return ProductOperation instance
     */
    public static synchronized ProductOperation getInstance() {
        if (instance == null) {
            instance = new ProductOperation();
        }
        return instance;
    }

    // =====================================================================
    // 2.9.2
    // =====================================================================

    /**
     * Extracts product information from the given product data files.
     * The data is saved into the data/products.txt file.
     */
    public void extractProductsFromFiles() {
        File folder = new File(SOURCE_FOLDER);
        File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            System.out.println("No product data files found in " + SOURCE_FOLDER);
            return;
        }

        // Keep insertion order, skip duplicated product IDs
        Map<String, Product> products = new LinkedHashMap<>();

        for (File csv : csvFiles) {
            String fileCategory = csv.getName().replaceFirst("(?i)\\.csv$", "");
            try (BufferedReader reader = Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8)) {
                List<String> header = null;
                String record;
                while ((record = readCsvRecord(reader)) != null) {
                    if (record.trim().isEmpty()) continue;
                    List<String> cols = parseCsvLine(record);
                    if (header == null) {
                        header = new ArrayList<>();
                        for (String h : cols) header.add(h.trim().replace("﻿", ""));
                        continue;
                    }
                    String id = col(header, cols, "id");
                    if (id.isEmpty() || products.containsKey(id)) continue;

                    String category = col(header, cols, "category");
                    if (category.isEmpty()) category = fileCategory;

                    Product p = new Product(
                            id,
                            col(header, cols, "model"),
                            category,
                            col(header, cols, "name"),
                            toDouble(col(header, cols, "current_price")),
                            toDouble(col(header, cols, "raw_price")),
                            toDouble(col(header, cols, "discount")),
                            (int) toDouble(col(header, cols, "likes_count"))
                    );
                    products.put(id, p);
                }
            } catch (IOException e) {
                System.out.println("Cannot read file " + csv.getName() + ": " + e.getMessage());
            }
        }

        writeAllProducts(new ArrayList<>(products.values()));
    }

    // =====================================================================
    // 2.9.3
    // =====================================================================

    /**
     * Retrieves one page of products from the database.
     * One page contains a maximum of 10 items.
     * @param pageNumber The page number to retrieve
     * @return A list of Product objects, current page number, and total pages
     */
    public ProductListResult getProductList(int pageNumber) {
        List<Product> all = readAllProducts();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));

        // Clamp page into valid range
        int page = Math.max(1, Math.min(pageNumber, totalPages));

        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        List<Product> pageItems = from < to ? new ArrayList<>(all.subList(from, to)) : new ArrayList<>();

        return new ProductListResult(pageItems, page, totalPages);
    }

    // =====================================================================
    // 2.9.4
    // =====================================================================

    /**
     * Deletes the product from the system based on the provided product_id.
     * @param productId The ID of the product to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteProduct(String productId) {
        if (productId == null) return false;
        List<Product> all = readAllProducts();
        boolean removed = all.removeIf(p -> productId.equals(p.getProId()));
        if (removed) {
            writeAllProducts(all);
        }
        return removed;
    }

    // =====================================================================
    // 2.9.5
    // =====================================================================

    /**
     * Retrieves all products whose name contains the keyword (case insensitive).
     * @param keyword The search keyword
     * @return A list of Product objects matching the keyword
     */
    public List<Product> getProductListByKeyword(String keyword) {
        List<Product> result = new ArrayList<>();
        if (keyword == null) return result;
        String kw = keyword.toLowerCase(Locale.ROOT);
        for (Product p : readAllProducts()) {
            String name = p.getProName();
            if (name != null && name.toLowerCase(Locale.ROOT).contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    // =====================================================================
    // 2.9.6
    // =====================================================================

    /**
     * Returns one product object based on the given product_id.
     * @param productId The ID of the product to retrieve
     * @return A Product object or null if not found
     */
    public Product getProductById(String productId) {
        if (productId == null) return null;
        for (Product p : readAllProducts()) {
            if (productId.equals(p.getProId())) {
                return p;
            }
        }
        return null;
    }

    // =====================================================================
    // 2.9.7
    // =====================================================================

    /**
     * Generates a bar chart showing the total number of products
     * for each category in descending order.
     * Saves the figure into the data/figure folder.
     */
    public void generateCategoryFigure() {
        Map<String, Double> counts = new HashMap<>();
        for (Product p : readAllProducts()) {
            counts.merge(p.getProCategory(), 1.0, Double::sum);
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : entries) {
            dataset.addValue(e.getValue(), "Products", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Total Products per Category", "Category", "Number of Products",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        saveChart(chart, "category_figure.png", 1000, 600);
    }

    // =====================================================================
    // 2.9.8
    // =====================================================================

    /**
     * Generates a pie chart showing the proportion of products that have
     * a discount value less than 30, between 30 and 60 inclusive,
     * and greater than 60.
     * Saves the figure into the data/figure folder.
     */
    public void generateDiscountFigure() {
        int less30 = 0, between = 0, greater60 = 0;
        for (Product p : readAllProducts()) {
            double d = p.getProDiscount();
            if (d < 30) less30++;
            else if (d <= 60) between++;
            else greater60++;
        }

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Discount < 30", less30);
        dataset.setValue("30 <= Discount <= 60", between);
        dataset.setValue("Discount > 60", greater60);

        JFreeChart chart = ChartFactory.createPieChart(
                "Product Discount Distribution", dataset, true, true, false);

        saveChart(chart, "discount_figure.png", 800, 600);
    }

    // =====================================================================
    // 2.9.9
    // =====================================================================

    /**
     * Generates a chart displaying the sum of products' likes_count
     * for each category in ascending order.
     * Saves the figure into the data/figure folder.
     */
    public void generateLikesCountFigure() {
        Map<String, Double> likes = new HashMap<>();
        for (Product p : readAllProducts()) {
            likes.merge(p.getProCategory(), (double) p.getProLikesCount(), Double::sum);
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(likes.entrySet());
        entries.sort(Map.Entry.comparingByValue());   // ascending

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : entries) {
            dataset.addValue(e.getValue(), "Likes", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Total Likes Count per Category", "Category", "Total Likes",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        saveChart(chart, "likes_count_figure.png", 1000, 600);
    }

    // =====================================================================
    // 2.9.10
    // =====================================================================

    /**
     * Generates a scatter chart showing the relationship between
     * likes_count and discount for all products.
     * Saves the figure into the data/figure folder.
     */
    public void generateDiscountLikesCountFigure() {
        XYSeries series = new XYSeries("Products", false, true);
        for (Product p : readAllProducts()) {
            series.add(p.getProDiscount(), p.getProLikesCount());
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Likes Count vs Discount", "Discount", "Likes Count",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        saveChart(chart, "discount_likes_count_figure.png", 1000, 600);
    }

    // =====================================================================
    // 2.9.11
    // =====================================================================

    /**
     * Removes all product data in the data/products.txt file.
     */
    public void deleteAllProducts() {
        writeAllProducts(Collections.emptyList());
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    /** Reads every product stored in data/products.txt. */
    private List<Product> readAllProducts() {
        List<Product> list = new ArrayList<>();
        Path path = Paths.get(PRODUCT_FILE);
        if (!Files.exists(path)) return list;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                Product p = parseProduct(line);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            System.out.println("Cannot read " + PRODUCT_FILE + ": " + e.getMessage());
        }
        return list;
    }

    /** Overwrites data/products.txt with the given products (one per line). */
    private void writeAllProducts(List<Product> products) {
        Path path = Paths.get(PRODUCT_FILE);
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (Product p : products) {
                    writer.write(formatProduct(p));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot write " + PRODUCT_FILE + ": " + e.getMessage());
        }
    }

    /** Product -> one line, e.g. {"pro_id":"123","pro_model":"...",...} */
    private String formatProduct(Product p) {
        return "{"
                + "\"pro_id\":\"" + esc(p.getProId()) + "\", "
                + "\"pro_model\":\"" + esc(p.getProModel()) + "\", "
                + "\"pro_category\":\"" + esc(p.getProCategory()) + "\", "
                + "\"pro_name\":\"" + esc(p.getProName()) + "\", "
                + "\"pro_current_price\":\"" + p.getProCurrentPrice() + "\", "
                + "\"pro_raw_price\":\"" + p.getProRawPrice() + "\", "
                + "\"pro_discount\":\"" + p.getProDiscount() + "\", "
                + "\"pro_likes_count\":\"" + p.getProLikesCount() + "\""
                + "}";
    }

    /** One line -> Product (null if the line is invalid). */
    private Product parseProduct(String line) {
        Map<String, String> f = new HashMap<>();
        Matcher m = FIELD_PATTERN.matcher(line);
        while (m.find()) {
            f.put(m.group(1), unesc(m.group(2)));
        }
        if (!f.containsKey("pro_id")) return null;
        return new Product(
                f.get("pro_id"),
                f.getOrDefault("pro_model", ""),
                f.getOrDefault("pro_category", ""),
                f.getOrDefault("pro_name", ""),
                toDouble(f.get("pro_current_price")),
                toDouble(f.get("pro_raw_price")),
                toDouble(f.get("pro_discount")),
                (int) toDouble(f.get("pro_likes_count"))
        );
    }

    /** Saves a chart as PNG into data/figure. */
    private void saveChart(JFreeChart chart, String fileName, int width, int height) {
        try {
            File dir = new File(FIGURE_FOLDER);
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, fileName);
            ChartUtils.saveChartAsPNG(out, chart, width, height);
            System.out.println("Figure saved to " + out.getPath());
        } catch (IOException e) {
            System.out.println("Cannot save figure " + fileName + ": " + e.getMessage());
        }
    }

    /** Reads one CSV record, joining lines when a quoted field spans several lines. */
    private static String readCsvRecord(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) return null;
        StringBuilder sb = new StringBuilder(line);
        while (countQuotes(sb) % 2 != 0) {
            String next = reader.readLine();
            if (next == null) break;
            sb.append('\n').append(next);
        }
        return sb.toString();
    }

    private static int countQuotes(CharSequence s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '"') n++;
        return n;
    }

    /** Splits a CSV record into columns, handling quoted values and "" escapes. */
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }

    private static String col(List<String> header, List<String> cols, String name) {
        int idx = header.indexOf(name);
        return (idx >= 0 && idx < cols.size()) ? cols.get(idx).trim() : "";
    }

    private static double toDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }

    private static String unesc(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
