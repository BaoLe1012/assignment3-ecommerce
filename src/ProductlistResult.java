import java.util.List;

/**
 * Helper class holding one page of products, the current page number,
 * and the total number of pages.
 */
public class ProductListResult {
    private final List<Product> products;
    private final int currentPage;
    private final int totalPages;

    public ProductListResult(List<Product> products, int currentPage, int totalPages) {
        this.products = products;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<Product> getProducts() {
        return products;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public String toString() {
        return "ProductListResult{page=" + currentPage + "/" + totalPages
                + ", items=" + products.size() + "}";
    }
}
