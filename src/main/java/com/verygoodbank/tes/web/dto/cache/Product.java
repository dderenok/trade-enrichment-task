package com.verygoodbank.tes.web.dto.cache;

import java.io.Serial;

/**
 * Product is a concrete implementation of {@link BaseWrapper}.
 * It represents a product with a specific name and a state indicating whether it is ready to be removed.
 */
public class Product implements BaseWrapper {
    @Serial
    private static final long serialVersionUID = 97835982623819787L;
    private final String productName;
    private boolean readyToRemove;

    public Product(String productName, boolean readyToRemove) {
        this.productName = productName;
        this.readyToRemove = readyToRemove;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isReadyToRemove() {
        return readyToRemove;
    }

    public void setReadyToRemove(boolean readyToRemove) {
        this.readyToRemove = readyToRemove;
    }
}
